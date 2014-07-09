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
package eu.celarcloud.celar_ms.ServerPack;

import java.util.logging.Level;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj.AgentStatus;
import eu.celarcloud.celar_ms.SocketPack.ISocket;
/* CONNECT
 * {
 *    "agentID" : "a953262dfee44b748453389ba7bfb18a"
 *    "agentIP" : "10.16.21.42"
 * }
 * AVAILABLE METRICS
 * {
 *    "agentID" : "a953262dfee44b748453389ba7bfb18a"
 *    "agentIP" : "10.16.21.42",
 *    "probes" :[
 *                {
 *                   "probeName": "memoryProbe",
 *                   "metrics" :[
 *                   			  {
 *                                   "name" : "memTotal",
 *                                   "type" : "INTEGER",
 *                                   "units" : "KB"
 *                                }
 *                              ] 
 *                }
 *              ]
 * }
 */
public class AgentRegister implements Runnable{
	
	public enum Status {OK,CONNECTED,ERROR,SYNTAX_ERROR,NOT_FOUND,WARNING,CONFLICT};
	
	private String[] msg;
	private ISocket router;
	private MonitoringServer server;
	
	//msg[0] address
	//msg[1] message type
	//msg[2] content
	public AgentRegister(String[] msg, ISocket router, MonitoringServer server){
		this.msg = msg;
		this.router = router;
		this.server = server;
	}

	public void run(){
		if (this.server.inDebugMode())
			System.out.println("AgentRegister>> processing the following message...\n"+msg[0]+" "+msg[1]+" "+msg[2]);	
		try {
			JSONParser parser = new JSONParser();
			JSONObject json;

			json = (JSONObject) parser.parse(msg[2]); //parse content
			
			if(msg[1].equals("AGENT.CONNECT"))
				connect(json);
			else if (msg[1].equals("AGENT.METRICS"))
				metrics(json);	
			else
				this.response(Status.ERROR, msg[1]+" request does not exist");
		}	
		catch (NullPointerException e){
			this.server.writeToLog(Level.SEVERE, e);
		}
		catch (Exception e){
			this.server.writeToLog(Level.SEVERE, e);
		}
	}
	
	private void connect(JSONObject json){
		String agentIP = (String) json.get("agentIP");
		String agentID = (String) json.get("agentID");
		if(agentID == null || agentIP == null)
			this.response(Status.SYNTAX_ERROR,"Agent details are INVALID");					
		else{
			if (this.server.agentMap.containsKey(agentID))
				this.response(Status.CONNECTED,"");		//already connected agent - heartbeat response
			else this.response(Status.OK,"");			//new agent or agent that wants to reconnect
		}
	}
	
	private void metrics(JSONObject json){
		
		String agentIP = (String) json.get("agentIP");
		String agentID = (String) json.get("agentID");
		
		AgentObj a = new AgentObj(agentID,agentIP);
		AgentObj agent = this.server.agentMap.putIfAbsent(agentID, a);
		if (agent == null){
			agent = a;
			this.server.writeToLog(Level.INFO, "New node Agent added, with ID: "+agentID+" and IP: "+agentIP);
			
			if(this.server.getDatabaseFlag())
			
				try {
					this.server.dbHandler.createAgent(agent);
				} 
				catch (Exception e) {
					this.server.writeToLog(Level.SEVERE, e);
				}
		}
		else{
			/*
			 * it is possible for agent to have gone offline but for a short period of time 
			 * (i.e. user restarted agent, changing available metrics in config file). If this is a very 
			 * short period then the HeartBeatMonitor will not have removed the Agent from the AgentMap. 
			 * For safety we clean the agent's metric list and will add the new offered metrics.
			 */
			this.removeOldAvailableMetrics(agentID, agent);
			agent.clearAttempts();
			agent.setStatus(AgentStatus.UP);
			if(this.server.getDatabaseFlag())
				try {
					this.server.dbHandler.updateAgent(agent.getAgentID(), AgentObj.AgentStatus.UP.name());
				} 
				catch (Exception e) {
					this.server.writeToLog(Level.SEVERE, e);
				}
		}
		
		JSONArray probes = (JSONArray) json.get("probes");
		JSONObject probe;
		for(Object iter:probes){
			probe = (JSONObject) iter;
			String probeName = (String) probe.get("probeName");

			JSONArray metrics = (JSONArray) probe.get("metrics");
			JSONObject met;
			MetricObj metric; 
			String metric_name;
			for(Object iter2 : metrics){
				met = (JSONObject)iter2;
				metric_name = (String)met.get("name");
				String metricID = agentID+":"+metric_name;

				metric = new MetricObj(metricID,agentID,metric_name,(String)met.get("units"),
						(String)met.get("type"),probeName.replace("Probe", ""),0);

        		if(this.server.metricMap.putIfAbsent(metricID, metric) == null){
        			if (this.server.inDebugMode())
        				System.out.println("AgentRegister>> "+agent.getAgentIP()+" added new metric: " + metric.toString());
        			
        			agent.addMetricToList(metricID);
        			
        			if(this.server.getDatabaseFlag())
						try {
							this.server.dbHandler.createMetric(metric);
						} 
        			    catch (Exception e) {
							this.server.writeToLog(Level.SEVERE, e);
						}	
        		}
        		else{
        			this.server.metricMap.replace(metricID, metric);
        		}
			}
		}
		this.response(Status.OK,"");	
		this.server.writeToLog(Level.INFO, "New Agent with ID: "+agentID+" and IP: "+agentIP+" metadata: "+agent.toString());
		//System.out.println(agent.toString()+"\n");
	}
	
	private void response(Status status,String body){
		try{
			String obj = ((body.equals("")) ? status.toString() : (status+"|"+body));
			this.router.send(msg[0], msg[1], obj);
		} 
		catch (CatascopiaException e){
			this.server.writeToLog(Level.SEVERE, e);
		}
	}
	
	private void removeOldAvailableMetrics(String host,AgentObj agent){
		try{
			for(String met:agent.getMetricList()){
				this.server.metricMap.remove(met);
				if (this.server.getDatabaseFlag())
					this.server.dbHandler.deleteMetric(agent.getAgentID(), met);
			}
			agent.clearMetricList();
		}
		catch(Exception e){
			this.server.writeToLog(Level.SEVERE, e);
		}
	}
}
