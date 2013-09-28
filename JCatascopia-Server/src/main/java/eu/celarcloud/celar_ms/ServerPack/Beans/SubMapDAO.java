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
