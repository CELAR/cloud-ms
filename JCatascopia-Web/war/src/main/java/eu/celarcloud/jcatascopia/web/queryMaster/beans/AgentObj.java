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
package eu.celarcloud.jcatascopia.web.queryMaster.beans;
/**
 * Represents an agent. Stores the ID, IP and status of the agent
 */
public class AgentObj{
		
	private String agentID;
	private String agentIP;
	private String status;
	
	public AgentObj(String agentID,String agentIP, String status){
		this.agentID = agentID;
		this.agentIP = agentIP;
		this.status = status;
	}
	
	public AgentObj(){}
	
	public String getAgentID(){
		return this.agentID;
	}
	
	public String getAgentIP(){
		return this.agentIP;
	}
	
	public void setAgentIP(String ip){
		this.agentIP = ip;
	}
		
	public void setStatus(String status){
		this.status = status;
	}	
	
	public String getStatus(){
		return this.status;
	}
	
	public String toString(){
		return "Agent>> AgentID: "+this.agentID+" AgentIP: "+this.agentIP+" status: "+this.status;
	}
	
	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		
		sb.append("\"agentID\":\""+this.agentID+"\"");
		if(this.agentIP != null && this.agentIP.length() > 0) 
			sb.append(",\"agentIP\":\""+this.agentIP+"\"");
		if(this.status != null && this.status.length() > 0)
			sb.append(",\"status\":\""+this.status+"\"");
		
		sb.append("}");
		return sb.toString();
	}
}
