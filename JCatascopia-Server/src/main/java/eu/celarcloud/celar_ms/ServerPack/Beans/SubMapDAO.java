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
package eu.celarcloud.celar_ms.ServerPack.Beans;

import java.util.concurrent.ConcurrentHashMap;

public class SubMapDAO {
	
	public static void createSubcription(ConcurrentHashMap<String,SubObj> subMap, 
										 ConcurrentHashMap<String,MetricObj> metricMap, SubObj sub, MetricObj metric){
		try{
			if (subMap.putIfAbsent(sub.getSubID(), sub) != null)
				//if a subscription with the same key exists then overwrite it
				SubMapDAO.removeSubscription(subMap, metricMap, sub.getSubID());
			metricMap.putIfAbsent(metric.getMetricID(), metric);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static void removeSubscription(ConcurrentHashMap<String,SubObj> subMap, 
										  ConcurrentHashMap<String,MetricObj> metricMap, String subID){
		try{	
			SubObj sub = subMap.get(subID);
			if (sub == null)
				return;
			metricMap.remove(sub.getMetricID());
			subMap.remove(subID);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static void addAgent(ConcurrentHashMap<String,SubObj> subMap, 
			                    ConcurrentHashMap<String,AgentObj> agentMap, String subID, String agentID){
		try{
			SubObj sub = subMap.get(subID);
			if (sub == null || !agentMap.containsKey(agentID))
				return;
			sub.addAgentToList(agentID);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static void removeAgent(ConcurrentHashMap<String,SubObj> subMap, 
            					   ConcurrentHashMap<String,AgentObj> agentMap, String subID, String agentID){
		try{
			SubObj sub = subMap.get(subID);
			if (sub == null || !agentMap.containsKey(agentID))
			return;
			sub.removeAgentFromList(agentID);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}
