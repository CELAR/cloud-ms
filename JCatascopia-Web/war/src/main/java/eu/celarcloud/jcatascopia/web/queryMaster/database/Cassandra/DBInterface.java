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
package eu.celarcloud.jcatascopia.web.queryMaster.database.Cassandra;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.Policies;
import com.datastax.driver.core.utils.UUIDs;

import eu.celarcloud.jcatascopia.web.queryMaster.beans.AgentObj;
import eu.celarcloud.jcatascopia.web.queryMaster.beans.MetricObj;
import eu.celarcloud.jcatascopia.web.queryMaster.beans.SubscriptionObj;
import eu.celarcloud.jcatascopia.web.queryMaster.database.IDBInterface;
import eu.celarcloud.jcatascopia.web.utils.Dealer;

public class DBInterface implements IDBInterface{
	private Cluster cluster;
	private Session session;
	private List<String> endpoints;
	private String keyspace;
	
	private static final String GET_AGENTS = "SELECT * FROM agent_table";
	private static final String GET_AGENT_AVAILABLE_METRICS = "SELECT * FROM metric_table WHERE agentID=?";
	//dont need to do ORDER BY event_timestamp DESC since the most recent metrics values are stored first on each row
	private static final String GET_METRIC_VALUE = "SELECT * FROM metric_value_table WHERE metricID=? AND event_date=? LIMIT 1";
	private static final String GET_AVAILABLE_METRICS_FOR_SUBS = "SELECT * FROM metric_table";
	private static final String GET_SUBSCRIPTIONS = "SELECT * FROM subscription_table";
	private static final String GET_SUB_META = "SELECT * FROM subscription_table WHERE subID=?";
	private static final String GET_SUB_AGENTS ="SELECT * FROM subscription_agents_table WHERE subID=?";
	private static final String GET_AVAILABLE_METRICS_ALL_AGENTS = "SELECT * FROM metric_table";

	PreparedStatement getAgentsStmt;
	PreparedStatement getAgentsAvailableMetricsStmt;
	PreparedStatement getMetricValuesStmt;
	PreparedStatement getAvailableMetricsForSubsStmt;
	PreparedStatement getSubsStmt;
	PreparedStatement getSubMetaStmt;
	PreparedStatement getSubAgentsStmt;
	PreparedStatement getAvailableMetricsAllAgentsStmt;
	
	public DBInterface(List<String> endpoints, String keyspace){
		this.endpoints = endpoints;
		this.keyspace = keyspace;
	}
	
	public DBInterface(List<String> endpoints, String user, String pass, String keyspace){
		this(endpoints, keyspace);
	}

	
	public void dbConnect(){
		this.cluster = Cluster.builder().addContactPoints(endpoints.toArray(new String[endpoints.size()]))
					                     .withRetryPolicy(Policies.defaultRetryPolicy())
					                     .build();
		System.out.printf("Successfully connected to cluster: %s\n", cluster.getMetadata().getClusterName());

		session = cluster.connect(keyspace);
		System.out.println("Successfully connected and created a new session");
		
		this.getAgentsStmt = session.prepare(GET_AGENTS);
		this.getAgentsAvailableMetricsStmt = session.prepare(GET_AGENT_AVAILABLE_METRICS);
		this.getMetricValuesStmt = session.prepare(GET_METRIC_VALUE);
		this.getAvailableMetricsForSubsStmt = session.prepare(GET_AVAILABLE_METRICS_FOR_SUBS);
		this.getSubsStmt = session.prepare(GET_SUBSCRIPTIONS);
		this.getSubMetaStmt = session.prepare(GET_SUB_META);
		this.getSubAgentsStmt = session.prepare(GET_SUB_AGENTS);
		this.getAvailableMetricsAllAgentsStmt = session.prepare(GET_AVAILABLE_METRICS_ALL_AGENTS);
	}

	public void dbClose() {
		this.cluster.close();	
	}

	public ArrayList<AgentObj> getAgents(String status){
		try{
			BoundStatement bs = this.getAgentsStmt.bind();
			ResultSet rs = session.execute(bs);
	        ArrayList<AgentObj> agentlist = new ArrayList<AgentObj>();
	        String st;
	        AgentObj agent;
			for (Row row : rs) {
				st = row.getString("status");
				if (status == null || status.length() == 0 || status.equals(st)){
					agent = new AgentObj(row.getString("agentID"),row.getString("agentIP"),st);
					agent.setAgentName(row.getString("agentName"));
					agent.setTags(row.getString("tags"));
					agentlist.add(agent);	
				}
			}
			return agentlist;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public ArrayList<AgentObj> getAgentsWithTimestamps(String status){
		try{
			BoundStatement bs = this.getAgentsStmt.bind();
			ResultSet rs = session.execute(bs);
	        ArrayList<AgentObj> agentlist = new ArrayList<AgentObj>();
	        String st,tstart,tstop;
	        AgentObj agent;
			for (Row row : rs) {
				st = row.getString("status");
				if (status == null || status.length() == 0 || status.equals(st)){
					agent = new AgentObj(row.getString("agentID"),row.getString("agentIP"),st);
					agentlist.add(agent);	
					tstart = Long.toString(UUIDs.unixTimestamp(row.getUUID("tstart"))/1000);
					agent.setTstart(tstart);
					if (row.getUUID("tstop") != null){
						tstop = Long.toString(UUIDs.unixTimestamp(row.getUUID("tstop"))/1000);
						agent.setTstop(tstop);
					}
					agent.setAgentName(row.getString("agentName"));
					agent.setTags(row.getString("tags"));
				}
			}
			return agentlist;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<MetricObj> getAgentAvailableMetrics(String agentID){
		try{
			BoundStatement bs = this.getAgentsAvailableMetricsStmt.bind();
			bs.setString("agentID", agentID);
			ResultSet rs = session.execute(bs);
			ArrayList<MetricObj> metriclist = new ArrayList<MetricObj>();
			for (Row row : rs)
				metriclist.add(new MetricObj(row.getString("metricID"), row.getString("name"),row.getString("units"),row.getString("type"),row.getString("mgroup")));
			return metriclist;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;		
	}
	
	public ArrayList<MetricObj> getMetricValues(String[] registeredMetrics){
		try{
			BoundStatement bs = this.getMetricValuesStmt.bind();
			ResultSet rs;
			Row row;
			String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
			ArrayList<MetricObj> metriclist = new ArrayList<MetricObj>();
			String t = "";
			
			for(String id : registeredMetrics){
				bs.setString("metricID", id);
				bs.setString("event_date", date);
				rs = this.session.execute(bs);
				row = rs.one();
				if(row != null){
					t = new Date(UUIDs.unixTimestamp(row.getUUID("event_timestamp"))).toString().split(" ")[3];
					metriclist.add(new MetricObj(row.getString("metricID"), row.getString("name"),
	    					row.getString("units"),row.getString("type"),row.getString("mgroup"),row.getString("value"),t));
				}
			}
			return metriclist;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;			
	}
	
	public ArrayList<MetricObj> getMetricValuesByTime(String metricID, long interval){
		try{
			interval *= 1000; //interval is in seconds... convert to ms
			long now = System.currentTimeMillis();
			long start = now - interval;
			long days = interval/(86400000);
			String d = ""; int i=0; boolean first = true;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			do{
				if(!first)
					d += ",";	
				d += "'"+sdf.format(new Date(now-i*86400000))+"'";
				first = false;
			}while((i++)<days); //calculate the days to check for since cassandra rows are partitioned by date
			
			String cql = "SELECT * FROM metric_value_table WHERE metricID='"+metricID+"' AND event_date IN ("+d+") AND event_timestamp>="+UUIDs.endOf(start);
			ResultSet rs = session.execute(cql);
			ArrayList<MetricObj> metriclist = new ArrayList<MetricObj>(); 	
			String t;
			for(Row row : rs){
				t = new Date(UUIDs.unixTimestamp(row.getUUID("event_timestamp"))).toString().split(" ")[3];
				metriclist.add(new MetricObj(row.getString("metricID"), row.getString("name"),
						row.getString("units"),row.getString("type"),row.getString("mgroup"),row.getString("value"),t));
			}
			Collections.reverse(metriclist); //reverse the order to get an ASCENDING list since cassandra keeps the latest value first for efficiency
			return metriclist;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;					
	}

	public ArrayList<MetricObj> getMetricValuesByTime(String metricID, long tstart, long tend){
		try{
			long days = (tend - tstart)/(86400000);
			String d = ""; int i=0; boolean first = true;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			do{
				if(!first)
					d += ",";	
				d += "'"+sdf.format(new Date(tend-i*86400000))+"'";
				first = false;
			}while((i++)<days);
			
			String cql = "SELECT * FROM metric_value_table WHERE metricID='"+metricID+"' AND event_date IN ("+d+") AND event_timestamp>="+UUIDs.endOf(tstart)+" AND event_timestamp<="+UUIDs.endOf(tend);
			ResultSet rs = session.execute(cql);
			ArrayList<MetricObj> metriclist = new ArrayList<MetricObj>(); 	
			String t;
			for(Row row : rs){
				t = new Date(UUIDs.unixTimestamp(row.getUUID("event_timestamp"))).toString().split(" ")[3];
				System.out.println(t+" "+row.toString());
				metriclist.add(new MetricObj(row.getString("metricID"), row.getString("name"),
								row.getString("units"),row.getString("type"),row.getString("mgroup"),row.getString("value"),t));
			}
			return metriclist;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;				
	}

	public ArrayList<MetricObj> getAvailableMetricsForSubs(){
		try{
			BoundStatement bs = this.getAvailableMetricsForSubsStmt.bind();		
			ResultSet rs = session.execute(bs);
			String name;
			HashMap<String,MetricObj> map = new HashMap<String,MetricObj>();
			for(Row row : rs){
				if (row.getString("is_sub") == null){ //not a subscription metric
					name = row.getString("name");
					if(!map.containsKey(name))
						map.put(name, new MetricObj(row.getString("metricID"), name, row.getString("units"),row.getString("type"),row.getString("mgroup")));
				}
			}
			ArrayList<MetricObj> metriclist = new ArrayList<MetricObj>();
			metriclist.addAll(map.values());
			return metriclist;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;		
	}

	public ArrayList<SubscriptionObj> getSubscriptions() {
		try{
			BoundStatement bs = this.getSubsStmt.bind();
			ResultSet rs = session.execute(bs);
			ArrayList<SubscriptionObj> subsList = new ArrayList<SubscriptionObj>();
			for(Row row : rs)
				subsList.add(new SubscriptionObj(row.getString("subID"),row.getString("name"), null,null));
			return subsList;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public SubscriptionObj getSubMeta(String subID) {
		try{
			BoundStatement bs = this.getSubMetaStmt.bind();
			bs.setString("subID", subID);
			ResultSet rs = session.execute(bs);
			Row row = rs.one();
			if (row != null)
				return	new SubscriptionObj(row.getString("subID"),row.getString("name"),row.getString("func"),String.valueOf(row.getInt("period")),
	                    row.getString("originMetric"),row.getString("metricID"),row.getString("type"),row.getString("units"),row.getString("mgroup"));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<AgentObj> getAgentsForSub(String subID) {
		try{
			BoundStatement bs = this.getSubAgentsStmt.bind();
			bs.setString("subID", subID);
			ResultSet rs = session.execute(bs);
			ArrayList<AgentObj> agentlist = new ArrayList<AgentObj>();
			for(Row row : rs)
				agentlist.add(new AgentObj(row.getString("agentID"),row.getString("agentIP"),null));
			return agentlist;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public boolean createSubscription(String ip, String port, String json){
		boolean success = msConnect(ip, port, "SUBSCRIPTION.ADD", json);
		System.out.println("SUBSCRIPTION.ADD -> " + (success? "success" : "fail"));
		return success;
	}
	
	public boolean removeSubscription(String ip, String port, String json){
		boolean success = msConnect(ip, port, "SUBSCRIPTION.DELETE", json);
		System.out.println("SUBSCRIPTION.DELETE -> " + (success? "success" : "fail"));
		return success;
	}
	
	/**
	 * Calls the monitoring server in order to add an agent to a subscription.
	 * 
	 * @param ip The monitoring server's ip
	 * @param port The monitoring server's port
	 * @param json a json with the subscription's id and agent's id
	 * @return true if the agent was added to the subscription agents, otherwise false
	 */
	public boolean addAgent(String ip, String port, String json){   
		boolean success = msConnect(ip, port, "SUBSCRIPTION.ADDAGENT", json);
		System.out.println("SUBSCRIPTION.ADDAGENT -> " + (success? "success" : "fail"));
		return success;
	}
	
	/**
	 * Calls the monitoring server in order to remove an agent from a subscription.
	 * 
	 * @param ip The monitoring server's ip
	 * @param port The monitoring server's port
	 * @param json a json with the subscription's id and agent's id
	 * @return true if the agent was removed from the subscription agents, otherwise false
	 */
	public boolean removeAgent(String ip, String port, String json){   
		boolean success = msConnect(ip, port, "SUBSCRIPTION.REMOVEAGENT", json);
		System.out.println("SUBSCRIPTION.REMOVEAGENT -> " + (success? "success" : "fail"));
		return success;
	}
	
	/**
	 * Connects to the monitoring server on the specified ip and port and requests the
	 * given type of action with the given data from it.
	 * 
	 * @param ip The monitoring server's ip
	 * @param port The monitoring server's port
	 * @param type The type of action to request from the monitoring server
	 * @param json_request The request body 
	 * @return true if the action was successful, otherwise false
	 */
	private boolean msConnect(String ip, String port, String type, String json_request){
		Dealer dealer = new Dealer(ip,port,"tcp",16,UUID.randomUUID().toString().replace("-", ""));
		int attempts = 0; 
		boolean connected = false;
    	String[] response = null;
    	try {			
			while(((attempts++)<3) && (!connected)){
	    		dealer.send("",type,json_request);
	            response = dealer.receive(12000L);
	            if (response != null){
	            	for(String s: response) System.out.println("ROUTER RESPONSE: " + s);
	            	connected = (response[1].contains("OK")) ? true : false;
	            	break;
	            }
				else	
					Thread.sleep(3000);
	    	}
			dealer.close();
		} 
    	catch (InterruptedException e){
			e.printStackTrace();
		} 
		return connected;
	}
	
	public ArrayList<MetricObj> getAvailableMetricsForAllAgents(){
		try{
			BoundStatement bs = this.getAvailableMetricsAllAgentsStmt.bind();
			ResultSet rs = session.execute(bs);
			
			HashMap<String,MetricObj> map = new HashMap<String,MetricObj>();
			for (Row row : rs)
				map.put(row.getString("name"), new MetricObj(row.getString("metricID"), row.getString("name"),row.getString("units"),row.getString("type"),row.getString("mgroup")));
			
			ArrayList<MetricObj> metriclist = new ArrayList<MetricObj>(map.values());
			return metriclist;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;		
	}
}
