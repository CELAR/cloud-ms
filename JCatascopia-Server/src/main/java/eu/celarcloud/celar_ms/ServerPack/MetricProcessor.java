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

import java.util.UUID;
import java.util.logging.Level;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;
import eu.celarcloud.celar_ms.SocketPack.Dealer;

public class MetricProcessor implements Runnable{

	private MonitoringServer server;
	private String msg;
	
	public MetricProcessor(MonitoringServer server,String msg){
		this.server = server;
		this.msg = msg;
	}
	
	public void run() {
		if (this.server.inDebugMode())
			System.out.println("\nMetricProcessor>> processing the following message...\n" + msg);	
		
		try{
			JSONParser parser = new JSONParser();
			JSONObject json;
			json = (JSONObject) parser.parse(msg);
			
			//check if metrics are from an intermediate server
			if (json.get("serverID") == null)
				processor(json);
			else
				redistProcessor(json);
		} 
		catch(NullPointerException e){
			this.server.writeToLog(Level.SEVERE, e);			
		}
		catch (Exception e){
			this.server.writeToLog(Level.SEVERE, e);			
		}				
		return;
	}
			
	public void processor(JSONObject json){
		try{						
			String agentID = (String) json.get("agentID");
			String agentIP = (String) json.get("agentIP");
			
			AgentObj agent = this.server.agentMap.get(agentID);
			if (agent != null){
				if(!agent.isRunning() && this.server.getDatabaseFlag())
					this.server.dbHandler.updateAgent(agent.getAgentID(), AgentObj.AgentStatus.UP.name());
				agent.clearAttempts();
				agent.setStatus(AgentObj.AgentStatus.UP);	
			}
			else{
				if (this.server.inDebugMode())
					System.out.println("Agent with ID: "+agentID+" and IP: "+agentIP+")"+ "is NOT REGISTERED");
				this.server.writeToLog(Level.INFO, "Agent with ID: "+agentID+" and IP: "+agentIP+" tried to inject metrics without REGISTERING");
				this.sendRegisterRequest(agentIP, "4243", "AGENT.RECONNECT", "");
				return;
			}
			
			JSONArray eventArray = (JSONArray) json.get("events");
			JSONObject event;
			for(Object o : eventArray){
				event = (JSONObject) o;
				
				String group = event.get("group").toString().replace("Probe", "");
				long timestamp = Long.parseLong(event.get("timestamp").toString());
				
				JSONArray metrics = (JSONArray) event.get("metrics");
				JSONObject obj;
				for(Object iter : metrics){	
					obj = (JSONObject) iter;
					String metric_name = (String) obj.get("name");
					String metricID = agentID+":"+metric_name;
					String value = (String) obj.get("val");
					
					MetricObj metric = new MetricObj(metricID,agentID,metric_name,(String)obj.get("units"),(String)obj.get("type"),group, value, timestamp);
					
					if (this.server.inDebugMode())
						System.out.println("MetricProccessor>> metric values: " + metric.toString());
					
					if(this.server.metricMap.putIfAbsent(metricID, metric) != null){
						this.server.metricMap.replace(metricID, metric);
						
						if(this.server.getDatabaseFlag())
							this.server.dbHandler.insertMetricValue(metric);
					}
					else{
						agent.addMetricToList(metricID);
						if(this.server.getDatabaseFlag()){
							this.server.dbHandler.createMetric(metric);
							this.server.dbHandler.insertMetricValue(metric);
						}
					}
					
					if(server.inRedistributeMode()){
						String t = metric.getType();
						if (t.equals("DOUBLE") || t.equals("INTEGER") || t.equals("LONG") || t.equals("FLOAT")){
							metric.calcAvg(Double.parseDouble(value));
						}
					}
				}	
			}
		} 
		catch(NullPointerException e){
			this.server.writeToLog(Level.SEVERE, e);			
		}
		catch (Exception e){
			this.server.writeToLog(Level.SEVERE, e);			
		}	
		return;
	}
	
	private void sendRegisterRequest(String ip, String port, String header,String msg){
		Dealer dealer = new Dealer(ip, port, UUID.randomUUID().toString().replace("-", ""));
    	String[] response = null;
    	String resp = "";
    	try {			
	    	dealer.send("", header, msg);	
            response = dealer.receive(7000L);
	        if (response != null)
	            resp = response[1];      	
    	}
    	catch(Exception e){	
    		e.printStackTrace();
    	}
    	finally{
    		dealer.close();
    	}
	}
	
	
	public void redistProcessor(JSONObject json){
		try{
	//		System.out.println(json.toString());
			String serverID = (String)json.get("serverID");
			String serverIP = (String)json.get("serverIP");
			
			JSONArray agentArray = (JSONArray) json.get("agents");
			for(Object o : agentArray){
				JSONObject a = (JSONObject) o;
				String agentID = (String)a.get("agentID");
				String agentIP = (String)a.get("agentIP");
				
				AgentObj agent = this.server.agentMap.get(agentID);
				if (agent != null){
					//agent exists in agentMap
//					System.out.println("existing host: " + agentIP);
					if(!agent.isRunning() && this.server.getDatabaseFlag())
		    			//AgentDAO.updateAgent(this.server.dbHandler.getConnection(), agent.getAgentID(), AgentObj.AgentStatus.UP.name());
						this.server.dbHandler.updateAgent(agent.getAgentID(), AgentObj.AgentStatus.UP.name());
					agent.clearAttempts();
					agent.setStatus(AgentObj.AgentStatus.UP);	
				}
				else{
					//new agent - register
					String agentName = (String) json.get("agentName");
					String agentTags = (String) json.get("tags");
					agent = new AgentObj(agentID,agentIP,agentName,agentTags);
					this.server.agentMap.putIfAbsent(agentID, agent);
					this.server.writeToLog(Level.INFO, "Intermediate Server: "+serverIP+" New node Agent added, with ID: "+agentID+" and IP: "+agentIP);
	    			//AgentDAO.createAgent(this.server.dbHandler.getConnection(), agent);
					this.server.dbHandler.createAgent(agent);
				}
				
				JSONArray metricArray = (JSONArray) a.get("metrics");
				for(Object o2 : metricArray){
					JSONObject m = (JSONObject) o2;
					
					String metric_name = (String)m.get("name");
					String metricID = agentID+":"+metric_name;
					String value = (String)m.get("val");
					long timestamp = Long.parseLong(m.get("timestamp").toString());
	
					
					MetricObj metric = new MetricObj(metricID,agentID,metric_name,(String)m.get("units"),
							                         (String)m.get("type"),(String)m.get("group"), value, timestamp);
					
					if (this.server.inDebugMode())
						System.out.println("MetricProccessor>> metric values...\n"+metric.toString());
					
					if(this.server.metricMap.putIfAbsent(metricID, metric) != null){
	//					System.out.println("metric exists in map: "+metric_name);
						this.server.metricMap.replace(metricID, metric);
						
						if(this.server.getDatabaseFlag())
							//MetricDAO.insertValue(this.server.dbHandler.getConnection(), metricID, timestamp, value);
							this.server.dbHandler.insertMetricValue(metric);
					}
					else{
	//					System.out.println("metric new in map: "+metric_name);
						agent.addMetricToList(metricID);
						if(this.server.getDatabaseFlag()){
							//MetricDAO.createMetric(this.server.dbHandler.getConnection(), metric);
							//MetricDAO.insertValue(this.server.dbHandler.getConnection(), metricID, timestamp, value);
							this.server.dbHandler.createMetric(metric);
							this.server.dbHandler.insertMetricValue(metric);
						}
					}
				}
				
			}
		} 
		catch(NullPointerException e){
			this.server.writeToLog(Level.SEVERE, e);			
		}
		catch (Exception e){
			this.server.writeToLog(Level.SEVERE, e);			
		}					
	}
}
