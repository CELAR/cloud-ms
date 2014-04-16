/*******************************************************************************
 * Copyright 2014, Laboratory of Internet Computing (LInC), Department of Computer Science, University of Cyprus
 * 
 * For any information relevant to JCatascopia Monitoring System,
 * please contact Demetris Trihinas, trihinas{at}cs.ucy.ac.cy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.celarcloud.jcatascopia.probepack;

import java.util.Date;
import java.util.HashMap;

import eu.celarcloud.jcatascopia.exceptions.CatascopiaException;

/**
 * 
 * @author Demetris Trihinas
 * 
 * An object of the ProbeMetric class represents a collected monitoring metric group of properties
 *
 */
public class ProbeMetric{
	/**
	 * timestamp of collected metric in ms.  
	 * Time is expressed as UNIX time. ms from UNIX epoch
	 */
	private long timestamp;
	/**
	 * hashmap to store collected values
	 */
	private HashMap<Integer,Object> values;
	/**
	 * probe ID that collected this metric
	 */
	private String probeID;
	
	public ProbeMetric(){
		this(new HashMap<Integer,Object>(), System.currentTimeMillis());
	}
	
	public ProbeMetric(HashMap<Integer,Object> values){
		this(values,System.currentTimeMillis());
	}
	/**
	 * 
	 * @param values - hashmap with key the propertyID of the metric
	 * @param timestamp - time values where collected in ms
	 */
	public ProbeMetric(HashMap<Integer,Object> values, long timestamp){
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
	
	public void removeMetricValue(int propID){
		this.values.remove(propID);
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
	public long getMetricTimestamp(){
		return this.timestamp;
	}
	/**
	 * method that sets the timestamp of the collected metric
	 * @return
	 */
	public void setMetricTimestamp(long t){
		this.timestamp = t;
	}
	
	public String getReadableTimestamp(){
		return new Date(this.timestamp).toString();
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
	
	public void removeAll(){
		this.values.clear();
	}
	
	public boolean isEmpty(){
		return this.values.isEmpty();
	}
}
