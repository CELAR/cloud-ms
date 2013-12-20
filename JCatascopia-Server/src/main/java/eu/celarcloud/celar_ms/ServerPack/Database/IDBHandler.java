package eu.celarcloud.celar_ms.ServerPack.Database;

import java.util.ArrayList;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.SubObj;

public interface IDBHandler{
	
	public void dbConnect();
	public void dbClose();
	public void dbInit(boolean drop_tables) throws CatascopiaException;
	
	public void createAgent(AgentObj agent);
	public void updateAgent(String agentID, String status);
	public void deleteAgent(String agentID);
	
	public void createMetric(MetricObj metric);
	public void deleteMetric(String agentID, String metricID);
	public void insertMetricValue(MetricObj metric);
	public void insertBatchMetricValues(ArrayList<MetricObj> metriclist);	
	
	public void createSubscription(SubObj sub, MetricObj metric);
	public void deleteSubscription(String subID);
	public void addAgentToSub(String subID, String agentID);
	public void removeAgentFromSub(String subID, String agentID);
	
	public void doQuery(String cql, boolean print);//only for testing and debugging
}
