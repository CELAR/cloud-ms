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
package eu.celarcloud.celar_ms.ServerPack.Database.Cassandra;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.Policies;
import com.datastax.driver.core.utils.UUIDs;

import eu.celarcloud.celar_ms.ServerPack.IJCatascopiaServer;
import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.SubObj;
import eu.celarcloud.celar_ms.ServerPack.Database.IDBHandler;

public class DBHandler implements IDBHandler{
	private Cluster cluster;
	private Session session;
	private List<String> endpoints;
	private String keyspace;
	private IJCatascopiaServer server;
	
	private static final String CREATE_AGENT = "INSERT INTO agent_table (agentID, agentIP, status, agentName, tags, tstart, tstop) VALUES (?,?,?,?,?,now(),NULL)";
	private static final String UPDATE_AGENT = "UPDATE agent_table SET status=?,tstop=NULL WHERE agentID=?";
	private static final String UPDATE_AGENT_TERMINATED = "UPDATE agent_table SET status=?,tstop=now() WHERE agentID=?";
	private static final String DELETE_AGENT = "DELETE FROM agent_Table WHERE agentID=?";
	private static final String CREATE_METRIC = "INSERT INTO metric_table (agentID, metricID, name, mgroup, type, units) VALUES (?,?,?,?,?,?)";
	private static final String DELETE_METRIC = "DELETE FROM metric_table WHERE agentID=? AND metricID =?";
	private static final String INSERT_METRIC_VALUE = "INSERT INTO metric_value_table " +
														 "(metricID, event_date, event_timestamp, value, name, mgroup, type, units) VALUES (?,?,?,?,?,?,?,?) USING TTL 432000";
	//ttl set to 5 days. a month is 2592000 seconds
	private static final String CREATE_SUBSCRIPTION = "INSERT INTO subscription_table " +
			 											"(subID, metricID, func, originMetric, period, name, mgroup, type, units) VALUES (?,?,?,?,?,?,?,?,?)";;
	private static final String ADD_AGENT_TO_SUB = "INSERT INTO subscription_agents_table (subID,agentID,agentIP) VALUES (?,?,?);";
	private static final String REMOVE_AGENT_FROM_SUB = "DELETE FROM subscription_agents_table WHERE subID =? AND agentID =? ";
	private static final String REMOVE_ALL_AGENTS_FROM_SUB = "DELETE FROM subscription_agents_table WHERE subID =?";
	private static final String DELETE_SUBSCRIPTION = "DELETE FROM subscription_table WHERE subID=?";
	
	private PreparedStatement insertAgentStmt;
	private PreparedStatement updateAgentStmt;
	private PreparedStatement updateAgentTermStmt;
	private PreparedStatement deleteAgentStmt;
	private PreparedStatement insertMetricStmt;
	private PreparedStatement deleteMetricStmt;
	private PreparedStatement insertMetricValueStmt;
	private PreparedStatement createSubStmt;
	private PreparedStatement addAgentToSubStmt;
	private PreparedStatement removeAgentFromSubStmt;
	private PreparedStatement removeAllAgentsSubStmt;
	private PreparedStatement deleteSubStmt;

	public DBHandler(List<String> endpoints, String keyspace, IJCatascopiaServer server){
		this.endpoints = endpoints;
		this.keyspace = keyspace.toLowerCase();
		this.server = server;
	}
	
	public DBHandler(List<String> endpoints, String user, String pass, String keyspace, Integer cnum, IJCatascopiaServer server){
		this(endpoints, keyspace, server);
	}

	public void dbConnect(){
		this.cluster = Cluster.builder().addContactPoints(endpoints.toArray(new String[endpoints.size()]))
				 						.withRetryPolicy(Policies.defaultRetryPolicy())
				 						.build();
		System.out.printf("Successfully connected to cluster: %s\n", cluster.getMetadata().getClusterName());
		
		boolean found = false;
		for(KeyspaceMetadata k : cluster.getMetadata().getKeyspaces())
			if (keyspace.equals(k.getName()))
				found = true;
		
		if (!found){
			session = cluster.connect(); //connect to cluster
			String cql = "CREATE KEYSPACE "+ keyspace +" WITH " + 
										  "replication = {'class':'SimpleStrategy','replication_factor':1}";
			session.execute(cql); //create keyspace
			System.out.println("Successfully created new keyspace: "+ keyspace);
		}
		session = cluster.connect(keyspace);
		System.out.println("Successfully connected and created a new session");
	}

	public void dbClose(){
		this.cluster.shutdown();	
	}
	
	public void dbInit(boolean drop_tables){
	    try {
			if(drop_tables){ 
				String[] tables = {"agent_table","metric_table","metric_value_table","subscription_table", "subscription_agents_table"};
				String cql;
				for(String t : tables){
					cql = "DROP TABLE IF EXISTS " + t;
					session.execute(cql);
				}
				
				String table = "agent_table";
				cql = "CREATE TABLE "+table+" (" + 
		                " agentID varchar," + 
		                " agentIP varchar," +
		                " agentName varchar," + 
		                " tags varchar," +
		                " status varchar," +
		                " tstart timeuuid," +
		                " tstop timeuuid," +
		                " PRIMARY KEY (agentID)" +
		                ");";
				session.execute(cql);
				System.out.println("Successfully dropped and created table: " + table);
				
				table = "metric_table";
				cql = "CREATE TABLE "+table+" (" + 
		                " agentID varchar," + 
		                " metricID varchar," +
		                " name varchar," +
		                " mgroup varchar," +
		                " type varchar," +
		                " units varchar," +
		                " is_sub varchar," +
		                " PRIMARY KEY (agentID,metricID)" +
		                ");";
				session.execute(cql);
				System.out.println("Successfully dropped and created table: " + table);
				
				table = "metric_value_table";
				cql = "CREATE TABLE "+table+" (" + 
		                " metricID varchar," + 
		                " event_date text," +
		                " event_timestamp timeuuid," +
		                " value varchar," +
		                " name varchar," +
		                " mgroup varchar," +
		                " type varchar," +
		                " units varchar," +
		                " PRIMARY KEY ((metricID,event_date),event_timestamp)" +
		                ") WITH CLUSTERING ORDER BY (event_timestamp DESC);";
				session.execute(cql);
				System.out.println("Successfully dropped and created table: " + table);

				table = "subscription_table";
				cql = "CREATE TABLE "+table+" (" + 
						" subID varchar," + 
		                " metricID varchar," + 
		                " func varchar," +
		                " originMetric varchar," +
		                " period int," +
		                " name varchar," +
		                " mgroup varchar," +
		                " type varchar," +
		                " units varchar," +
		                " PRIMARY KEY (subID)" +
		                ");";
				session.execute(cql);				
				System.out.println("Successfully dropped and created table: " + table);
				
				table = "subscription_agents_table";
				cql = "CREATE TABLE "+table+" (" + 
						" subID varchar," + 
		                " agentID varchar," + 
		                " agentIP varchar," +
		                " PRIMARY KEY (subID,agentID)" +
		                ");";
				session.execute(cql);
				System.out.println("Successfully dropped and created table: " + table);
			}
			
			this.insertAgentStmt = session.prepare(CREATE_AGENT);
			this.updateAgentStmt = session.prepare(UPDATE_AGENT);
			this.updateAgentTermStmt = session.prepare(UPDATE_AGENT_TERMINATED);
			this.deleteAgentStmt = session.prepare(DELETE_AGENT);
			this.insertMetricStmt = session.prepare(CREATE_METRIC);
			this.deleteMetricStmt = session.prepare(DELETE_METRIC);
			this.insertMetricValueStmt = session.prepare(INSERT_METRIC_VALUE);
			this.createSubStmt = session.prepare(CREATE_SUBSCRIPTION);
			this.addAgentToSubStmt = session.prepare(ADD_AGENT_TO_SUB);
			this.removeAgentFromSubStmt = session.prepare(REMOVE_AGENT_FROM_SUB);
			this.removeAllAgentsSubStmt = session.prepare(REMOVE_ALL_AGENTS_FROM_SUB);
			this.deleteSubStmt = session.prepare(DELETE_SUBSCRIPTION);
	    }
	    catch(Exception e){
	    	this.server.writeToLog(Level.SEVERE, "Cassandra DB Handler>> "+e);
	    }
	}

	public void createAgent(AgentObj agent){
		try{
			BoundStatement bs = this.insertAgentStmt.bind();
			bs.setString("agentID", agent.getAgentID());
			bs.setString("agentIP", agent.getAgentIP());
			bs.setString("status", agent.getStatus().name());
			bs.setString("agentName", agent.getAgentName());
			if (agent.getAgentTags() != null)
				bs.setString("tags", agent.getAgentTags());
			session.execute(bs);
		}
		catch(Exception e){
		    this.server.writeToLog(Level.SEVERE, "Cassandra DBHandler createAgent>> "+e);
		}
	}

	public void updateAgent(String agentID, String status){
		try{
			BoundStatement bs=null;
			if(!status.equals("TERMINATED"))
				bs = this.updateAgentStmt.bind();
			else 
				bs = this.updateAgentTermStmt.bind();
			bs.setString("agentID", agentID);
			bs.setString("status", status);
			session.execute(bs);		
		}
		catch(Exception e){
		    this.server.writeToLog(Level.SEVERE, "Cassandra DB Handler updateAgent>> "+e);
		}
	}

	public void deleteAgent(String agentID){
		try{
			BoundStatement bs = this.deleteAgentStmt.bind();
			bs.setString("agentID", agentID);
			session.execute(bs);
		}
		catch(Exception e){
			this.server.writeToLog(Level.SEVERE, "Cassandra DBHandler deleteAgent>> "+e);
		}
	}
	
	public void createMetric(MetricObj metric){
		try{
			BoundStatement bs = this.insertMetricStmt.bind();
			bs.setString("agentID", metric.getAgentID());
			bs.setString("metricID", metric.getMetricID());
			bs.setString("name", metric.getName());
			bs.setString("mgroup",metric.getGroup());
			bs.setString("type", metric.getType());
			bs.setString("units", metric.getUnits());
			session.execute(bs);
		}
		catch(Exception e){
			this.server.writeToLog(Level.SEVERE, "Cassandra DBHandler createMetric>> "+e);
		}
	}
	
	public void deleteMetric(String agentID, String metricID){
		try{
			BoundStatement bs = this.deleteMetricStmt.bind();
			bs.setString("agentID", agentID);
			bs.setString("metricID", metricID);
			session.execute(bs);
		}
		catch(Exception e){
			this.server.writeToLog(Level.SEVERE, "Cassandra DBHandler deleteMetric>> "+e);
		}
	}
	
	public void insertMetricValue(MetricObj metric){
		try{
			BoundStatement bs = this.insertMetricValueStmt.bind();
			bs.setString("metricID", metric.getMetricID());
			bs.setString("event_date", new SimpleDateFormat("yyyy-MM-dd").format(new Date(metric.getTimestamp())));
			bs.setUUID("event_timestamp", UUIDs.endOf(metric.getTimestamp()));
			bs.setString("value", metric.getValue());
			bs.setString("name", metric.getName());
			bs.setString("mgroup",metric.getGroup());
			bs.setString("type", metric.getType());
			bs.setString("units", metric.getUnits());
			session.execute(bs);
		}
		catch(Exception e){
			this.server.writeToLog(Level.SEVERE, "Cassandra DBHandler insertMetricValue>> "+e);
		}
	}
	
	public void insertBatchMetricValues(ArrayList<MetricObj> metriclist){
		try{
			StringBuilder sb = new StringBuilder();
			sb.append("BEGIN BATCH ");
			for(MetricObj metric : metriclist){
				
				sb.append("INSERT INTO metric_value_table (metricID, event_date, event_timestamp, value, name, mgroup, type, units) VALUES (");
				sb.append("'"+metric.getMetricID()+"'");
				sb.append(",'"+new SimpleDateFormat("yyyy-MM-dd").format(new Date(metric.getTimestamp()))+"'");
				sb.append(","+UUIDs.endOf(metric.getTimestamp()));
				sb.append(",'"+metric.getValue()+"'");
				sb.append(",'"+metric.getName()+"'");
				sb.append(",'" +metric.getGroup()+"'");
				sb.append(",'"+metric.getType()+"'");
				sb.append(",'"+metric.getUnits()+"'");
				sb.append(") USING TTL 2592000 ");
			}
			sb.append(" APPLY BATCH;");
			session.execute(sb.toString());
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void doQuery(String cql, boolean print){
		try{
			ResultSet rs = session.execute(cql);
			if (print){
				for (Row row : rs) {
					System.out.println(row.toString());
				}
			}
		}
		catch(Exception e){
			e.printStackTrace(); 
		}
	}

	public void createSubscription(SubObj sub, MetricObj metric) {
		try{
			BoundStatement bs = this.createSubStmt.bind();
			bs.setString("subID", sub.getSubID());
			bs.setString("metricID", metric.getMetricID());
			bs.setString("func", sub.getGroupingFunc().name());
			bs.setString("originMetric", sub.getOriginMetric());
			bs.setInt("period", sub.getPeriod());
			bs.setString("name", metric.getName());
			bs.setString("mgroup",metric.getGroup());
			bs.setString("type", metric.getType());
			bs.setString("units", metric.getUnits());
			session.execute(bs);	
			
//			bs = this.insertMetricStmt.bind();
//			bs.setString("agentID", metric.getAgentID());
//			bs.setString("metricID", metric.getMetricID());
//			bs.setString("name", metric.getName());
//			bs.setString("mgroup",metric.getGroup());
//			bs.setString("type", metric.getType());
//			bs.setString("units", metric.getUnits());
//			session.execute(bs);
			
			bs = this.addAgentToSubStmt.bind();
			String subID = sub.getSubID();
			for(String agentID:sub.getAgentList()){
				bs.setString("subID", subID);
				bs.setString("agentID", agentID);
				bs.setString("agentIP",this.server.getAgentMap().get(agentID).getAgentIP());
				session.execute(bs);
			}			
		} 
		catch(Exception e){
			this.server.writeToLog(Level.SEVERE, "Cassandra DBHandler createSubscription>> "+e);
		}
	}

	public void deleteSubscription(String subID){
		try{
			BoundStatement bs = this.deleteSubStmt.bind();
			bs.setString("subID", subID);
			session.execute(bs);
			
			bs = this.removeAllAgentsSubStmt.bind();
			bs.setString("subID", subID);
			session.execute(bs);
		} 
		catch(Exception e){
			this.server.writeToLog(Level.SEVERE, "Cassandra DBHandler deleteSubscription>> "+e);
		}			
	}

	public void addAgentToSub(String subID, String agentID){
		try{
			BoundStatement bs = this.addAgentToSubStmt.bind();
			bs.setString("subID", subID);
			bs.setString("agentID", agentID);
			bs.setString("agentIP",this.server.getAgentMap().get(agentID).getAgentIP());
			session.execute(bs);			
		} 
		catch(Exception e){
			this.server.writeToLog(Level.SEVERE, "Cassandra DBHandler addAgentToSub>> "+e);
		}		
	}

	public void removeAgentFromSub(String subID, String agentID) {
		try{
			BoundStatement bs = this.removeAgentFromSubStmt.bind();
			bs.setString("subID", subID);
			bs.setString("agentID", agentID);
			session.execute(bs);			
		} 
		catch(Exception e){
			this.server.writeToLog(Level.SEVERE, "Cassandra DBHandler removeAgentFromSub>> "+e);
		}				
	}
}
