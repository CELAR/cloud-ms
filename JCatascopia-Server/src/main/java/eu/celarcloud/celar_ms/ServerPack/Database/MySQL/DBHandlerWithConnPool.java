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
package eu.celarcloud.celar_ms.ServerPack.Database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.dbcp.BasicDataSource;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ServerPack.IJCatascopiaServer;
import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.SubObj;
import eu.celarcloud.celar_ms.ServerPack.Database.IDBHandler;

/**
 * MySQL DBHandler with Connection Pool
 * 
 * @author Demetris Trihinas
 */
public class DBHandlerWithConnPool implements IDBHandler{
	private BasicDataSource dataSource = null;
	private Connection conn = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	
	private IJCatascopiaServer server;
	private String host;
	private String user;
	private String pass;
	private String database;
	private int CONN_NUM;
	
	private static final String CREATE_AGENT = "INSERT INTO agent_table (agentID,agentIP,status,agentName,tags) VALUES (?,?,?,?,?)";
	private static final String UPDATE_AGENT = "UPDATE agent_table SET status=?,tstop=NULL WHERE agentID=?";
	private static final String UPDATE_AGENT_TERMINATED = "UPDATE agent_table SET status=?,tstop=CURRENT_TIMESTAMP WHERE agentID=?";
	private static final String DELETE_AGENT = "DELETE FROM agent_table WHERE (agentID = ?)";
	private static final String CREATE_METRIC = "INSERT INTO metric_table (metricID,agentID,name,mgroup,units,type) VALUES (?,?,?,?,?,?)";
	private static final String DELETE_METRIC = "DELETE FROM metric_table WHERE (metricID = ?) ";
	//private static final String INSERT_METRIC_VALUE = "INSERT INTO metric_value_table (metricID,timestamp,value) VALUES (?,?,?)";	
	private static final String INSERT_METRIC_VALUE = "INSERT INTO metric_value_table (metricID,timestamp,value,name,mgroup,type,units) VALUES (?,?,?,?,?,?,?)";	
	
	private static final String CREATE_SUBSCRIPTION = "INSERT INTO subscription_table (subID,func,originMetric,period) VALUES (?,?,?,?);";
	private static final String CREATE_METRIC_FOR_SUB = "INSERT INTO metric_table (metricID,agentID,name,mgroup,units,type,is_sub) VALUES (?,?,?,?,?,?,?)";
	private static final String ADD_AGENT_TO_SUB = "INSERT INTO subscription_agents_table (subID,agentID) VALUES (?,?);";
	private static final String DELETE_SUB = "DELETE FROM subscription_table WHERE (subID=?) ";
	private static final String DELETE_SUB_METRIC = "DELETE FROM metric_table WHERE (agentID=?) ";
	private static final String REMOVE_ALL_AGENTS_FROM_SUB = "DELETE FROM subscription_agents_table WHERE (subID=?) ";
	private static final String REMOVE_AGENT_FROM_SUB = "DELETE FROM subscription_agents_table WHERE (subID =? AND agentID =?) ";

	public DBHandlerWithConnPool(List<String> endpoints, String user, String pass, String database, Integer cnum, IJCatascopiaServer server){
		this.server = server;
		this.host = endpoints.get(0);
		this.user = user;
		this.pass = pass;
		this.database = database;
		this.CONN_NUM = cnum;
	}
	
	public DBHandlerWithConnPool(List<String> endpoints, String user, String pass, String database, IJCatascopiaServer server){
		this(endpoints, user, pass, database, new Integer(1), server);
	}
	
	public void dbConnect(){
		boolean con = false;
		int interval = 20000;
		int max = 600000; //after 10min shut everything down
		int t = 0;
		
		while (con == false && t < max){
			try{
				this.dbConnect1();
			}
			catch(Exception e){
				String s = "No Database backend available, retry to connect in "+interval/1000+" seconds";
				System.out.println(s);
				this.server.writeToLog(Level.WARNING, e.getMessage() + " " + s);
				con = false;
				t += interval;
				try {
					Thread.sleep(interval);
				} 
				catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}
			con = true;
		}
		//if there is still no connection after 10mins, try again and this time don't catch the error
		if (con == false)
			this.dbConnect1();
	}
	
	public void dbConnect1(){
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
	        
//	        System.out.println("Creating a connection...");
			this.server.writeToLog(Level.INFO, "MySQL DBHandler>> creating a connection...");
	        	    
		    //connected, but does database exist?
	        conn = dataSource.getConnection();
		    resultSet = conn.getMetaData().getCatalogs();
		    boolean found = false;
		    while (resultSet.next()) {
		    	String databaseName = resultSet.getString(1);
		        if(databaseName.equals(database)){
//		        	System.out.println("Database exists...");
					this.server.writeToLog(Level.INFO, "MySQL DBHandler>> Connected to JCatascopia DB...");
		        	found = true;
		        }
		    }
		    if(!found)
//		    	System.out.println("Database doesn't exist...");
				this.server.writeToLog(Level.INFO, "DBHandler>> database doesn't exist...");
		    this.resultSet.close();
		} 
		catch(SQLException e) {
			this.server.writeToLog(Level.SEVERE, e);
		}
		catch(Exception e){
			this.server.writeToLog(Level.SEVERE, e);
		}
		finally{
			try{conn.close();} catch (SQLException e) {this.server.writeToLog(Level.SEVERE, e);}
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
			this.server.writeToLog(Level.SEVERE, e);
	    }
	}
	
	private Connection getConnection(){
		Connection c = null;
		try {
			c = this.dataSource.getConnection();
		} 
		catch (Exception e) {
			this.server.writeToLog(Level.SEVERE, e);
		}
		return c;
	}

	public void dbInit(boolean drop_tables) throws CatascopiaException{
		Connection c = null;
		String query = "";
        PreparedStatement stmt = null; 
        try{
        	c = this.getConnection();
        	if(drop_tables){
        		final String[] tables = {"agent_table","metric_table","metric_value_table","subscription_table","subscription_agents_table"};
                
	        	for(String table:tables){
	        		query = "DROP TABLE IF EXISTS "+table;
	            	stmt = c.prepareStatement(query);
					stmt.executeUpdate();
					server.writeToLog(Level.INFO, "MySQL DBHandler>> dropped table: "+table);
	        	}
        	}
			query = "CREATE TABLE IF NOT EXISTS `agent_table` (" +
					"`agentID` varchar(32) NOT NULL," +
					"`agentIP` varchar(16) NOT NULL," +
					"`agentName` varchar(16) NOT NULL," +
					"`status` enum('UP','DOWN','TERMINATED') NOT NULL," +
					"`tstart` timestamp DEFAULT CURRENT_TIMESTAMP," +
					"`tstop` timestamp NULL," +
					"`tags` varchar(64) NULL," +
					"PRIMARY KEY (`agentID`)" +
					") ENGINE=InnoDB DEFAULT CHARSET=latin1;";
			stmt = c.prepareStatement(query);
			stmt.executeUpdate();
			server.writeToLog(Level.INFO, "MySQL DBHandler>> created table: agent_table");
			
			query = "CREATE TABLE IF NOT EXISTS `metric_table` (" +
			        "`metricID` varchar(64) NOT NULL," +
			        "`agentID` varchar(32) NOT NULL," +
			        "`name` varchar(50) NOT NULL," +
			        "`mgroup` varchar(50) NOT NULL," +
			        "`units` varchar(10) NOT NULL," +
			        "`type` varchar(20) NOT NULL," +
			        "`is_sub` varchar(10) DEFAULT NULL,"+
			        "PRIMARY KEY (`metricID`)" +
			        ") ENGINE=InnoDB DEFAULT CHARSET=latin1;";
			stmt = c.prepareStatement(query);
			stmt.executeUpdate();
			server.writeToLog(Level.INFO, "MySQL DBHandler>> created table: metric_table");
			
//			query = "CREATE TABLE IF NOT EXISTS `metric_value_table` (" +
//					"`valueID` bigint(20) unsigned NOT NULL AUTO_INCREMENT," +
//					"`metricID` varchar(64) NOT NULL," +
//					"`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
//					"`value` varchar(32) NOT NULL," +
//					"PRIMARY KEY (`valueID`)) " +
//					"ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;";
			query = "CREATE TABLE IF NOT EXISTS `metric_value_table` (" +
					"`valueID` bigint(20) unsigned NOT NULL AUTO_INCREMENT," +
					"`metricID` varchar(64) NOT NULL," +
					"`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					"`value` varchar(32) NOT NULL," +
					 "`name` varchar(50) NOT NULL," +
			        "`mgroup` varchar(50) NOT NULL," +
			        "`units` varchar(10) NOT NULL," +
			        "`type` varchar(20) NOT NULL," +
					"PRIMARY KEY (`valueID`)) " +
					"ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;";
			
			stmt = c.prepareStatement(query);
			stmt.executeUpdate();
			server.writeToLog(Level.INFO, "MySQL DBHandler>> created table: metric_value_table");
			
			query = "CREATE TABLE IF NOT EXISTS `subscription_table` (" +
					"`subID` varchar(32) NOT NULL, " +
					"`func` varchar(256) NOT NULL, " +
					"`period` int(11) NOT NULL, " +
					"`originMetric` varchar(50) NOT NULL,"+
					"PRIMARY KEY (`subID`)) " +
					"ENGINE=InnoDB DEFAULT CHARSET=latin1;";
			stmt = c.prepareStatement(query);
			stmt.executeUpdate();
			server.writeToLog(Level.INFO, "MySQL DBHandler>> created table: subscription_table");
			
			query = "CREATE TABLE IF NOT EXISTS `subscription_agents_table` (" +
					" `subID` varchar(32) NOT NULL," +
					" `agentID` varchar(32) NOT NULL," +
					" PRIMARY KEY (`subID`,`agentID`)) " +
					"ENGINE=InnoDB DEFAULT CHARSET=latin1;";
			stmt = c.prepareStatement(query);
			stmt.executeUpdate();
			server.writeToLog(Level.INFO, "MySQL DBHandler>> created table: subscription_agents_table");	
		} 
        catch (SQLException e){
			throw new CatascopiaException(e.toString(),CatascopiaException.ExceptionType.DATABASE);
		}
        catch (Exception e){
			throw new CatascopiaException(e.toString(),CatascopiaException.ExceptionType.DATABASE);
        }
        finally{
	    	 if (stmt != null)
	    		 try{
	    			 stmt.close();
	    		 }catch (SQLException e) {
	    				throw new CatascopiaException(e.toString(),CatascopiaException.ExceptionType.DATABASE);
				 }
	    }		
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

	public void createAgent(AgentObj agent){
        PreparedStatement stmt = null;
		Connection c = null;
        try{
        	c = this.getConnection();
			stmt = c.prepareStatement(CREATE_AGENT);
			stmt.setString(1, agent.getAgentID()); 
			stmt.setString(2, agent.getAgentIP()); 
			stmt.setString(3, agent.getStatus().name());
			stmt.setString(4, agent.getAgentName()); 
			stmt.setString(5, agent.getAgentTags()); 
			stmt.executeUpdate();
		}
        catch (MySQLIntegrityConstraintViolationException e) {
        	updateAgent(agent.getAgentID(), AgentObj.AgentStatus.UP.name());	
        }
        catch (SQLException e) {
	    	server.writeToLog(Level.SEVERE, "MySQL Handler createAgent>> "+e);
		} 
	    catch (Exception e) {
	    	server.writeToLog(Level.SEVERE, "MySQL Handler createAgent>> "+e);
		} 
	    finally{
	    	this.release(stmt, c);		
        }				
	}

	public void updateAgent(String agentID, String status){
	    PreparedStatement stmt = null;
		Connection c = null;
	    try{
        	c = this.getConnection();
        	if(!status.equals("TERMINATED"))
		    	stmt = c.prepareStatement(UPDATE_AGENT);
        	else
        		stmt = c.prepareStatement(UPDATE_AGENT_TERMINATED);
	    	stmt.setString(1, status); 
	    	stmt.setString(2, agentID);
	    	stmt.executeUpdate();
	     } 
	     catch (SQLException e) {
	    	 server.writeToLog(Level.SEVERE, "MySQL Handler updateAgent>> "+e);
	     } 
	     catch (Exception e) {
	    	 server.writeToLog(Level.SEVERE, "MySQL Handler updateAgent>> "+e);
	     } 
	     finally{
	    	 this.release(stmt, c);		
	     }			
	}

	public void deleteAgent(String agentID){
		PreparedStatement stmt = null;
		Connection c = null;
	    try{
        	c = this.getConnection();
	    	stmt = c.prepareStatement(DELETE_AGENT);
	    	stmt.setString(1, agentID); 
	    	stmt.executeUpdate();
	    }
	    catch (SQLException e) {
	    	server.writeToLog(Level.SEVERE, "MySQL Handler deleteAgent>> "+e);
		} 
	    catch (Exception e) {
	    	server.writeToLog(Level.SEVERE, "MySQL Handler deleteAgent>> "+e);
		} 
	    finally{
	    	this.release(stmt, c);		
        }			
	}

	public void createMetric(MetricObj metric){
		PreparedStatement stmt = null;
		Connection c = null;
	    try{
        	c = this.getConnection();
			stmt = c.prepareStatement(CREATE_METRIC);
			stmt.setString(1, metric.getMetricID()); 
			stmt.setString(2, metric.getAgentID()); 
			stmt.setString(3, metric.getName()); 
			stmt.setString(4, metric.getGroup());
			stmt.setString(5, metric.getUnits()); 
			stmt.setString(6, metric.getType()); 			
			stmt.executeUpdate();
		 }
	     catch (SQLException e) {
			server.writeToLog(Level.SEVERE, "MySQL Handler createMetric>> "+e);
		 } 
	     catch (Exception e) {
			server.writeToLog(Level.SEVERE, "MySQL Handler createMetric>> "+e);
		 } 
	     finally{
	    	 this.release(stmt, c);		
         }			
	}

	public void deleteMetric(String agentID, String metricID) {
		PreparedStatement stmt = null;
		Connection c = null;
	    try{
	    	c = this.getConnection();
	    	stmt = c.prepareStatement(DELETE_METRIC);
	    	stmt.setString(1, metricID); 
			stmt.executeUpdate();		
	    }
	    catch (SQLException e) {
			server.writeToLog(Level.SEVERE, "MySQL Handler deleteMetric>> "+e);
		} 
	    catch (Exception e) {
			server.writeToLog(Level.SEVERE, "MySQL Handler deleteMetric>> "+e);
		} 
	    finally{
	    	 this.release(stmt, c);		
        }			
	}

	public void insertMetricValue(MetricObj metric) {
		PreparedStatement stmt = null;
		Connection c = null;
	    try{
	    	c = this.getConnection();
			stmt = c.prepareStatement(INSERT_METRIC_VALUE);
			stmt.setString(1, metric.getMetricID()); 
			stmt.setTimestamp(2, new java.sql.Timestamp(metric.getTimestamp())); 
			stmt.setString(3, metric.getValue()); 
			
			stmt.setString(4, metric.getName()); 
			stmt.setString(5, metric.getGroup());
			stmt.setString(6, metric.getUnits()); 
			stmt.setString(7, metric.getType());
			
			stmt.executeUpdate();		
	    }
	    catch (SQLException e) {
	    	server.writeToLog(Level.SEVERE, "MySQL Handler insertMetricValue>> "+e);
		} 
		catch (Exception e) {
			server.writeToLog(Level.SEVERE, "MySQL Handler insertMetricValue>> "+e);
		} 
		finally{
			this.release(stmt, c);		
	    }			
	}
	
	public void insertBatchMetricValues(ArrayList<MetricObj> metriclist) {
		PreparedStatement stmt = null;
		Connection c = null;
	    try{
	    	c = this.getConnection();
	    	stmt = c.prepareStatement(INSERT_METRIC_VALUE);
	    	for(MetricObj metric : metriclist){
	    		stmt.setString(1, metric.getMetricID()); 
				stmt.setTimestamp(2, new java.sql.Timestamp(metric.getTimestamp())); 
				stmt.setString(3, metric.getValue()); 
				stmt.addBatch();
	    	}
	    	stmt.executeBatch();
	    }
	    catch (SQLException e) {
	    	server.writeToLog(Level.SEVERE, "MySQL Handler insertBatchMetricValues>> "+e);
		} 
		catch (Exception e) {
			server.writeToLog(Level.SEVERE, "MySQL Handler insertBatchMetricValues>> "+e);
		} 
		finally{
			this.release(stmt, c);		
	    }						
		
	}
	
	public void createSubscription(SubObj sub, MetricObj metric){
		PreparedStatement stmt = null;
		Connection c = null;
	    try{
	    	c = this.getConnection();
	    	stmt = c.prepareStatement(CREATE_SUBSCRIPTION);
			stmt.setString(1, sub.getSubID()); 
			stmt.setString(2, sub.getGroupingFunc().name()); 
			stmt.setString(3, sub.getOriginMetric()); 
			stmt.setInt(4, sub.getPeriod());
			stmt.executeUpdate();
			
			stmt = c.prepareStatement(CREATE_METRIC_FOR_SUB);
			stmt.setString(1, metric.getMetricID()); 
			stmt.setString(2, metric.getAgentID()); 
			stmt.setString(3, metric.getName()); 
			stmt.setString(4, metric.getGroup());
			stmt.setString(5, metric.getUnits()); 
			stmt.setString(6, metric.getType()); 
			stmt.setString(7, "yes");
			stmt.executeUpdate();
			
			stmt = c.prepareStatement(ADD_AGENT_TO_SUB);
			String subID = sub.getSubID();
			for(String agentID:sub.getAgentList()){
				stmt.setString(1,subID);
				stmt.setString(2, agentID);
				stmt.addBatch();
			}
			stmt.executeBatch();
	    }
	    catch (SQLException e) {
	    	server.writeToLog(Level.SEVERE, "MySQL Handler createSubscription>> "+e);
		} 
		catch (Exception e) {
			server.writeToLog(Level.SEVERE, "MySQL Handler createSubscription>> "+e);
		} 
		finally{
			this.release(stmt, c);		
	    }			
	}
	
	public void deleteSubscription(String subID){
		PreparedStatement stmt = null;
		Connection c = null;
	    try{
	    	c = this.getConnection();
	    	stmt = c.prepareStatement(DELETE_SUB);
	    	stmt.setString(1, subID); 
	    	stmt.executeUpdate();
	    	
	    	stmt = c.prepareStatement(DELETE_SUB_METRIC);
	    	stmt.setString(1, subID); 
	    	stmt.executeUpdate();
	        
	    	stmt = c.prepareStatement(REMOVE_ALL_AGENTS_FROM_SUB);
	    	stmt.setString(1, subID); 
	    	stmt.executeUpdate();
	    }
	    catch (SQLException e) {
	    	server.writeToLog(Level.SEVERE, "MySQL Handler deleteSubscription>> "+e);
		} 
		catch (Exception e) {
			server.writeToLog(Level.SEVERE, "MySQL Handler deleteSubscription>> "+e);
		} 
		finally{
			this.release(stmt, c);		
	    }			
	}
	
	public void addAgentToSub(String subID, String agentID){
		PreparedStatement stmt = null;
		Connection c = null;
	    try{
	    	c = this.getConnection();
	    	stmt = c.prepareStatement(ADD_AGENT_TO_SUB);
	    	stmt.setString(1, subID); 
	    	stmt.setString(2, agentID);
	    	stmt.executeUpdate();
	    }
	    catch (SQLException e) {
	    	server.writeToLog(Level.SEVERE, "MySQL Handler addAgentToSub>> "+e);
		} 
		catch (Exception e) {
			server.writeToLog(Level.SEVERE, "MySQL Handler addAgentToSub>> "+e);
		} 
		finally{
			this.release(stmt, c);		
	    }			
	}
	
	public void removeAgentFromSub(String subID, String agentID){
		PreparedStatement stmt = null;
		Connection c = null;
	    try{
	    	c = this.getConnection();
	    	stmt = c.prepareStatement(REMOVE_AGENT_FROM_SUB);
	    	stmt.setString(1, subID); 
	    	stmt.setString(2, agentID);
	    	stmt.executeUpdate();
	    }
	    catch (SQLException e) {
	    	server.writeToLog(Level.SEVERE, "MySQL Handler removeAgentToSub>> "+e);
		} 
		catch (Exception e) {
			server.writeToLog(Level.SEVERE, "MySQL Handler removeAgentToSub>> "+e);
		} 
		finally{
			this.release(stmt, c);		
	    }			
	}

	public void doQuery(String cql, boolean print) {
		// TODO Auto-generated method stub
	}	
}
