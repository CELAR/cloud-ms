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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import eu.celarcloud.jcatascopia.probepack.Probe;
import eu.celarcloud.jcatascopia.probepack.ProbeMetric;
import eu.celarcloud.jcatascopia.probepack.ProbePropertyType;

public class DataPlayProbe extends Probe{
	
	private static final String CONFIG_PATH = "dataplay.properties";
	private Properties config;
	private String playgen_url;

	public DataPlayProbe(String name, int freq) {
		super(name, freq);
		
		parseConfig();
		String ip = config.getProperty("playgen.ip", "localhost");
	    String port = config.getProperty("playgen.port", "3000");
	  
	    playgen_url = "http://"+ip+":"+port+"/api/info";
	    
	    this.addProbeProperty(0,"mean",ProbePropertyType.INTEGER,"#","");
	    this.addProbeProperty(1,"standev",ProbePropertyType.INTEGER,"#","");
	    this.addProbeProperty(2,"variation",ProbePropertyType.DOUBLE,"#","");	 
	}
	
	public DataPlayProbe(){
		this("DataplayProbe",20);
	}
	
	@Override
	public String getDescription() {
		return "A JCatascopia Probe collecting DataPlay master node stats";
	}

	@Override
	public ProbeMetric collect() {
		HashMap<Integer,Object> values = new HashMap<Integer,Object>();
		try{
			URL obj = new URL(playgen_url);
			HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
			conn.setRequestMethod("GET");		
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			if(conn.getResponseCode() == 200){
				while ((line = in.readLine()) != null)
					sb.append(line);
				
				JSONParser parser = new JSONParser(); 
				JSONObject json = (JSONObject) parser.parse(sb.toString());
				
				int mean = ((Long) json.get("mean")).intValue(); 
				int standev = ((Long) json.get("standev")).intValue(); 
				double variation = (Double) json.get("variation"); 

				values.put(0, mean);
				values.put(1, standev);
				values.put(2, variation);
			}
		}
		catch(Exception e){
			this.writeToProbeLog(Level.WARNING, e);
		}

//	    for (Entry<Integer, Object> entry : values.entrySet()){
//			System.out.println(entry.getKey()+" "+entry.getValue());
//		}
		return new ProbeMetric(values);
	}	
	
	//parse the configuration file
	private void parseConfig() {
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
		DataPlayProbe probe = new DataPlayProbe();
		probe.activate();
	}
}
