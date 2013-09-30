package eu.celarcloud.celar_ms.ServerPack;

import java.util.logging.Level;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj.AgentStatus;
import eu.celarcloud.celar_ms.ServerPack.Database.AgentDAO;
import eu.celarcloud.celar_ms.ServerPack.Database.MetricDAO;
import eu.celarcloud.celar_ms.SocketPack.ISocket;
/* CONNECT
 * {
 *    "agentID" : "a953262dfee44b748453389ba7bfb18a"
 *    "agentIP" : "10.16.21.42",
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
	
	public enum Status {OK,ERROR,SYNTAX_ERROR,NOT_FOUND,WARNING,CONFLICT};
	
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
			System.out.println("\nAgentRegister>> processing the following message...\n"+msg[0]+" "+msg[1]+"\n"+msg[2]);	
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
		else this.response(Status.OK,"");		
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
					AgentDAO.createAgent(this.server.dbHandler.getConnection(), agent);
				} 
			    catch (CatascopiaException e) {
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
					AgentDAO.updateAgent(this.server.dbHandler.getConnection(), agent.getAgentID(), AgentObj.AgentStatus.UP.name());
				} 
			    catch (CatascopiaException e) {
					this.server.writeToLog(Level.SEVERE, e);
				}
		}
		
		JSONArray probes = (JSONArray) json.get("probes");
		JSONObject probe;
		for(Object iter:probes){
			probe = (JSONObject)iter;
			String probeName = (String) probe.get("probeName");

			JSONArray metrics = (JSONArray) probe.get("metrics");
			JSONObject met;
			MetricObj metric; 
			String metric_name;
			for(Object iter2:metrics){
				met = (JSONObject)iter2;
				metric_name = (String)met.get("name");
				String metricID = agentID+":"+metric_name;

				metric = new MetricObj(metricID,agentID,null,metric_name,(String)met.get("units"),
						(String)met.get("type"),probeName.replace("Probe", ""));

        		if(this.server.metricMap.putIfAbsent(metricID, metric) == null){
        			if (this.server.inDebugMode())
        				System.out.println("AgentRegister>> "+agent.getAgentIP()+" added new metric...\n"+metric.toString());
        			agent.addMetricToList(metricID);
        			
        			if(this.server.getDatabaseFlag())
						try {
							MetricDAO.createMetric(this.server.dbHandler.getConnection(), metric);
						} 
        			    catch (CatascopiaException e) {
							this.server.writeToLog(Level.SEVERE, e);
						}
        			
        		}
        		else{
//        			System.out.println("metric exists in map: "+metric_name);
        			this.server.metricMap.replace(metricID, metric);
        		}
			}
		}
		this.response(Status.OK,"");	
		System.out.println(agent.toString()+"\n");
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
					MetricDAO.deleteMetric(this.server.dbHandler.getConnection(), met);
			}
			agent.clearMetricList();
		}
		catch(Exception e){
			this.server.writeToLog(Level.SEVERE, e);
		}
	}
}