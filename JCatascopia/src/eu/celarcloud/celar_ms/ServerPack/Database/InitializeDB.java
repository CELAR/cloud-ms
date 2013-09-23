package eu.celarcloud.celar_ms.ServerPack.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ServerPack.IJCatascopiaServer;

public class InitializeDB {
	
	public static void createTables(Connection conn, boolean drop_tables, IJCatascopiaServer server) throws CatascopiaException{
		final String[] tables = {"agent_table","metric_table","metric_value_table","subscription_table","subscription_agents_table"};
		
		String query = "";
        PreparedStatement stmt = null;
        try {
        	if(drop_tables){
	        	for(String table:tables){
	        		query = "DROP TABLE IF EXISTS "+table;
	            	stmt = conn.prepareStatement(query);
					stmt.executeUpdate();
//					System.out.println("dropped table: "+table);
					server.writeToLog(Level.INFO, "InitializeDB>> dropped table: "+table);
	        	}
        	}
			query = "CREATE TABLE IF NOT EXISTS `agent_table` (" +
					"`agentID` varchar(32) NOT NULL," +
					"`agentIP` varchar(16) NOT NULL," +
					"`status` enum('UP','DOWN','DEAD') NOT NULL," +
					"PRIMARY KEY (`agentID`)" +
					") ENGINE=InnoDB DEFAULT CHARSET=latin1;";
			stmt = conn.prepareStatement(query);
			stmt.executeUpdate();
//			System.out.println("created table: "+"agent_table");
			server.writeToLog(Level.INFO, "InitializeDB>> created table: agent_table");
			
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
			stmt = conn.prepareStatement(query);
			stmt.executeUpdate();
//			System.out.println("created table: "+"metric_table");
			server.writeToLog(Level.INFO, "InitializeDB>> created table: metric_table");
			
			query = "CREATE TABLE IF NOT EXISTS `metric_value_table` (" +
					"`valueID` bigint(20) unsigned NOT NULL AUTO_INCREMENT," +
					"`metricID` varchar(64) NOT NULL," +
					"`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					"`value` varchar(32) NOT NULL," +
					"PRIMARY KEY (`valueID`)) " +
					"ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;";
			stmt = conn.prepareStatement(query);
			stmt.executeUpdate();
//			System.out.println("created table: "+"metric_value_table");
			server.writeToLog(Level.INFO, "InitializeDB>> created table: metric_value_table");
			
			query = "CREATE TABLE IF NOT EXISTS `subscription_table` (" +
					"`subID` varchar(32) NOT NULL, " +
					"`func` varchar(256) NOT NULL, " +
					"`period` int(11) NOT NULL, " +
					"`originMetric` varchar(50) NOT NULL,"+
					"PRIMARY KEY (`subID`)) " +
					"ENGINE=InnoDB DEFAULT CHARSET=latin1;";
			stmt = conn.prepareStatement(query);
			stmt.executeUpdate();
//			System.out.println("created table: "+"subscription_table");
			server.writeToLog(Level.INFO, "InitializeDB>> created table: subscription_table");
			
			query = "CREATE TABLE IF NOT EXISTS `subscription_agents_table` (" +
					" `subID` varchar(32) NOT NULL," +
					" `agentID` varchar(32) NOT NULL," +
					" PRIMARY KEY (`subID`,`agentID`)) " +
					"ENGINE=InnoDB DEFAULT CHARSET=latin1;";
			stmt = conn.prepareStatement(query);
			stmt.executeUpdate();
//			System.out.println("created table: "+"subscription_agents_table");
			server.writeToLog(Level.INFO, "InitializeDB>> created table: subscription_agents_table");
			
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
}
