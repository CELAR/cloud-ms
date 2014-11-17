
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;

import eu.celarcloud.jcatascopia.probepack.Probe;
import eu.celarcloud.jcatascopia.probepack.ProbeMetric;
import eu.celarcloud.jcatascopia.probepack.ProbePropertyType;


public class CassandraProbe extends Probe{
	
	private static int DEFAULT_SAMPLING_PERIOD = 45;
	private static String DEFAULT_PROBE_NAME = "CassandraProbe";
	
	private static final String CONFIG_PATH = "cassandra.properties";
	private Properties config;
	private HashMap<String,Integer> keyspaces;
	private String nodetool_path;
	
	private String myIP;
	private boolean nodeloadflag;
	private int loadint;

	public CassandraProbe(String name, int freq) {
		super(name, freq);
		
		parseConfig();
				
		keyspaces = new HashMap<String,Integer>();
		String[] ks = this.config.getProperty("keyspaces", "").split(";");
		int j = 0, i = 0, c = 0;
		while(i < ks.length){
			this.addProbeProperty(j++,ks[i]+"_readLatency",ProbePropertyType.DOUBLE,"ms",ks[i]+" keyspace read latency");
			this.addProbeProperty(j++,ks[i]+"_writeLatency",ProbePropertyType.DOUBLE,"ms",ks[i]+" keyspace write latency");
			this.addProbeProperty(j++,ks[i]+"_load",ProbePropertyType.LONG,"KB",ks[i]+" keyspace total space used in KBytes");
			keyspaces.put(ks[i], c);
			i++; c += 3;
		}
		
		this.nodeloadflag = Boolean.parseBoolean(this.config.getProperty("calc_node_load","false"));
		if (nodeloadflag){
			this.loadint = j;
			this.addProbeProperty(this.loadint,"node_load",ProbePropertyType.DOUBLE,"KB","load on the node in KB");
			myIP = getMyIP();
			System.out.println("Node Load will attempt to be calculated for host: " + myIP);
//			myIP="127.0.0.1";
		}
		
		nodetool_path = config.getProperty("nodetool_path", "nodetool");
	}
	
	public CassandraProbe(){
		this(DEFAULT_PROBE_NAME, DEFAULT_SAMPLING_PERIOD);
	}

	@Override
	public ProbeMetric collect() {
		HashMap<Integer,Object> values = new HashMap<Integer,Object>();
				
		HashMap<String,Double> temp = this.calcValues();
		double r,w;
		long b;
		for(Entry<String, Integer> entry:keyspaces.entrySet()){
			r = (temp.get(entry.getKey()+"_readLatency") != null) ? temp.get(entry.getKey()+"_readLatency") : Double.NaN;
			w = (temp.get(entry.getKey()+"_writeLatency") != null) ? temp.get(entry.getKey()+"_writeLatency") : Double.NaN;
			b = (temp.get(entry.getKey()+"_load") != null) ? temp.get(entry.getKey()+"_load").longValue() : -1;
			values.put(entry.getValue(), r);
			values.put(entry.getValue()+1, w);
			values.put(entry.getValue()+2, b);
		}
		
		if (this.nodeloadflag){
			double l = 0;
			l = this.calcNodeLoad();
			values.put(this.loadint, l);
		}
		
//		for(Entry<Integer,Object> entry:values.entrySet()){
//			System.out.println(entry.getKey()+" "+entry.getValue());
//		}
		
		return new ProbeMetric(values);
	}
	
	private HashMap<String,Double> calcValues(){
		HashMap<String,Double> stats = new HashMap<String,Double>();
		try{	
			String[] cmd = {"/bin/sh", "-c", nodetool_path + " cfstats"};
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			//BufferedReader b = new BufferedReader(new FileReader(new File("cassandra_example")));
			String line = "";
			while ((line = b.readLine()) != null){
				if (line.contains("Keyspace: ")){
					String s = line.split(" ")[1];
					if (keyspaces.containsKey(s)){
						b.readLine();
						line = b.readLine();
						stats.put(s+"_readLatency", Double.parseDouble(line.split("\\s+")[3]));
						b.readLine();
						line = b.readLine();
						stats.put(s+"_writeLatency", Double.parseDouble(line.split("\\s+")[3]));
						
						double size = 0;
						while (!(line = b.readLine()).contains("----------------") && line != null){
							if (line.contains("Space used (total), bytes:"))
								size += Double.parseDouble(line.split(":")[1].trim());
						}
						stats.put(s+"_load", size/1000);	
					}
				}
			}
	        b.close();			
		}
		catch(Exception e){
			this.writeToProbeLog(Level.SEVERE, "Couldn't get Cassandra node stats"); 
		}
		
		return stats;
	}

	@Override
	public String getDescription() {
		return "Probe that collects read/write latency from Cassandra database";
	}
	
	//parse the configuration file
	private void parseConfig(){
		this.config = new Properties();
		//load config properties file
		try {				
			InputStream fis = getClass().getResourceAsStream(CONFIG_PATH);
			config.load(fis);
			if (fis != null)
	    		fis.close();
		} 
		catch (FileNotFoundException e){
			this.writeToProbeLog(Level.SEVERE,"config file not found");
		} 
		catch (IOException e){
			this.writeToProbeLog(Level.SEVERE,"config file parsing error");
		}
	}
	
	private double calcNodeLoad(){
		double load = 0;
		try{
			String[] cmd = {"/bin/sh", "-c", nodetool_path + " status"};
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			boolean flag = false;
			String line = "";
			while ((line = b.readLine()) != null){
				if (line.contains("Address")) {
					flag = true;
					continue;
				}
				if (line.matches(".* \\d+ .*") && flag){
					String[] tmp = line.split("\\s+");
					if (tmp[1].equals(this.myIP)){
						load = Double.parseDouble(tmp[2]);
						if (tmp[3].equals("MB")) load*=1024;
						else if (tmp[3].equals("GB")) load=load*1024*1024;
					}	
				}
			}
			b.close();
		}
		catch(Exception e){
			e.printStackTrace();
			this.writeToProbeLog(Level.SEVERE, "Couldn't get Cassandra node load"); 
		}
		return load;
	}
	
	private static String getMyIP(){
		try {	
			String[] cmd = {"/bin/sh", "-c", "ifconfig eth0 | grep -o 'inet addr:[0-9.]*' | grep -o [0-9.]*"};
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			return b.readLine();
			
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) {
		CassandraProbe p = new CassandraProbe();
		p.activate();
	}
}
