package eu.celarcloud.celar_ms.ServerPack.Database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.dbcp.BasicDataSource;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ServerPack.IJCatascopiaServer;
import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;
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
	
	private static final String CREATE_AGENT = "INSERT INTO agent_table (agentID,agentIP,status) VALUES (?,?,?)";
	private static final String UPDATE_AGENT = "UPDATE agent_table SET status=? WHERE agentID=?";
	private static final String DELETE_AGENT = "DELETE FROM agent_table WHERE (agentID = ?)";
	private static final String CREATE_METRIC = "INSERT INTO metric_table (metricID,agentID,name,mgroup,units,type) VALUES (?,?,?,?,?,?)";
	private static final String DELETE_METRIC = "DELETE FROM metric_table WHERE (metricID = ?) ";
	private static final String INSERT_METRIC_VALUE = "INSERT INTO metric_value_table (metricID,timestamp,value) VALUES (?,?,?)";	
	
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
					"`status` enum('UP','DOWN','DEAD') NOT NULL," +
					"PRIMARY KEY (`agentID`)" +
					") ENGINE=InnoDB DEFAULT CHARSET=latin1;";
			stmt = c.prepareStatement(query);
			stmt.executeUpdate();
			server.writeToLog(Level.INFO, "MySQL DBHandler>> created table: agent_table");
			
			query = "CREATE TABLE IF NOT EXISTS `metric_table` (" +
			        "`metricID` varchar(64) NOT NULL," +
			        "`agentID` varchar(32) DEFAULT NULL," +
			        "`name` varchar(50) NOT NULL," +
			        "`mgroup` varchar(50) NOT NULL," +
			        "`units` varchar(10) NOT NULL," +
			        "`type` varchar(20) NOT NULL," +
			        "`subID` varchar(32) DEFAULT NULL,"+
			        "PRIMARY KEY (`metricID`)" +
			        ") ENGINE=InnoDB DEFAULT CHARSET=latin1;";
			stmt = c.prepareStatement(query);
			stmt.executeUpdate();
			server.writeToLog(Level.INFO, "MySQL DBHandler>> created table: metric_table");
			
			query = "CREATE TABLE IF NOT EXISTS `metric_value_table` (" +
					"`valueID` bigint(20) unsigned NOT NULL AUTO_INCREMENT," +
					"`metricID` varchar(64) NOT NULL," +
					"`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					"`value` varchar(32) NOT NULL," +
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
			stmt.executeUpdate();
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
	    	stmt = c.prepareStatement(UPDATE_AGENT);
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

	public void doQuery(String cql, boolean print) {
		// TODO Auto-generated method stub
	}
}
