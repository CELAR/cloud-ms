package eu.celarcloud.celar_ms.ServerPack;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;
import eu.celarcloud.celar_ms.ServerPack.Database.AgentDAO;
import eu.celarcloud.celar_ms.ServerPack.Database.MetricDAO;

public class MetricProcessor implements Runnable{

	private MonitoringServer server;
	private String msg;
	
	public MetricProcessor(MonitoringServer server,String msg){
		this.server = server;
		this.msg = msg;
	}
	
	@Override
	public void run() {
		System.out.println("\nmessage: "+msg+"\n");	
		
		JSONParser parser = new JSONParser();
		JSONObject json;
		try{
			json = (JSONObject) parser.parse(msg);
			
//			System.out.println(json);

			//TODO DON'T add metrics from NON REGISTERED Agents. Send to Agent message to register first
			
			String agentID = (String) json.get("agentID");
			String agentIP = (String) json.get("agentIP");
			
			JSONArray eventArray = (JSONArray) json.get("events");
			for(Object o : eventArray){
				JSONObject event = (JSONObject) o;
				
				String group = event.get("group").toString().replace("Probe", "");
				long timestamp = Long.parseLong(event.get("timestamp").toString());
				
				AgentObj agent = this.server.agentMap.get(agentID);
				if (agent != null){
//					System.out.println("existing host: " + agentIP);
					if(!agent.isRunning() && this.server.getDatabaseFlag())
		    			AgentDAO.updateAgent(this.server.dbHandler.getConnection(), agent.getAgentID(), AgentObj.AgentStatus.UP.name());
					agent.clearAttempts();
					agent.setStatus(AgentObj.AgentStatus.UP);

					
				}
				else{
					System.out.println("host: "+agentIP+" NOT REGISTERED");
					return;
				}

				JSONArray metrics = (JSONArray) event.get("metrics");
				JSONObject obj;
				for(Object iter:metrics){
					obj = (JSONObject)iter;
					String metric_name = (String)obj.get("name");
					String metricID = agentID+":"+metric_name;
					String value = (String)obj.get("val");
					
					MetricObj metric = new MetricObj(metricID,agentID,null,metric_name,(String)obj.get("units"),
							(String)obj.get("type"),group, value);
					
					System.out.println(metric.toString());
					
					if(this.server.metricMap.putIfAbsent(metricID, metric) != null){
//						System.out.println("metric exists in map: "+metric_name);
						this.server.metricMap.replace(metricID, metric);
						
						if(this.server.getDatabaseFlag())
							MetricDAO.insertValue(this.server.dbHandler.getConnection(), metricID, timestamp, value);
					}
					else{
//						System.out.println("metric new in map: "+metric_name);
						agent.addMetricToList(metricID);
						if(this.server.getDatabaseFlag()){
							MetricDAO.createMetric(this.server.dbHandler.getConnection(), metric);
							MetricDAO.insertValue(this.server.dbHandler.getConnection(), metricID, timestamp, value);
						}
					}
				}		
			}
		} 
		catch(NullPointerException e){
			e.printStackTrace();			
		}
		catch (Exception e){
			e.printStackTrace();
		}	
	}
}
