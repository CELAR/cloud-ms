package eu.celarcloud.celar_ms.ProbePack.ProbeLibrary;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;

import eu.celarcloud.celar_ms.ProbePack.Probe;
import eu.celarcloud.celar_ms.ProbePack.ProbeMetric;
import eu.celarcloud.celar_ms.ProbePack.ProbePropertyType;

/**
 * 
 * @author Demetris Trihinas
 *
 */
public class TomcatProbe extends Probe{
	
	private static final String user = "user";
	private static final String pass = "password";
	private static final String GlobalRequestProcessor_URL = "http://localhost:8080/manager/jmxproxy?qry=Catalina:name=%22http-bio-8080%22,type=GlobalRequestProcessor";
	private static final String ThreadPool_URL = "http://localhost:8080/manager/jmxproxy?qry=Catalina:name=%22http-bio-8080%22,type=ThreadPool";
	private static String auth_header;
	private long lastRequestCount;
	private long lasttime;
	
	@SuppressWarnings("restriction")
	public TomcatProbe(String name, int freq){
		super(name,freq);
		this.addProbeProperty(0,"maxThreads",ProbePropertyType.LONG,"","");
	    this.addProbeProperty(1,"currentThreadCount",ProbePropertyType.LONG,"","");
	    this.addProbeProperty(2,"currentThreadsBusy",ProbePropertyType.LONG,"","");
	    this.addProbeProperty(3,"bytesReceived",ProbePropertyType.LONG,"Bytes","");
	    this.addProbeProperty(4,"bytesSent",ProbePropertyType.LONG,"Bytes","");
	    this.addProbeProperty(5,"requestCount",ProbePropertyType.LONG,"","");
	    this.addProbeProperty(6,"errorCount",ProbePropertyType.LONG,"","");
	    this.addProbeProperty(7,"processingTime",ProbePropertyType.LONG,"","");
	    this.addProbeProperty(8,"requestThroughput",ProbePropertyType.DOUBLE,"req/s","");
	    
	    String creds = user+":"+pass;
		auth_header = "Basic " + new sun.misc.BASE64Encoder().encode(creds.getBytes());
		
		this.lastRequestCount = 0;
		this.lasttime = System.currentTimeMillis()/1000;
	}
	
	public TomcatProbe(){
		this("TomcatProbe",40);
	}
		
	@Override
	public String getDescription() {
		return "TomcatProbe collect's Apache Tomcat usage stats.";
	}

	@Override
	public ProbeMetric collect() {
		HashMap<String,Long> curValues = this.calcValues();
		long curtime = System.currentTimeMillis()/1000;

		long maxThreads = 0;
	    long currentThreadCount = 0;
	    long currentThreadsBusy = 0;
	    long bytesReceived = 0;
	    long bytesSent = 0;
	    long requestCount = 0;
	    long errorCount = 0;
	    long processingTime = 0;
	    double requestThroughput = 0.0;
	    
		if (curValues != null){
			maxThreads = curValues.get("maxThreads");
		    currentThreadCount = curValues.get("currentThreadCount");
		    currentThreadsBusy = curValues.get("currentThreadsBusy");
		    bytesReceived = curValues.get("bytesReceived");
		    bytesSent = curValues.get("bytesSent");
		    requestCount = curValues.get("requestCount");
		    errorCount = curValues.get("errorCount");
		    processingTime = curValues.get("processingTime");
		    requestThroughput = (1.0*(requestCount - lastRequestCount))/(curtime-lasttime);
		}
		
        HashMap<Integer,Object> values = new HashMap<Integer,Object>();
        values.put(0, maxThreads);
	    values.put(1, currentThreadCount);
	    values.put(2, currentThreadsBusy);
	    values.put(3, bytesReceived);
	    values.put(4, bytesSent);
	    values.put(5, requestCount);
	    values.put(6, errorCount);
	    values.put(7, processingTime);
	    values.put(8, requestThroughput);
	    
	    lasttime = curtime;
	    lastRequestCount = requestCount;
	    
//	    for (Entry<Integer,Object> entry : values.entrySet()){
//			System.out.println(entry.getKey()+" "+entry.getValue());
//		}
	    
		return new ProbeMetric(values);
	}
	
	private HashMap<String,Long> calcValues(){
		HashMap<String,Long> statMap = new HashMap<String,Long>();
		try{
			URL obj = new URL(GlobalRequestProcessor_URL);
			HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
			conn.setRequestMethod("GET");		
			conn.setRequestProperty("Authorization", auth_header);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			if(conn.getResponseCode() == 200){
				while ((line = in.readLine()) != null){
					if(line.contains("bytesSent")){
						statMap.put("bytesSent", Long.parseLong(line.split(": ")[1]));
						continue;
					}
					if(line.contains("bytesReceived")){
						statMap.put("bytesReceived", Long.parseLong(line.split(": ")[1]));
						continue;
					}
					if(line.contains("errorCount")){
						statMap.put("errorCount", Long.parseLong(line.split(": ")[1]));
						continue;
					}
					if(line.contains("processingTime")){
						statMap.put("processingTime", Long.parseLong(line.split(": ")[1]));
						continue;
					}
					if(line.contains("requestCount")){
						statMap.put("requestCount", Long.parseLong(line.split(": ")[1]));
						continue;
					}
				}
			}
			in.close();		
			
			obj = new URL(ThreadPool_URL);
			conn = (HttpURLConnection) obj.openConnection();
			conn.setRequestMethod("GET");		
			conn.setRequestProperty("Authorization", auth_header);
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			if(conn.getResponseCode() == 200){
				while ((line = in.readLine()) != null){
					if(line.contains("maxThreads")){
						statMap.put("maxThreads", Long.parseLong(line.split(": ")[1]));
						continue;
					}
					if(line.contains("currentThreadCount")){
						statMap.put("currentThreadCount", Long.parseLong(line.split(": ")[1]));
						continue;
					}
					if(line.contains("currentThreadsBusy")){
						statMap.put("currentThreadsBusy", Long.parseLong(line.split(": ")[1]));
						continue;
					}
				}
			}
			in.close();
		}
		catch(Exception e){
			this.writeToProbeLog(Level.WARNING, e);
			return null;
		}
		return statMap;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TomcatProbe probe = new TomcatProbe();
		probe.activate();
	}
}
