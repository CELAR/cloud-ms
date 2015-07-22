import java.util.HashMap;
import java.util.Random;

import eu.celarcloud.jcatascopia.probepack.Probe;
import eu.celarcloud.jcatascopia.probepack.ProbeMetric;
import eu.celarcloud.jcatascopia.probepack.ProbePropertyType;

public class ExampleProbe extends Probe {
	
	private static int DEFAULT_SAMPLING_PERIOD = 10;
	private static String DEFAULT_PROBE_NAME = "ExampleProbe";

	public ExampleProbe(String name, int freq) {
		super(name, freq);
		/*
		 * define metrics that will be collected as ProbeProperties.
		 * 
		 * addProbeProperty (int id, String name, ProbePropertyType type,String units, String description)
		 * 
		 * e.g. addProbeProperty(0,"cpuUsage",ProbePropertyType.DOUBLE,"%","Current system cpu usage");
		 */
		
		this.addProbeProperty(0,"curTimestamp",ProbePropertyType.LONG,"","Current Timestamp");
		this.addProbeProperty(1,"myRandomMetric",ProbePropertyType.DOUBLE,"%","Random Values");
	}
	
	public ExampleProbe(){
		this(DEFAULT_PROBE_NAME, DEFAULT_SAMPLING_PERIOD);
	}

	@Override
	public ProbeMetric collect() {
		HashMap<Integer,Object> values = new HashMap<Integer,Object>();
		/*
		 * add to HashMap the values for each defined metric
		 * 
		 * e.g. values.put(0,71.32)
		 */
		
		long t = System.currentTimeMillis();
		double d = (new Random()).nextDouble()*100;
		
		values.put(0, t);
		values.put(1, d);
				
		return new ProbeMetric(values);
	}

	@Override
	public String getDescription() {
		return "An example or better a template of a JCatascopia Probe";
	}
	
	public static void main(String[] args) {
		ExampleProbe p = new ExampleProbe();
		p.activate();
	}
}