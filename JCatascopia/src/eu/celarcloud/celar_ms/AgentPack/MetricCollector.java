package eu.celarcloud.celar_ms.AgentPack;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.celarcloud.celar_ms.ProbePack.IProbe;
import eu.celarcloud.celar_ms.ProbePack.ProbeMetric;
import eu.celarcloud.celar_ms.ProbePack.ProbeProperty;

public class MetricCollector extends Thread{
	
	public enum CollectorStatus{INACTIVE,ACTIVE,DYING};

	private LinkedBlockingQueue<ProbeMetric> metricQueue;
	private HashMap<String,IProbe> probeMap;
	private CollectorStatus collectorStatus;
	private boolean printing;
	
	private IAgent agent;
	private AgentAggregator aggregator;
	
	public MetricCollector(LinkedBlockingQueue<ProbeMetric> metricQueue, IAgent agent,HashMap<String,IProbe> probes, AgentAggregator aggregator){	
		super("MetricCollector-Thread");
		this.metricQueue = metricQueue;
		this.probeMap = probes;
		this.collectorStatus = CollectorStatus.ACTIVE;
		this.printing = true;
		
		this.agent = agent;
		this.aggregator = aggregator;
	}
	
	public synchronized void terminate(){
		this.collectorStatus = CollectorStatus.DYING;
	}
	
	@Override
	public void run(){
		ProbeMetric metric;
		while(this.collectorStatus != CollectorStatus.DYING){
			try {
				metric = this.metricQueue.poll(500, TimeUnit.MILLISECONDS);
				if (metric != null)
					this.processMetric(metric);
				else
					Thread.sleep(2000);
			}
			catch (InterruptedException e) {
				//TODO -logging - if reading from queue fails we log error and continue;
				continue;
			}
		}
	}
		
	private void processMetric(ProbeMetric metric){
		String myProbeID;
		IProbe probe; 
		try{
			myProbeID = metric.getAssignedProbeID();
			probe = this.probeMap.get(myProbeID);
		}
		catch (NullPointerException e){
			//it is common for a metric to be in queue but probe was just removed from agents probe map	
			//log read queue failure
			return;
		}	
		
		JSONObject msg = new JSONObject();
		JSONArray values = new JSONArray();
		try {
			msg.put("timestamp", metric.getMetricTimestamp().getTimestamp());
			msg.put("host", this.agent.getAgentIP());
			msg.put("group", probe.getProbeName());
			HashMap<Integer,ProbeProperty> properties = probe.getProbeProperties();
			for (Entry<Integer,Object> entry : metric.getMetricValues().entrySet()){
				Integer key = entry.getKey();
				Object val = entry.getValue();
				JSONObject obj = new JSONObject();
				obj.put("name", properties.get(key).getPropertyName());
				obj.put("units", properties.get(key).getPropertyUnits());
				obj.put("type", properties.get(key).getPropertyType());
				obj.put("val", val.toString());
				values.put(obj);
			}	
			msg.put("metrics", values);
		} 
		catch (JSONException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//add to distributors queue
		String message = msg.toString();
		this.aggregator.process(message);
		
		//if printing turned on print to screen
//		if(this.printing)
//			System.out.println(message);
	}
	
	public void setConsoleWriting(boolean status){
		this.printing = status;
	}
}