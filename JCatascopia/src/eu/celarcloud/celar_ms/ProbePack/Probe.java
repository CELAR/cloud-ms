package eu.celarcloud.celar_ms.ProbePack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ProbePack.Filters.Filter;

/** 
 * @author Demetris Trihinas
 * 
 * Abstract Probe Class. Probe Developers MUST Extend this class.
 * A probe extends Java Thread class.
 * Methods to be overriden are:
 *     collect - Method that collects a metric
 *     getDescription - Method that provides a description of the Probe
 * Optionally: 
 *     cleanUp - Method that cleans up any messes before the Probe is terminated
 *     checkReceivedMetric - Method that performs metric check(s) and/or validation
 *
 */
public abstract class Probe extends Thread implements IProbe{
    /**
     * give the probe an ID	
     */
	private String probeID;
	/**
	 * a human readable name set by Probe Developer to identify a Probe
	 */
	private String probeName;
	/**
	 * default collecting frequency 
	 */
	private int collectPeriod;
	/**
	 * frequency in ms
	 */
	private long collectPeriodMillis;
	/**
	 * probe status - ACTIVE, INACTIVE, DYING
	 */
	private ProbeStatus probeStatus;
	/**
	 * use flag to know if probe/thread already started once
	 */
	private boolean firstFlag;
	/**
	 * metrics are properties.
	 * i.e. MemoryProbe has properties: total,free,used,swap,etc.
	 */
	private HashMap<Integer,ProbeProperty> probeProperties;
	/**
	 * 	store last collected metric
	 */
	private ProbeMetric lastMetric;
	/**
	 * if queue attached, push metrics
	 */
	private LinkedBlockingQueue<String> metricQueue = null;
	/**
	 * Logger to handle logging
	 */
	private Logger myLogger;
	/**
	 * flag to check if logging available
	 */
	private boolean loggingFlag;
	
	private byte errorCountFlag;
	
	private boolean metricsPullFlag;
	
	private HashMap<Integer, Filter> filterMap;
	private boolean filterFlag;
	
	/**
	 * Probe Constructor
	 * @param name - Probe name
	 * @param freq - Probe collecting frequency (in seconds)
	 */
	public Probe(String name, int freq){
		super(name);
		this.probeID = UUID.randomUUID().toString().replace("-", "");
		this.probeName = name;
		this.collectPeriod = freq;
		this.collectPeriodMillis = freq * 1000;
		this.probeStatus = ProbeStatus.INACTIVE;
		this.firstFlag = true;
		this.probeProperties = new HashMap<Integer,ProbeProperty>();
		this.lastMetric = null;
		this.loggingFlag = false;
		this.errorCountFlag=0;
		this.metricsPullFlag = false;
		
		this.filterMap = new HashMap<Integer, Filter>();
		this.filterFlag = false;
	}
	
	/**
	 * MS Agent can attach a logger to the probe in order to log issues concerning this Probe
	 * @param logger
	 */
	public void attachLogger(Logger logger){
		try{
			this.myLogger = logger;
			this.loggingFlag = true;
			this.writeToProbeLog(Level.INFO, "Logging turned ON");
		}
		catch(Exception e){
			this.loggingFlag = false;
		}
	}
	
	/**
	 * method that returns Probe ID
	 */
	public String getProbeID(){
		return this.probeID;
	}
	/**
	 * method that returns Probe Name
	 */
	public String getProbeName(){
		return this.probeName;
	}
	/**
	 * method that sets a new Probe Name
	 */
	public void setProbeName(String name){
		this.probeName = name;
	}
	/**
	 * method that returns current Probe Collection Period (seconds)
	 */
	public int getCollectPeriod(){
		return this.collectPeriod;
	}
	/**
	 * method that sets Probe Collection Period (seconds)
	 */
	public void setCollectPeriod(int freq){
		this.collectPeriod = freq;
		this.collectPeriodMillis = freq * 1000;
	}
	/**
	 * method that returns Probe Status - INACTIVE, ACTIVE, DYING
	 */
	public ProbeStatus getProbeStatus(){
		return this.probeStatus;
	}
	/**
	 * method that returns the Probes Description provided by the Probe Developer
	 */
	public abstract String getDescription();
	/**
	 * method that logs messages to this probes log
	 */
	public void writeToProbeLog(Level level, Object msg){
		if(this.loggingFlag)
			this.myLogger.log(level, this.probeName+": "+msg);
	}
	/**
	 * method that adds/updates a property to the Probe Property hashmap
	 */
	public void addProbeProperty(int propID,String propName,ProbePropertyType propType,
			                     String propUnits, String desc){
		this.probeProperties.put(propID, new ProbeProperty(propID,propName,propType,propUnits,desc));
	}
	/**
	 * method that returns a hashmap of all Probes Properties
	 */
	public HashMap<Integer,ProbeProperty> getProbeProperties(){
		return this.probeProperties;
	}
	/**
	 * method that returns a list of all Probes Properties
	 */
	public ArrayList<ProbeProperty> getProbePropertiesAsList(){
		ArrayList<ProbeProperty> list = new ArrayList<ProbeProperty>();
	    for (Entry<Integer,ProbeProperty> entry : this.probeProperties.entrySet()) {
	    	ProbeProperty val = entry.getValue();
	    	list.add(val);
		 }
	    return list;
	}
	/**
	 * method that returns a Property by ID
	 */
	public ProbeProperty getProbePropertyByID(int propID) throws CatascopiaException{
		if (this.probeProperties.containsKey(propID))
			return this.probeProperties.get(propID);
		this.writeToProbeLog(Level.WARNING, this.probeName+": Get Probe Property Failed, property ID given does not exist: "+propID);
		throw new CatascopiaException("Get Probe Property Failed, property ID given does not exist: "+propID, 
				                       CatascopiaException.ExceptionType.KEY);
	}
	/**
	 * override Thread class start method to call activate if invoked 
	 */
	@Override
	public void start(){
		this.activate();
	}
	/**
	 * method to activate collecting mechanism of probe
	 */
	public synchronized void activate(){
		if (this.probeStatus == ProbeStatus.INACTIVE){
			if (firstFlag){
				super.start();
				firstFlag = false;
			}
			else this.notify();
			this.probeStatus = ProbeStatus.ACTIVE;	
			this.writeToProbeLog(Level.INFO, this.probeName+": Probe Activated");
		}	
	}
	/**
	 * method to deactivate collecting mechanism of probe
	 */
	public void deactivate(){
		this.probeStatus = ProbeStatus.INACTIVE;
		this.writeToProbeLog(Level.INFO, this.probeName+": Probe Deactivated");
	}
	/**
	 * method to terminate-kill probe
	 */
	public synchronized void terminate(){
		this.probeStatus = ProbeStatus.DYING;
		this.writeToProbeLog(Level.INFO, this.probeName+": Probe Terminated");
		this.notify();
	}
	
	@Override
	public void run() {
		try {
			ProbeMetric recvMetric;
			while(this.probeStatus != ProbeStatus.DYING){
				if(this.probeStatus == ProbeStatus.ACTIVE){
					//collecting enabled
					try {
						//collect metric
						recvMetric = this.collect();
						//check if valid
						this.checkReceivedMetric(recvMetric);
						//add probeID to metric - user doesn't have to do this
						recvMetric.setAssignedProbeID(this.probeID);
						this.lastMetric = recvMetric;
						
						if (this.filterFlag)
							this.checkFilters(recvMetric);
						
						String jsonMetric = this.metricToJSON(recvMetric);
//						System.out.println(jsonMetric);
						
						//add to metricQueue if attached to probe
						if(this.metricQueue != null)
							this.metricQueue.offer(jsonMetric,500,TimeUnit.MILLISECONDS);
						this.errorCountFlag = 0;
					}
					catch(CatascopiaException e){
						//TODO first error alert
						//after a number of errors report termination and terminate
						if (e.getExceptionType() == CatascopiaException.ExceptionType.QUEUE)
							this.writeToProbeLog(Level.SEVERE, "Failed to write to Metric Queue: " + e);
						else this.writeToProbeLog(Level.SEVERE,"Received Metric Failed: " + e);
						this.errorReport();
					}
					catch(Exception e){
						this.errorReport();
					}
					Thread.sleep(this.collectPeriodMillis);
				}
				else 
					synchronized(this){
						while(this.probeStatus == ProbeStatus.INACTIVE)
							this.wait();
					}
			}
		}
		catch (InterruptedException e){			
			this.writeToProbeLog(Level.SEVERE, e);
			this.errorReport();
		}
		catch (Exception e){
			this.writeToProbeLog(Level.SEVERE, e);
			this.errorReport();
		}
	}
	/**
	 * collect method is provided by Probe Developer
	 */
	public abstract ProbeMetric collect();
	/**
	 * optional method to clean up loose ends before probe terminates
	 */
	public void cleanUp(){}
	
	public String metricToJSON(ProbeMetric metric){
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"timestamp\":\""+metric.getMetricTimestamp()+"\",");
		sb.append("\"group\":\""+this.probeName+"\",");
		sb.append("\"metrics\":[");
		for (Entry<Integer,Object> entry : metric.getMetricValues().entrySet()){
			Integer key = entry.getKey();
			Object val = entry.getValue();
			sb.append("{\"name\":\""+this.probeProperties.get(key).getPropertyName()+"\",");
			sb.append("\"units\":\""+this.probeProperties.get(key).getPropertyUnits()+"\",");
			sb.append("\"type\":\""+this.probeProperties.get(key).getPropertyType()+"\",");
			sb.append("\"val\":\""+val.toString()+"\"},");
		}
		sb.replace(sb.length()-1, sb.length(), "");
		sb.append("]}");
		return sb.toString();
	}	
	
	/**
	 * method that returns a hashmap containing probe metadata
	 */
	public HashMap<String, String> getProbeMetadata(){
		HashMap<String,String> meta = new HashMap<String,String>();
		meta.put("probeID", this.probeID);
		meta.put("probeName", this.probeName);
		meta.put("collectFreq", Integer.toString(this.collectPeriod));
		meta.put("probeStatus", this.probeStatus.toString());
		meta.put("probeDesc", this.getDescription());
		return meta;
	}
	
	/**
	 * 
	 * @param metric
	 * @throws CatascopiaException
	 */
	public void checkReceivedMetric(ProbeMetric metric) throws CatascopiaException{
		if(!(ProbeMetric.class.isInstance(metric)))
			throw new CatascopiaException("Received Metric is not valid. All metrics must be of type ProbeMetric.",
					                       CatascopiaException.ExceptionType.TYPE);
		
		HashMap<Integer,Object> vals = metric.getMetricValues();
		if(vals.size() != this.probeProperties.size())
			throw new CatascopiaException("Number of values in Received Metric not equal to number of Probe Properties specified",
					                       CatascopiaException.ExceptionType.ATTRIBUTE);
			
		for (Entry<Integer,Object> entry :vals.entrySet()) {
			ProbePropertyType type = this.probeProperties.get(entry.getKey()).getPropertyType(); 
			if(!ProbePropertyType.isType(type,entry.getValue()))
				throw new CatascopiaException("Received Metric is not valid. Metric Property with ID: "
			                                  +entry.getKey()+" must be of type: "+type, CatascopiaException.ExceptionType.TYPE);  	   
		 }
	}
	
	public void turnFilteringOn(int propID, Filter f){
		if (this.probeProperties.containsKey(propID)){
			this.filterMap.put(propID, f);
			this.filterFlag = true;
		}
	}
	
	public void turnFilteringOff(){
		this.filterFlag = false;
	}
	
	
	private void checkFilters(ProbeMetric metric){
		for (Entry<Integer,Filter> entry :this.filterMap.entrySet()){
			try {
				if (entry.getValue().check((Double)metric.getMetricValueByID(entry.getKey())))
					metric.removeMetricValue(entry.getKey());
			} 
			catch (CatascopiaException e) {
				this.writeToProbeLog(Level.SEVERE, e);
				this.errorReport();
			}
			catch (Exception e){
				this.writeToProbeLog(Level.SEVERE, e);
				this.errorReport();
			}
		}
	}
	
	
	/**
	 * method that returns last collected metric
	 */
	public ProbeMetric getLastMetric(){
		return this.lastMetric;
	}
	/**
	 * method that sets last metric
	 */
	public void setLastMetric(ProbeMetric metric){
		this.lastMetric = metric;
	}
	/**
	 * method that returns last metric values
	 */
	public HashMap<Integer,Object> getLastMetricValues(){
		if(this.lastMetric != null)
			return this.lastMetric.getMetricValues();
		return null;
	}
	/**
	 * method that returns last metric timestamp
	 */
	public long getLastUpdateTime(){
		if(this.lastMetric != null)
			return this.lastMetric.getMetricTimestamp();
		return -1;
	}
	/**
	 * method that attaches queue to probe to push metrics to Agent
	 */
	public void attachQueue(LinkedBlockingQueue<String> queue){
		this.metricQueue = queue;
		this.writeToProbeLog(Level.INFO, this.probeName+": Metric Queue attached to Probe");
	}
	/**
	 * method to dettach queue from probe
	 */
	public void removeQueue(){
		this.metricQueue = null;
		this.writeToProbeLog(Level.INFO, this.probeName+": Metric Queue removed from Probe");
	}
	
	private synchronized void errorReport(){
		if ((++this.errorCountFlag)>10){
			this.probeStatus=IProbe.ProbeStatus.DYING;
			this.writeToProbeLog(Level.SEVERE, "Probe:"+this.probeName+" terminating due to many errors");
		}
	}
	
	public boolean metricsPullable(){
		return this.metricsPullFlag;
	}
	
	public void setPullableFlag(boolean flag){
		this.metricsPullFlag = flag;
	}
	
	public void pull(){
		if (this.metricQueue != null)
			try {
				this.metricQueue.offer(this.metricToJSON(this.collect()), 500, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				this.writeToProbeLog(Level.SEVERE, e);
			} catch (Exception e) {
				this.writeToProbeLog(Level.SEVERE, e);
			}
	}
}
