package eu.celarcloud.celar_ms.ServerPack;

import java.util.ArrayList;
import java.util.logging.Level;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.SubMapDAO;
import eu.celarcloud.celar_ms.ServerPack.Beans.SubObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.SubObj.GroupingFunction;
import eu.celarcloud.celar_ms.ServerPack.Database.SubscriptionDAO;
import eu.celarcloud.celar_ms.ServerPack.subsciptionPack.SubTask;
import eu.celarcloud.celar_ms.SocketPack.ISocket;
/*
 * {
 *    "subID" : "067e61623b6f4ae2a1712470b63dff00",
 * 	  "metric" : {
 *  		      "name" : "clusterCPUTotal",
 *                "type" : "DOUBLE",
 *                "units" : "%",
 *                "group" : "CPU",
 *                "val" : "AVG(cpuTotal)",
 *                "period" : "20",
 *                "agents" : ["10.16.21.2","10.16.21.5"]
 *               }
 *   "storeInDB" : "YES",
     "action" : "NOTIFY >70%,<15%"
 * }
 */
public class SubProcessor implements Runnable{
	
	public enum Status {OK,ERROR,SYNTAX_ERROR,NOT_FOUND,WARNING,CONFLICT};
	
	private String[] msg;
	private ISocket router;
	private MonitoringServer server;
	
	//msg[0] address
	//msg[1] message type
	//msg[2] content
	public SubProcessor(String[] msg, ISocket router, MonitoringServer server){
		this.msg = msg;
		this.router = router;
		this.server = server;
	}

	public void run(){
		if (this.server.inDebugMode())
			System.out.println("\nSubProcessor>> processing the following message...\n"+msg[0]+" "+msg[1]+"\n"+msg[2]);	
		
		try{
			JSONParser parser = new JSONParser();
			JSONObject json;
	
			json = (JSONObject) parser.parse(msg[2]); //parse content
			
			if (msg[1].equals("SUBSCRIPTION.ADD"))
				this.addSubscription(json);	
			else if (msg[1].equals("SUBSCRIPTION.ADDAGENT"))
				this.addAgentToSub(json);
			else if (msg[1].equals("SUBSCRIPTION.REMOVEAGENT"))
				this.removeAgentFromSub(json);
			else if (msg[1].equals("SUBSCRIPTION.DELETE"))
				this.deleteSubscription(json);
			else
				this.response(Status.ERROR, msg[1]+" request does not exist");
		}
		catch(NullPointerException e){
			this.server.writeToLog(Level.SEVERE, e);
			this.response(Status.SYNTAX_ERROR, msg[1]+" Subscription is not valid");
		}
		catch(IllegalArgumentException e){
			this.server.writeToLog(Level.SEVERE, e);
			this.response(Status.SYNTAX_ERROR,"Grouping function either does not exist or is not currently supported");
		}
		catch (Exception e){
			this.server.writeToLog(Level.SEVERE, e);
			this.response(Status.ERROR, msg[1]+" an error msg");
		}	
		
	}
	
	private void addSubscription(JSONObject json){
			String subID = (String) json.get("subID");
			
			JSONObject metric = (JSONObject) json.get("metric");
			String subName = (String) metric.get("name");

			//String[] val = metric.get("val").toString().split("(");
			String[] val = metric.get("val").toString().split(":");
			GroupingFunction func = SubObj.GroupingFunction.valueOf(val[0]); //if not a valid function exception thrown
			//String originMetric = val[1].replace(")", "");
			String originMetric = val[1];
			int period = Integer.parseInt(metric.get("period").toString());
			String metricID = subID+":"+subName;

			MetricObj metricobj = new MetricObj(metricID,null,subID,subName,(String)metric.get("units"),
					                            (String)metric.get("type"),(String)metric.get("group"));
			
			JSONArray agents =  (JSONArray) metric.get("agents");
			ArrayList<String> agentlist = new ArrayList<String>();
			for(Object a : agents){
				if (server.agentMap.containsKey(a.toString()))
					agentlist.add(a.toString());
				else{ 
					this.response(Status.NOT_FOUND, "Agent with ID "+a.toString()+" does not exist");
					return;
				}
			}
			
			SubObj subobj = new SubObj(subID,subName,metricID,originMetric,agentlist,func,period);
			
			if (this.server.inDebugMode()){
				System.out.println("SubProcessor>> MetricObj...\n"+metricobj.toString());
				System.out.println("SubProcessor>> SubObj...\n"+subobj.toString());
			}
			
			//add to subMap and metricMap
			SubMapDAO.createSubcription(server.subMap, server.metricMap, subobj, metricobj);
			this.server.writeToLog(Level.INFO, "SubProcessor>> created a new subscription...\n"+subobj.toString());
			
			//add to DB
			if (this.server.getDatabaseFlag())
				SubscriptionDAO.createSubscription(this.server.dbHandler.getConnection(), subobj, metricobj);
			
			this.response(Status.OK,"");
			
			this.server.subscheduler.scheduleTask(new SubTask(server,subID), period*1000);
	}
	
	private void addAgentToSub(JSONObject json){
		String subID = (String) json.get("subID");
		String agentID = (String) json.get("agentID");
		//edit Map
		SubMapDAO.addAgent(server.subMap, server.agentMap, subID, agentID);
		
		//edit to DB
		if (this.server.getDatabaseFlag())
			SubscriptionDAO.addAgent(this.server.dbHandler.getConnection(), subID, agentID);
		
		this.response(Status.OK, "");		
	}
	
	private void removeAgentFromSub(JSONObject json){
		String subID = (String) json.get("subID");
		String agentID = (String) json.get("agentID");
		//edit Map
		SubMapDAO.removeAgent(server.subMap, server.agentMap, subID, agentID);
		
		//edit to DB
		if (this.server.getDatabaseFlag())
			SubscriptionDAO.removeAgent(this.server.dbHandler.getConnection(), subID, agentID);
		
		this.response(Status.OK, "");		
	}
	
	private void deleteSubscription(JSONObject json){
		String subID = (String) json.get("subID");
		//edit Map
		SubMapDAO.removeSubscription(server.subMap, server.metricMap, subID);
		
		//edit to DB
		if (this.server.getDatabaseFlag())
			SubscriptionDAO.deleteSubscription(this.server.dbHandler.getConnection(), subID);

		this.response(Status.OK, "");		
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
}
