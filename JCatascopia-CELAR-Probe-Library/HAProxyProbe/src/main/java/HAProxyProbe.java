import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;

import eu.celarcloud.jcatascopia.probepack.Probe;
import eu.celarcloud.jcatascopia.probepack.ProbeMetric;
import eu.celarcloud.jcatascopia.probepack.ProbePropertyType;

/**
 * 
 * @author Demetris Trihinas
 *
 */
public class HAProxyProbe extends Probe{
	
	private static final String CONFIG_PATH = "haproxy.properties";
	private static String auth_header;
	private static String haproxy_url;
	
	private String proxy_name;
	HashMap<String,Integer> lastValues;
	private long lasttime;
	
	private Properties config;
	
	private boolean firstflag = true;
	
	@SuppressWarnings("restriction")
	public HAProxyProbe(String name, int freq){
		super(name,freq);
		this.addProbeProperty(0,"activeSessions",ProbePropertyType.INTEGER,"#","Number of active connections");
		this.addProbeProperty(1,"requestRate",ProbePropertyType.DOUBLE,"req/s","Requests per second");
		this.addProbeProperty(2,"proxyBytesIN",ProbePropertyType.DOUBLE,"bytes/s","Bytes IN per second");
		this.addProbeProperty(3,"proxyBytesOUT",ProbePropertyType.DOUBLE,"bytes/s","Bytes OUT per second");
		this.addProbeProperty(4,"avgResponseTime",ProbePropertyType.DOUBLE,"ms","Average response time in ms of all servers");
		this.addProbeProperty(5,"servers",ProbePropertyType.INTEGER,"#","Number of servers behind proxy");
		this.addProbeProperty(6,"errorRate",ProbePropertyType.DOUBLE,"err/s","Errors per second");

		parseConfig();
	    String user = config.getProperty("haproxy_username", "user");
	    String pass = config.getProperty("haproxy_password", "password");
	    String ip = config.getProperty("haproxy_ip", "localhost");
	    String port = config.getProperty("haproxy_port", "30000");
	    proxy_name = config.getProperty("haproxy_proxy_name", "myproxy")+",";

	    String creds = user+":"+pass;
		auth_header = "Basic " + new sun.misc.BASE64Encoder().encode(creds.getBytes());
		haproxy_url = "http://"+ip+":"+port+"/haproxy?stats;csv";
		
		lastValues = this.calcValues();
		lasttime = System.currentTimeMillis()/1000;		
	}
	
	public HAProxyProbe(){
		this("HAProxyProbe",20);
	}
			
	@Override
	public String getDescription() {
		return "HAProxyProbe collect's HAProxy Load Balancer usage stats";
	}

	@Override
	public ProbeMetric collect() {
		int activeSessions = 0;
		double requestRate = 0.0;
		double avgResponseTime = 0.0;
		double bytesIn = 0.0;
		double bytesOut = 0.0;
		double errorRate = 0.0;
		int servers = 0;
		
		long curtime = System.currentTimeMillis()/1000;
		HashMap<String,Integer> curValues = this.calcValues();

	    long timediff = curtime-lasttime;
		if (curValues != null && timediff >= 0){
			activeSessions = curValues.get("scur");
		    requestRate = (1.0 * (curValues.get("stot")-lastValues.get("stot")))/timediff;
		    servers = curValues.get("servers");
		    avgResponseTime = curValues.get("rtime")/servers;
		    bytesIn = (1.0 * (curValues.get("bin")-lastValues.get("bin")))/timediff;
		    bytesOut = (1.0 * (curValues.get("bout")-lastValues.get("bout")))/timediff;
		    errorRate = (1.0 * (curValues.get("err")-lastValues.get("err")))/timediff;
		}   
		
        HashMap<Integer,Object> values = new HashMap<Integer,Object>();
        values.put(0, activeSessions);
        values.put(1, requestRate);
        values.put(2, bytesIn);
        values.put(3, bytesOut);
        values.put(4, avgResponseTime);
        values.put(5, servers);
        values.put(6, errorRate);
	    
        this.lasttime = curtime;
        this.lastValues = curValues;
        
//	    for (Entry<Integer,Object> entry : values.entrySet()){
//			System.out.println(entry.getKey()+" "+entry.getValue());
//		}
	    
		return new ProbeMetric(values);
	}
	
	private HashMap<String,Integer> calcValues(){
		HashMap<String,Integer> statMap = new HashMap<String,Integer>();
		try{
			URL obj = new URL(haproxy_url);
			HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
			conn.setRequestMethod("GET");		
			conn.setRequestProperty("Authorization", auth_header);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			
			int scur = 0, stot = 0, servers = 0, err = 0, bin = 0, bout = 0, rtime = 0;
			
			if(conn.getResponseCode() == 200){
				while ((line = in.readLine()) != null){
					if(line.contains(this.proxy_name) && !line.contains("BACKEND") && !line.contains("FRONTEND")){
						String[] tokenz = line.split(",");
						scur += Integer.parseInt(tokenz[4]);
						stot += Integer.parseInt(tokenz[7]);
						bin += Integer.parseInt(tokenz[8]);
						bout += Integer.parseInt(tokenz[9]);
						err += Integer.parseInt(tokenz[12]);
						err += Integer.parseInt(tokenz[13]);
						err += Integer.parseInt(tokenz[14]);
						if (tokenz.length >= 60)
							rtime += Integer.parseInt(tokenz[60]);
						else {
							rtime = 0;
							if (firstflag){
								this.writeToProbeLog(Level.WARNING, "This version of haproxy does no support response time. try version 1.5 or higher");
								firstflag = false;
							}
						}
						servers++;
						continue;
					}
				}
				statMap.put("scur", scur);
				statMap.put("stot", stot);
				statMap.put("bin", bin);
				statMap.put("bout", bout);
				statMap.put("err", err);
				statMap.put("servers", servers);
				statMap.put("rtime", rtime);
			}
			in.close();			
		}
		catch(Exception e){
			this.writeToProbeLog(Level.WARNING, e);
			return null;
		}
		return statMap;
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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HAProxyProbe probe = new HAProxyProbe();
		probe.activate();
	}
}
