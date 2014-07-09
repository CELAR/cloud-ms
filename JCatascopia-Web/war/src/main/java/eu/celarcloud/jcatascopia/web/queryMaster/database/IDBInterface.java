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
package eu.celarcloud.jcatascopia.web.queryMaster.database;

import java.util.ArrayList;

import eu.celarcloud.jcatascopia.web.queryMaster.beans.AgentObj;
import eu.celarcloud.jcatascopia.web.queryMaster.beans.MetricObj;
import eu.celarcloud.jcatascopia.web.queryMaster.beans.SubscriptionObj;

public interface IDBInterface{
	
	public void dbConnect();
	public void dbClose();
	
	public ArrayList<AgentObj> getAgents(String status);
	public ArrayList<MetricObj> getAgentAvailableMetrics(String agentID);
	public ArrayList<MetricObj> getMetricValues(String[] registeredMetrics);
	public ArrayList<MetricObj> getMetricValuesByTime(String metricID, long interval);
	public ArrayList<MetricObj> getMetricValuesByTime(String metricID, long start, long end);
	
	public ArrayList<MetricObj> getAvailableMetricsForSubs();
	public ArrayList<SubscriptionObj> getSubscriptions();
	public SubscriptionObj getSubMeta(String subID);
	public ArrayList<AgentObj> getAgentsForSub(String subID);
	public boolean createSubscription(String ip, String port, String json);
	public boolean removeSubscription(String ip, String port, String json);
	public boolean addAgent(String ip, String port, String json);
	public boolean removeAgent(String ip, String port, String json);
	
	public ArrayList<MetricObj> getAvailableMetricsForAllAgents();
	
	public ArrayList<AgentObj> getAgentsWithTimestamps(String status);

}
