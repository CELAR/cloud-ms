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
package eu.celarcloud.jcatascopia.web.queryMaster.database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.dbcp.BasicDataSource;

import eu.celarcloud.jcatascopia.web.queryMaster.beans.AgentObj;
import eu.celarcloud.jcatascopia.web.queryMaster.beans.MetricObj;
import eu.celarcloud.jcatascopia.web.queryMaster.beans.SubscriptionObj;
import eu.celarcloud.jcatascopia.web.queryMaster.database.IDBInterface;
import eu.celarcloud.jcatascopia.web.utils.Dealer;

/**
 * DBHandler with Connection Pool
 * @author Demetris Trihinas
 *
 */
public class DBInterfaceWithConnPool implements IDBInterface{
	private BasicDataSource dataSource = null;
	private Connection conn = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	
	private String host;
	private String user;
	private String pass;
	private String database;
	private int CONN_NUM;
	
	private static final String GET_AGENT_AVAILABLE_METRICS = "SELECT metricID,name,mgroup,units,type FROM metric_table WHERE agentID=?";
//	private static final String GET_METRIC_VALUES_BY_TIMERANGE = "SELECT m1.metricID,m1.value,m1.timestamp, m2.name, m2.type, m2.units, m2.mgroup FROM metric_table as m2 JOIN (SELECT * FROM metric_value_table WHERE metricID=? AND timestamp>=? AND timestamp<=?) as m1 ON m1.metricID=m2.metricID;";
	private static final String GET_METRIC_VALUES_BY_TIMERANGE = "SELECT * FROM metric_value_table WHERE metricID=? AND timestamp>=? AND timestamp<=?";
	private static final String GET_AVAILABLE_METRICS_FOR_SUBS = "SELECT metricID,name,mgroup,units,type FROM metric_table WHERE ((is_sub is NULL) AND (type='INTEGER' OR type='DOUBLE')) GROUP BY name";
	private static final String GET_SUBSCRIPTIONS = "SELECT a.subID, b.name FROM subscription_table a JOIN metric_table b ON a.subID = b.agentID";
	private static final String GET_SUB_META = "SELECT * FROM subscription_table a JOIN metric_table b ON a.subID = b.agentID WHERE a.subID =?";
	private static final String GET_SUB_AGENTS = 	"SELECT b.agentID, c.agentIP FROM subscription_table a LEFT JOIN subscription_agents_table b ON a.subID = b.subID AND a.subID =? JOIN agent_table c ON b.agentID = c.agentID";

	public DBInterfaceWithConnPool(String host, String user, String pass, String database, int cnum){
		this.host = host;
		this.user = user;
		this.pass = pass;
		this.database = database;
		this.CONN_NUM = cnum;
	}
	
	public DBInterfaceWithConnPool(List<String> endpoints, String user, String pass, String database, Integer cnum){
		this.host = endpoints.get(0);
		this.user = user;
		this.pass = pass;
		this.database = database;
		this.CONN_NUM = cnum;
	}
	
	public DBInterfaceWithConnPool(List<String> endpoints, String user, String pass, String database){
		this(endpoints, user, pass, database, new Integer(1));
	}
	
	public void dbConnect(){
		try {
	        dataSource = new BasicDataSource();
	        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
	        dataSource.setUrl("jdbc:mysql://"+host+"/"+database);
	        dataSource.setUsername(user);
	        dataSource.setPassword(pass);
	        dataSource.setMaxActive(CONN_NUM);
	        //dataSource.setMinIdle(CONN_NUM/2);
	        dataSource.setMinIdle(CONN_NUM);
	        dataSource.setInitialSize(CONN_NUM);
	        
	        System.out.println("MySQL DBInterface>> creating a connection...");
	        	    
		    //connected, but does database exist?
	        conn = dataSource.getConnection();
		    resultSet = conn.getMetaData().getCatalogs();
		    boolean found = false;
		    while (resultSet.next()) {
		    	String databaseName = resultSet.getString(1);
		        if(databaseName.equals(database)){
		        	System.out.println("MySQL DBInterface>> Connected to database...");
		        	found = true;
		        }
		    }
		    if(!found)
		    	System.out.println("MySQL DBInterface>> database doesn't exist...");
		    this.resultSet.close();
		} 
		catch(SQLException e) {
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{conn.close();} catch (SQLException e) {e.printStackTrace();}
		}
	}
	
	public void dbClose(){
	    try{
	    	if (resultSet != null)
	    		resultSet.close();
	    	if (statement != null)
	    		statement.close();
	    	if (conn != null)
	    		conn.close();
	    }
	    catch (Exception e){
			e.printStackTrace();
	    }
	}
	
	private Connection getConnection(){
		Connection c = null;
		try {
			c = this.dataSource.getConnection();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	private void release(PreparedStatement stmt, Connection conn){
		try{
			if (stmt != null)
    			stmt.close();
			if (conn != null)
				conn.close();
		}
		catch (SQLException e){
			e.printStackTrace();
		} 
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public ArrayList<AgentObj> getAgents(String status) {
		String query = "SELECT * FROM agent_table";
        PreparedStatement stmt = null;
		Connection c = null;
        try{
        	query += (status == null || status.length() == 0) ? "" : " WHERE status='" + status + "'";
        	
        	c = this.getConnection();
   			stmt = c.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            ArrayList<AgentObj> agentlist = new ArrayList<AgentObj>();
	        AgentObj agent;
            while(rs.next()){
            	agent = new AgentObj(rs.getString("agentID"),rs.getString("agentIP"),rs.getString("status"));
            	agent.setAgentName(rs.getString("agentName"));
				agent.setTags(rs.getString("tags"));
            	agentlist.add(agent);
            }
            return agentlist;
		}
        catch (SQLException e){
			e.printStackTrace();
		}
        catch (Exception e){
        	e.printStackTrace();
        }
        finally{
        	this.release(stmt, c);	
        }
        return null;
	}
	
	public ArrayList<AgentObj> getAgentsWithTimestamps(String status) {
		String query = "SELECT * FROM agent_table";
        PreparedStatement stmt = null;
		Connection c = null;
        try{
        	query += (status == null || status.length() == 0) ? "" : " WHERE status='" + status + "'";
        	
        	c = this.getConnection();
   			stmt = c.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            ArrayList<AgentObj> agentlist = new ArrayList<AgentObj>();
            String tstart;
	        AgentObj agent;
            while(rs.next()){
            	agent = new AgentObj(rs.getString("agentID"),rs.getString("agentIP"),rs.getString("status"));
            	agentlist.add(agent);
            	tstart = Long.toString(rs.getTimestamp("tstart").getTime()/1000);
        		agent.setTstart(tstart);
            	if (rs.getTimestamp("tstop") != null)
            		agent.setTstop(Long.toString(rs.getTimestamp("tstop").getTime()/1000));
            	agent.setAgentName(rs.getString("agentName"));
				agent.setTags(rs.getString("tags"));
            }
            return agentlist;
		}
        catch (SQLException e){
			e.printStackTrace();
		}
        catch (Exception e){
        	e.printStackTrace();
        }
        finally{
        	this.release(stmt, c);	
        }
        return null;
	}
	

	/**
	 * Connects to the given database and retrieves a metadata list of all the available
	 * metrics for the given agent 
	 */
	public ArrayList<MetricObj> getAgentAvailableMetrics(String agentID) {
		PreparedStatement stmt = null;
		Connection c = null;
	    try{
	    	c = this.getConnection();
			stmt = c.prepareStatement(GET_AGENT_AVAILABLE_METRICS);
			stmt.setString(1, agentID);
	        ResultSet rs = stmt.executeQuery();
	        ArrayList<MetricObj> metriclist = new ArrayList<MetricObj>();
	        while (rs.next())
	        	metriclist.add(new MetricObj(rs.getString("metricID"), rs.getString("name"),
    							             rs.getString("units"),rs.getString("type"),rs.getString("mgroup")));
	        return metriclist;
		}
        catch(SQLException e){
			e.printStackTrace();
		}
        catch(Exception e){
        	e.printStackTrace();
        }
        finally{
        	this.release(stmt, c);	
        }
	        return null;
	}

	/**
	 * Connects to the given database and retrieves the latest value 
	 * stored in the database for each of the given metrics
	 */
	public ArrayList<MetricObj> getMetricValues(String[] registeredMetrics) {
		if(registeredMetrics==null || registeredMetrics.length==0) return null;
		
		StringBuilder query = new StringBuilder();
//		query.append("SELECT m1.*, m3.name, m3.units, m3.type, m3.mgroup FROM metric_value_table as m1 ");
		query.append("SELECT m1.* FROM metric_value_table as m1 ");
		query.append("JOIN (SELECT MAX(valueID) as latest FROM metric_value_table GROUP BY metricID) as m2 ON m1.valueID = latest ");
//		query.append("JOIN (SELECT * FROM metric_table ");
		query.append("WHERE ");
		
		boolean first = true; // where clause building
		for(String s: registeredMetrics){
			if(!first) query.append("|| ");
			query.append("metricID =  '"+ s +"' ");
			first = false;
		}
//		query.append(") as m3 ON m1.metricID=m3.metricID");
		
		PreparedStatement stmt = null;
		Connection c = null;
		try {
			c = this.getConnection();
			stmt = c.prepareStatement(query.toString());
            ResultSet rs = stmt.executeQuery();
            ArrayList<MetricObj> metriclist = new ArrayList<MetricObj>(); String t = "";
            while (rs.next()){
            	t = rs.getString("timestamp").split(" ")[1].replace(".0", "");
            	metriclist.add(new MetricObj(rs.getString("metricID"), rs.getString("name"),
            					rs.getString("units"),rs.getString("type"),rs.getString("mgroup"),rs.getString("value"),t));
            }
            return metriclist;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
        catch(Exception e){
        	e.printStackTrace();
        }
		finally{
        	this.release(stmt, c);	
        }
		return null;
	}

	/**
	 * Connects to the given database and retrieves all the values of the given
	 * metric from x seconds before until now
	 */
	public ArrayList<MetricObj> getMetricValuesByTime(String metricID,long interval) {
		String interv = String.valueOf(interval);
		StringBuilder query = new StringBuilder();
		if (interv != null){
//			query.append("SELECT m1.metricID,m1.value,m1.timestamp, m2.name, m2.type, m2.units, m2.mgroup FROM metric_table as m2 ");
//			query.append("JOIN (SELECT * FROM metric_value_table WHERE metricID='"+metricID+"' AND timestamp>DATE_SUB(now(), INTERVAL "+interval+" SECOND)) ");
//			query.append("as m1 ON m1.metricID=m2.metricID;");
			query.append("SELECT * FROM metric_value_table ");
			query.append("WHERE metricID='"+metricID+"' AND timestamp>DATE_SUB(now(), INTERVAL "+interval+" SECOND);");
		}
		else{
//			query.append("SELECT m1.*,m3.name, m3.units, m3.type, m3.mgroup FROM metric_value_table as m1 ");
//			query.append("JOIN (SELECT MAX(valueID) as latest FROM metric_value_table WHERE metricID='"+metricID+"') as m2 ON m1.valueID = latest ");
//			query.append("JOIN (SELECT * FROM metric_table) as m3 ON m1.metricID=m3.metricID");
			query.append("SELECT m1.* FROM metric_value_table as m1 ");
			query.append("JOIN (SELECT MAX(valueID) as latest FROM metric_value_table WHERE metricID='"+metricID+"') as m2 ON m1.valueID = latest ");
		}

		PreparedStatement stmt = null;
		Connection c = null;
		try {
			c = this.getConnection();
			stmt = c.prepareStatement(query.toString());
            ResultSet rs = stmt.executeQuery();
            
            ArrayList<MetricObj> metriclist = new ArrayList<MetricObj>(); String t = "";
            while (rs.next()){
            	t = rs.getString("timestamp").split(" ")[1].replace(".0", "");
            	metriclist.add(new MetricObj(rs.getString("metricID"), rs.getString("name"),
    					rs.getString("units"),rs.getString("type"),rs.getString("mgroup"),rs.getString("value"),t));
            }
            return metriclist;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
        catch(Exception e){
        	e.printStackTrace();
        }
		finally{
        	this.release(stmt, c);	
        }
		return null;

	}

	/**
	 * Connects to the given database and retrieves all the values between the given timerange
	 * for the given metric
	 * UNIX TIME
	 */
	public ArrayList<MetricObj> getMetricValuesByTime(String metricID, long start, long end){
		PreparedStatement stmt = null;
		Connection c = null;
		try {
			c = this.getConnection();
			stmt = c.prepareStatement(GET_METRIC_VALUES_BY_TIMERANGE);
			stmt.setString(1, metricID);
			stmt.setTimestamp(2, new Timestamp(start*1000));
			stmt.setTimestamp(3, new Timestamp(end*1000));

            ResultSet rs = stmt.executeQuery();
            ArrayList<MetricObj> metriclist = new ArrayList<MetricObj>();String t = "";
            while (rs.next()){
            	t = rs.getString("timestamp").split(" ")[1].replace(".0", "");
            	metriclist.add(new MetricObj(rs.getString("metricID"), rs.getString("name"),
    					rs.getString("units"),rs.getString("type"),rs.getString("mgroup"),rs.getString("value"),t));
            }
            return metriclist;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
        catch(Exception e){
        	e.printStackTrace();
        }
		finally{
        	this.release(stmt, c);	
        }
		return null;
	}

	public ArrayList<MetricObj> getAvailableMetricsForSubs(){
		PreparedStatement stmt = null;
		Connection c = null;
		try{ 
			c = this.getConnection();
			stmt = c.prepareStatement(GET_AVAILABLE_METRICS_FOR_SUBS);
			ResultSet rs = stmt.executeQuery();
			ArrayList<MetricObj> metriclist = new ArrayList<MetricObj>();
			while (rs.next())
				metriclist.add(new MetricObj(rs.getString("metricID"), rs.getString("name"),rs.getString("units"),rs.getString("type"),null));
			return metriclist;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
        	this.release(stmt, c);	
        }
		return null;
	}
	
	/**
	 * Connects to the given database and retrieves all the subscriptions.
	 * @return a list of all the subscriptions
	 */
	public ArrayList<SubscriptionObj> getSubscriptions(){
		PreparedStatement stmt = null;
		Connection c = null;
		try{ 
			c = this.getConnection();
			stmt = c.prepareStatement(GET_SUBSCRIPTIONS);			
			ResultSet rs = stmt.executeQuery();
			ArrayList<SubscriptionObj> subsList = new ArrayList<SubscriptionObj>();
			while(rs.next())
				subsList.add(new SubscriptionObj(rs.getString("subID"),rs.getString("name"), null,null));
			return subsList;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
        	this.release(stmt, c);	
        }
		return null;
	}
	
	/**
	 * Connects to the given database and retrieves the metadata of the given subscription
	 * @param subID The subscription id
	 * @return an object with all the subscription's metadata
	 */
	public SubscriptionObj getSubMeta(String subID){
		PreparedStatement stmt = null;
		Connection c = null;
		try{ 
			c = this.getConnection();
			stmt = c.prepareStatement(GET_SUB_META);
			stmt.setString(1, subID);
			ResultSet rs = stmt.executeQuery();
			if(rs.next())
				return new SubscriptionObj(rs.getString("subID"),rs.getString("name"),rs.getString("func"),rs.getString("period"),
						                    rs.getString("originMetric"),rs.getString("metricID"),rs.getString("type"),rs.getString("units"),rs.getString("mgroup"));
			else return null;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
        	this.release(stmt, c);	
        }
		return null;
	}
	
	public ArrayList<AgentObj> getAgentsForSub(String subID){
		PreparedStatement stmt = null;
		Connection c = null;
		try{ 
			c = this.getConnection();
			stmt = c.prepareStatement(GET_SUB_AGENTS);
			stmt.setString(1, subID);
			ResultSet rs = stmt.executeQuery();
            ArrayList<AgentObj> agentlist = new ArrayList<AgentObj>();
            while(rs.next())
            	agentlist.add(new AgentObj(rs.getString("agentID"),rs.getString("agentIP"),null));
            return agentlist;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
        	this.release(stmt, c);	
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
	
	public ArrayList<MetricObj> getAvailableMetricsForAllAgents(){return null;}

}
