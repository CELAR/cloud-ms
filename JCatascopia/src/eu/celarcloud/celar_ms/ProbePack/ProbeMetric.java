package eu.celarcloud.celar_ms.ProbePack;

import java.util.HashMap;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.utils.CatascopiaTimestamp;

/**
 * 
 * @author Demetris Trihinas
 * 
 * An object of the ProbeMetric class represents a collected monitoring metric group of properties
 *
 */
public class ProbeMetric {
	/**
	 * timestamp of collected metric
	 */
	private CatascopiaTimestamp timestamp;
	/**
	 * hashmap to store collected values
	 */
	private HashMap<Integer,Object> values;
	/**
	 * probe ID that collected this metric
	 */
	private String probeID;
	
	public ProbeMetric(){
		this(new HashMap<Integer,Object>(), new CatascopiaTimestamp());
	}
	
	public ProbeMetric(HashMap<Integer,Object> values){
		this(values,new CatascopiaTimestamp());
	}
	/**
	 * 
	 * @param values - hashmap with key the propertyID of the metric
	 * @param timestamp - time values where collected
	 */
	public ProbeMetric(HashMap<Integer,Object> values, CatascopiaTimestamp timestamp){
		this.timestamp = timestamp;
		this.values = values;
	}
	/**
	 * method that adds a metric to the value hashmap
	 * @param propID - propertyID
	 * @param value - value to be added
	 */
	public void addMetricValue(int propID, Object value){
		this.values.put(propID, value);
	}
	/**
	 * method that returns a value of a specified by the ID metric
	 * @param propID
	 * @return
	 * @throws CatascopiaException
	 */
	public Object getMetricValueByID(int propID) throws CatascopiaException{
		if (this.values.containsKey(propID))
			return this.values.get(propID);
		throw new CatascopiaException("Get Metric Value Failed, property ID given does not exist: "+propID, 
				                       CatascopiaException.ExceptionType.KEY);
	}
	/**
	 * method that returns the hashmap with all the values
	 * @return
	 */
	public HashMap<Integer,Object> getMetricValues(){
		return this.values;
	}
	/**
	 * method that returns the timestamp of the collected metric
	 * @return
	 */
	public CatascopiaTimestamp getMetricTimestamp(){
		return this.timestamp;
	}
	/**
	 * method that sets the timestamp of the collected metric
	 * @return
	 */
	public void setMetricTimestamp(CatascopiaTimestamp t){
		this.timestamp = t;
	}
	/**
	 * method that returns the probeID of the metric
	 * @return
	 */
	public String getAssignedProbeID(){
		return this.probeID;
	}
	/**
	 * method that assigns a probeID to the metric
	 * @return
	 */
	public void setAssignedProbeID(String probeid){
		this.probeID = probeid;
	}
}
