import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.IOException;

import eu.celarcloud.jcatascopia.probepack.Probe;
import eu.celarcloud.jcatascopia.probepack.ProbeMetric;
import eu.celarcloud.jcatascopia.probepack.ProbePropertyType;

public class ScanWorkerProbe extends Probe{
	
	private static int DEFAULT_SAMPLING_PERIOD = 10;
	private static String DEFAULT_PROBE_NAME = "ScanWorkerProbe";
	private static long SCAN_PORT = 8080;

    private String scanHost;
	private String scanClass;
	private int workerId;
	private boolean verbose;

	private double doubleFromFile(String fname, double def) {
		
		try {
			FileReader fr = new FileReader(fname);
			StringWriter sw = new StringWriter();

			char[] buf = new char[4096];
		
			int lastRead;

			while((lastRead = fr.read(buf, 0, 4096)) != -1)
				sw.write(buf, 0, lastRead);

			fr.close();
			sw.close();
			return Double.parseDouble(sw.toString());
		}
		catch(IOException e) {
			return def;
		}
		
	}

	private double getDiskFreeProp() throws IOException {
	
		File fs = new File("/mnt/scanfs");
		return ((double)fs.getFreeSpace()) / fs.getTotalSpace();
	    
	}	

	public void addProps() {

		this.addProbeProperty(0, "idleCoresProp", ProbePropertyType.DOUBLE,"", "Proportion of cores idle");
		this.addProbeProperty(1, "idleMemProp", ProbePropertyType.DOUBLE,"", "Proportion of memory idle");
		this.addProbeProperty(2, "freeDiskProp", ProbePropertyType.DOUBLE, "", "Proportion of scratch disk free");

	}

	public ScanWorkerProbe(String name, int freq) throws Exception {
		super(name, freq);
		addProps();
	}

	public ScanWorkerProbe(boolean verbose) {
		super(DEFAULT_PROBE_NAME, DEFAULT_SAMPLING_PERIOD);
		this.verbose = verbose;
		addProps();
	}
	
	public ScanWorkerProbe() throws Exception {
		this(DEFAULT_PROBE_NAME, DEFAULT_SAMPLING_PERIOD);
	}

	public ProbeMetric collectOrThrow() throws Exception {

		HashMap<Integer,Object> values = new HashMap<Integer,Object>();
		values.put(0, doubleFromFile("/tmp/scan_idle_cores", 0));
		values.put(1, doubleFromFile("/tmp/scan_idle_mem", 0));
		values.put(2, getDiskFreeProp());

		if(verbose) {
			Iterator it = values.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry)it.next();
				System.out.println(pair.getKey() + " = " + pair.getValue());
			}
		}

		return new ProbeMetric(values);

	}
    
	@Override
	public ProbeMetric collect() {

		try {
			return collectOrThrow();
		}
		catch(Exception e) {
			System.err.printf("Exception collection SCAN probe data: %s\n", e.toString());
			return new ProbeMetric(new HashMap<Integer, Object>());
		}

	}

	@Override
	public String getDescription() {
		return "SCAN worker status";
	}
	
	public static void main(String[] args) {
		try {
			ScanWorkerProbe p = new ScanWorkerProbe(true);
			p.activate();
		}
		catch(Exception e) {
			System.err.println("Probe failed: " + e);
			e.printStackTrace(System.err);
		}
	}
}