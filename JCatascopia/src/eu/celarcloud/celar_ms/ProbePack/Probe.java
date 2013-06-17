package eu.celarcloud.celar_ms.ProbePack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.celarcloud.celar_ms.ProbePack.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.utils.CatascopiaLogging;
import eu.celarcloud.celar_ms.utils.Timestamp;

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
	private int collectFreq;
	/**
	 * frequency in ms
	 */
	private long collectFreqMillis;
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
	private LinkedBlockingQueue<ProbeMetric> metricQueue = null;
	/**
	 * Logger to handle logging
	 */
	private Logger myLogger;
	/**
	 * flag to check if logging available
	 */
	private boolean loggingFlag;
	
	/**
	 * Probe Constructor
	 * @param name - Probe name
	 * @param freq - Probe collecting frequency (in seconds)
	 */
	public Probe(String name, int freq){
		super(name);
		this.probeID = UUID.randomUUID().toString();
		this.probeName = name;
		this.collectFreq = freq;
		this.collectFreqMillis = freq * 1000;
		//probe status initialized to INACTIVE
		this.probeStatus = ProbeStatus.INACTIVE;
		this.firstFlag = true;
		this.probeProperties = new HashMap<Integer,ProbeProperty>();
		this.lastMetric = null;
		//this.initLogging(); setting flag to pause for testing reasons
		this.loggingFlag = false;
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
	 * method that returns current Probe Collection Frequency (seconds)
	 */
	public int getCollectFreq(){
		return this.collectFreq;
	}
	/**
	 * method that sets Probe Collection Frequency (seconds)
	 */
	public void setCollectFreq(int freq){
		this.collectFreq = freq;
		this.collectFreqMillis = freq * 1000;
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
	//initialize logging
	private void initLogging(){
		try{
			this.myLogger = CatascopiaLogging.getLogger(this.probeName);
			this.myLogger.info(this.probeName+": Created and Initialized");
			this.loggingFlag = true;
		}
		catch (Exception e){
			//Unable to log events
			this.loggingFlag = false;
		} 
	}
	/**
	 * method that logs messages to this probes log
	 */
	public void writeToProbeLog(Level level, String msg){
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
						//add to metricQueue if attached to probe
						if(this.metricQueue != null)
							this.metricQueue.offer(recvMetric,1,TimeUnit.SECONDS);
					}
					catch (CatascopiaException e){
						//TODO first error alert
						//after a number of errors report termination and terminate
						if (e.getExceptionType() == CatascopiaException.ExceptionType.QUEUE)
							this.writeToProbeLog(Level.SEVERE, "Failed to write to Metric Queue: " + e);
						else this.writeToProbeLog(Level.SEVERE,"Received Metric Failed: " + e);
					}
					Thread.sleep(this.collectFreqMillis);
				}
				else 
					synchronized(this){
						while(this.probeStatus == ProbeStatus.INACTIVE)
							this.wait();
					}
			}
		}
		catch (InterruptedException e){
			e.printStackTrace();
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
	/**
	 * method that returns a hashmap containing probe metadata
	 */
	public HashMap<String, String> getProbeMetadata(){
		HashMap<String,String> meta = new HashMap<String,String>();
		meta.put("probeID", this.probeID);
		meta.put("probeName", this.probeName);
		meta.put("collectFreq", Integer.toString(this.collectFreq));
		meta.put("probeStatus", this.probeStatus.toString());
		meta.put("probeDesc", this.getDescription());
		return meta;
	}
	//method that checks if a metric is valid
	private void checkReceivedMetric(ProbeMetric metric) throws CatascopiaException{
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
	public Timestamp getLastUpdateTime(){
		if(this.lastMetric != null)
			return this.lastMetric.getMetricTimestamp();
		return null;
	}
	/**
	 * method that attacheds queue to probe to push metrics to Agent
	 */
	public void attachQueue(LinkedBlockingQueue<ProbeMetric> queue){
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
}
