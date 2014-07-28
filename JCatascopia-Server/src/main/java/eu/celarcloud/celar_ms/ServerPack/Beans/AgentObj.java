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

public class AgentObj{
	
	public enum AgentStatus{UP,DOWN,TERMINATED};
	
	private String agentID;
	private String agentIP;
	private String agentName;
	private String agentTags;
	private AgentStatus agentStatus;
	private byte attempts;
	private List<String> metricList;
	
	public AgentObj(String agentID,String agentIP,String agentName,String agentTags){
		this.agentID = agentID;
		this.agentIP = agentIP;
		this.agentStatus = AgentStatus.UP;
		this.attempts = 0;
		this.metricList = Collections.synchronizedList(new ArrayList<String>());
		this.agentName = (agentName != null && !agentName.equals("")) ? agentName : this.agentIP;
		this.agentTags = (agentTags != null && !agentTags.equals("")) ? agentTags : null;
	}
	
	public String getAgentID(){
		return this.agentID;
	}
	
	public String getAgentIP(){
		return this.agentIP;
	}
	
	public void setAgentIP(String ip){
		this.agentIP = ip;
	}
	
	public String getAgentName(){
		return this.agentName;
	}
	
	public String getAgentTags(){
		return this.agentTags;
	}
	
	public void setAgentName(String n){
		this.agentName = n;
	}
	
	public void setAgentTags(String t){
		this.agentTags = t;
	}
		
	public boolean isRunning(){
		return (this.agentStatus == AgentStatus.UP) ? true : false;
	}
	
	public AgentStatus getStatus(){
		return this.agentStatus;
	}
	
	public void setStatus(AgentStatus status){
		this.agentStatus = status;
	}
	
	public void incrementAttempts(){
		this.attempts++;
	}
	
	public void clearAttempts(){
		this.attempts = 0;
	}
	
	public byte getAttempts(){
		return this.attempts;
	}
	
	public List<String> getMetricList(){
		return this.metricList;
	}
	
	public void addMetricToList(String metric){
		synchronized(metricList){
			this.metricList.add(metric);
		}
	}
	
	public void removeMetricFromList(String metric){
		synchronized(metricList){
			for(int i=0;i<metricList.size();i++)
				if (metricList.get(i).equals(metric))
					metricList.remove(i);
		}
	}
	
	public void clearMetricList(){
		synchronized(metricList){
			metricList.clear();
		}
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		try{
			synchronized(metricList){
				for(String m : metricList)
					sb.append(m.split(":")[1]+", ");
			}
		}
		catch(Exception e){}
		return "Agent>> AgentID: "+this.agentID+" AgentIP: "+this.agentIP+" status: "+this.agentStatus+ " AgentName: "+this.agentName+
			   " available_metrics: ["+sb.toString()+"]";
	}
}
