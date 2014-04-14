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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SubObj {
	public enum GroupingFunction{SUM,AVG,MIN,MAX,CUSTOM};

	private String subID;
	private String subName;
	private String originMetric;
	private List<String> agentList;
	private GroupingFunction func;
	private String metricID;
	private int period;
	
	public SubObj(String subID, String subName, String metricID, String originMetric, ArrayList<String> agentlist, 
			      GroupingFunction func, int period){
		this.subID = subID;
		this.subName = subName;
		this.metricID = metricID;
		this.originMetric = originMetric;
		this.agentList = Collections.synchronizedList(new ArrayList<String>());
		this.agentList.addAll(agentlist);
		this.func = func;
		this.period = period;
		
	}
	
	public String getSubID(){
		return this.subID;
	}
	
	public void setSubID(String id){
		this.subID = id;
	}
		
	public String getOriginMetric(){
		return this.originMetric;
	}
	
	public String getSubName(){
		return this.subName;
	}
	
	public GroupingFunction getGroupingFunc(){
		return this.func;
	}
	
	public void setGroupingFunc(GroupingFunction func){
		this.func = func;
	}
	
	public String getMetricID(){
		return this.metricID;
	}
	
	public void setMetricID(String id){
		this.metricID = id;
	}
	
	public int getPeriod(){
		return this.period;
	}
	
	public void setPeriod(int period){
		this.period = period;
	}
			
	public List<String> getAgentList(){
		return this.agentList;
	}
	
	public void addAgentToList(String agentID){
		synchronized(agentList){
			this.agentList.add(agentID);
		}
	}
	
	public void removeAgentFromList(String agentID){
		synchronized(agentList){
			for(int i=0;i<agentList.size();i++)
				if (agentList.get(i).equals(agentID))
					agentList.remove(i);
		}
	}
	
	public void clearAgentList(){
		synchronized(agentList){
			agentList.clear();
		}
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		try{
			synchronized(agentList){
				for(String a : agentList)
					sb.append(a+", ");
			}
		}
		catch(Exception e){}
		return "Subscription>> subID: "+this.subID+" subName: "+this.subName+" metricID: "+this.metricID
			  +" originMetric: "+this.originMetric+" function: "+this.getGroupingFunc()+
			   " update_period: "+this.period+" agents: ["+sb.toString()+"]";
	}
}
