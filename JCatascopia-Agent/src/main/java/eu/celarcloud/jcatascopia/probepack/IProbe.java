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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.celarcloud.jcatascopia.exceptions.CatascopiaException;
import eu.celarcloud.jcatascopia.probepack.filters.Filter;


public interface IProbe {
	
	public enum ProbeStatus{INACTIVE,ACTIVE,DYING};
	
	public String getProbeID();
	
	public String getProbeName();
	
	public void setProbeName(String name);
	
	public int getCollectPeriod();
	
	public void setCollectPeriod(int freq);
	
	public ProbeStatus getProbeStatus();
	
	public void writeToProbeLog(Level level, Object msg);

	public void addProbeProperty(int propID,String propName,ProbePropertyType propType, String propUnits, String desc);
	
	public HashMap<Integer,ProbeProperty> getProbeProperties();
	
	public ArrayList<ProbeProperty> getProbePropertiesAsList();
	
	public ProbeProperty getProbePropertyByID(int propID)  throws CatascopiaException;
	
	public void activate();
	
	public void deactivate();
	
	public void terminate();
	
	public HashMap<String, String> getProbeMetadata();
	
	public ProbeMetric getLastMetric();
	
	public void setLastMetric(ProbeMetric metric);
	
	public HashMap<Integer,Object> getLastMetricValues();
	
	public long getLastUpdateTime();
	
	
	//user must override these
	public abstract String getDescription();
	
	public abstract ProbeMetric collect();
	//
	
	//user optionally can override 
	public void cleanUp();
	public void checkReceivedMetric(ProbeMetric metric) throws CatascopiaException;
	//
	
	public void attachLogger(Logger logger);

	public void attachQueue(LinkedBlockingQueue<String> queue);
	
	public void removeQueue();
	
	public boolean metricsPullable();
	
	public void setPullableFlag(boolean flag);
	
	public void pull();
	
	public void turnFilteringOn(int propID, Filter f);
	public void turnFilteringOn(int propID, Filter f, boolean globalFilterFlag);
	public void turnFilteringOff();
}
