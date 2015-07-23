import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import eu.celarcloud.jcatascopia.probepack.Probe;
import eu.celarcloud.jcatascopia.probepack.ProbeMetric;
import eu.celarcloud.jcatascopia.probepack.ProbePropertyType;


public class ScanProbe extends Probe{
	
	private static int DEFAULT_SAMPLING_PERIOD = 10;
	private static String DEFAULT_PROBE_NAME = "ScanProbe";
	private static String SCAN_HOST = "localhost";
	private static long SCAN_PORT = 8080;

	private List<String> classes;

	public ScanProbe(String name, int freq) {
		super(name, freq);

		try {

			InputStream is = getStream("getclasses");
			JSONTokener tok = new JSONTokener(is);
			JSONArray jsclasses = new JSONArray(tok);

			classes = new ArrayList<String>();

			for(int i = 0, ilim = jsclasses.length(); i != ilim; ++i)
				classes.add(jsclasses.getString(i));

			is.close();

		}
		catch(Exception e) {

			System.err.println("Exception " + e.toString() + " creating SCAN probe");
			e.printStackTrace(System.err);
			return;

		}
			
		this.addProbeProperty(0, "queueLength",ProbePropertyType.LONG,"", "queue length");
		this.addProbeProperty(1, "avgCpuUsage", ProbePropertyType.DOUBLE, "", "average task CPU usage");
		this.addProbeProperty(2, "avgMemoryUsage", ProbePropertyType.DOUBLE, "", "average task memory usage");
		this.addProbeProperty(3, "workerUtilisation", ProbePropertyType.DOUBLE, "", "worker pool utilisation (1 = all active, 0 = all idle)");
		this.addProbeProperty(4, "rewardLostToQueueing", ProbePropertyType.DOUBLE, "", "reward lost due to tasks queueing");
		this.addProbeProperty(5, "rewardLostToSmallWorkers", ProbePropertyType.DOUBLE, "", "reward lost due to workers unable to offer sufficient local parallelism");
		this.addProbeProperty(6, "totalReward", ProbePropertyType.DOUBLE, "", "total reward gained so far");
		
		int idx = 6;

		for(String c : classes) {

			this.addProbeProperty(idx + 1, c + "_workPerHour",ProbePropertyType.DOUBLE,"", c + " work units per hour");
			idx++;

		}
	}
	
	public ScanProbe(){
		this(DEFAULT_PROBE_NAME, DEFAULT_SAMPLING_PERIOD);
	}

	private InputStream getStream(String relurl) throws MalformedURLException, IOException {

		String url = String.format("http://%s:%d/%s", SCAN_HOST, SCAN_PORT, relurl);
		return new URL(url).openStream();

	}

	private String getString(String relurl) throws MalformedURLException, IOException {

		InputStream is = getStream(relurl);
		InputStreamReader isr = new InputStreamReader(is);
		StringWriter sw = new StringWriter();

		char[] buf = new char[4096];
		
		int lastRead;

		while((lastRead = isr.read(buf, 0, 4096)) != -1)
			sw.write(buf, 0, lastRead);

		isr.close();
		return sw.toString();

	}

	private long getQueueLength() throws MalformedURLException, IOException {

		InputStream is = getStream("lsprocs");
		JSONTokener tok = new JSONTokener(is);
		JSONObject a = new JSONObject(tok);
		long ret = 0;

		for(Iterator<String> keys = a.keys(); keys.hasNext();) {

		    JSONObject val = a.getJSONObject(keys.next());
		    if(val.isNull("worker"))
			++ret;

		}

		is.close();
		return ret;

	}

	private double getWorkerUtilisation() throws MalformedURLException, IOException {

		InputStream is = getStream("lsworkers");
		JSONTokener tok = new JSONTokener(is);
		JSONObject a = new JSONObject(tok);
		long totalWorkers = a.length();
		long busyWorkers = 0;

		for(Iterator<String> keys = a.keys(); keys.hasNext();) {

			JSONObject val = a.getJSONObject(keys.next());
			if(val.has("busy") && val.getBoolean("busy"))
				++busyWorkers;

		}

		if(totalWorkers == 0)
			return 0;
		else
			return ((double)busyWorkers) / totalWorkers;

	}

	private double getWorkPerHour(String c) throws MalformedURLException, IOException {

		String wph = getString("getwph?classname=" + c);
		return Double.parseDouble(wph);		

	}

	private double getRewardLossToQueueing() throws MalformedURLException, IOException {

		String v = getString("getqueuerewardloss");
		return Double.parseDouble(v);		

	}	

	private double getRewardLossToSmallWorkers() throws MalformedURLException, IOException {

		String v = getString("getscalerewardloss");
		return Double.parseDouble(v);		

	}	

	private double getTotalReward() throws MalformedURLException, IOException {
		
		String v = getString("gettotalreward");
		return Double.parseDouble(v);		

	}

	public ProbeMetric collectOrThrow() throws Exception {

		HashMap<Integer,Object> values = new HashMap<Integer,Object>();
		
		long qlen = getQueueLength();
		double ut = getWorkerUtilisation();
		double queueLoss = getRewardLossToQueueing();
		double scaleLoss = getRewardLossToSmallWorkers();
		double totalReward = getTotalReward();

		values.put(0, qlen);
		values.put(1, (double)0);
		values.put(2, (double)0);
		values.put(3, ut);
		values.put(4, queueLoss);
		values.put(5, scaleLoss);
		values.put(6, totalReward);

		int offset = 7;
			
		for(String c : classes) {
	
			double wph = getWorkPerHour(c);
			values.put(offset, wph);
			offset++;

		}
		
//		for(Entry<Integer,Object> e : values.entrySet())
//			System.out.println(e.getKey() + ": " + e.getValue());
//		
//		for(Entry<Integer, ProbeProperty> e : this.getProbeProperties().entrySet())
//			System.out.println(e.getKey() + ": " + e.getValue().getPropertyName());
		
		return new ProbeMetric(values);

	}
    
	@Override
	public ProbeMetric collect() {

		try {

			return collectOrThrow();

		}
		catch(Exception e) {

			System.err.printf("Exception collection SCAN probe data: %s\n", e.toString());
			e.printStackTrace(System.err);
			return new ProbeMetric(new HashMap<Integer, Object>());

		}

	}

	@Override
	public String getDescription() {
		return "Queue length and work units/h for SCAN tasks";
	}
	
	public static void main(String[] args) {
		ScanProbe p = new ScanProbe();
		p.activate();
	}
}