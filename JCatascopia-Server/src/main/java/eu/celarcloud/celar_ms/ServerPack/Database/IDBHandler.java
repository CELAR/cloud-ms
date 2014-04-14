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
package eu.celarcloud.celar_ms.ServerPack.Database;

import java.util.ArrayList;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.SubObj;

public interface IDBHandler{
	
	public void dbConnect();
	public void dbClose();
	public void dbInit(boolean drop_tables) throws CatascopiaException;
	
	public void createAgent(AgentObj agent);
	public void updateAgent(String agentID, String status);
	public void deleteAgent(String agentID);
	
	public void createMetric(MetricObj metric);
	public void deleteMetric(String agentID, String metricID);
	public void insertMetricValue(MetricObj metric);
	public void insertBatchMetricValues(ArrayList<MetricObj> metriclist);	
	
	public void createSubscription(SubObj sub, MetricObj metric);
	public void deleteSubscription(String subID);
	public void addAgentToSub(String subID, String agentID);
	public void removeAgentFromSub(String subID, String agentID);
	
	public void doQuery(String cql, boolean print);//only for testing and debugging
}
