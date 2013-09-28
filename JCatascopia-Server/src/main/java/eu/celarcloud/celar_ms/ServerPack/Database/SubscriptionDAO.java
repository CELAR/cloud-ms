package eu.celarcloud.celar_ms.ServerPack.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.SubObj;

public class SubscriptionDAO{
	
	public static synchronized void createSubscription(Connection conn, SubObj sub, MetricObj metric){
		String query = "";
        PreparedStatement stmt = null;

        try{        	
        	query = "INSERT INTO subscription_table (subID,func,originMetric,period) VALUES (?,?,?,?);";

			stmt = conn.prepareStatement(query);
			stmt.setString(1, sub.getSubID()); 
			stmt.setString(2, sub.getGroupingFunc().name()); 
			stmt.setString(3, sub.getOriginMetric()); 
			stmt.setInt(4, sub.getPeriod());
			SubscriptionDAO.dbUpdate(conn, stmt);
			
			query = "INSERT INTO metric_table (metricID,name,mgroup,units,type,subID) VALUES (?,?,?,?,?,?);";
			stmt = conn.prepareStatement(query);
			stmt.setString(1, metric.getMetricID());
			stmt.setString(2, metric.getName());
			stmt.setString(3, metric.getGroup());
			stmt.setString(4, metric.getUnits());
			stmt.setString(5, metric.getType());
			stmt.setString(6, sub.getSubID());
			SubscriptionDAO.dbUpdate(conn, stmt);
           
			query = "INSERT INTO subscription_agents_table (subID,agentID) VALUES (?,?);";
			stmt = conn.prepareStatement(query);
			String subID = sub.getSubID();
			for(String agentID:sub.getAgentList()){
				stmt.setString(1,subID);
				stmt.setString(2, agentID);
				stmt.addBatch();
			}
			stmt.executeBatch();

//      		System.out.println("Succesfully inserted to catascopiaDB new Subscription with id: "+sub.getSubID()+" and metricID: "+metric.getMetricID());
		}
        catch (SQLException e){
			e.printStackTrace();
		}
        finally{
        	if (stmt != null)
        		try{
        			stmt.close();
				}catch (SQLException e){
					e.printStackTrace();
				} 
        }
	}
	
	public static void deleteSubscription(Connection conn, String subID){
		String query = "";
	    PreparedStatement stmt = null;
	    try{
	    	query = "DELETE FROM subscription_table WHERE (subID = ?) ";
	    	stmt = conn.prepareStatement(query);
	    	stmt.setString(1, subID); 
	        SubscriptionDAO.dbUpdate(conn, stmt);
	        
	        query = "DELETE FROM metric_table WHERE (subID = ?) ";
	    	stmt = conn.prepareStatement(query);
	    	stmt.setString(1, subID); 
	        SubscriptionDAO.dbUpdate(conn, stmt);
	        
	        query = "DELETE FROM subscription_agents_table WHERE (subID = ?) ";
	    	stmt = conn.prepareStatement(query);
	    	stmt.setString(1, subID); 
	        SubscriptionDAO.dbUpdate(conn, stmt);	        
	        
//        	System.out.println("Succesfully deleted from catascopiaDB subscription with id: "+subID);        
	     } 
	     catch (SQLException e) {
			e.printStackTrace();
		 } 
	     finally{
	    	 if (stmt != null)
	    		 try{
	    			 stmt.close();
	    		 }catch (SQLException e) {
					e.printStackTrace();
				 }
	     }
	}
	
	public static void addAgent(Connection conn, String subID, String agentID){
		String query = "";
	    PreparedStatement stmt = null;
	    try{
	    	query = "INSERT INTO subscription_agents_table (subID,agentID) VALUES (?,?);";
			stmt = conn.prepareStatement(query);
			stmt.setString(1,subID);
			stmt.setString(2, agentID);
	        SubscriptionDAO.dbUpdate(conn, stmt);
	      
//        	System.out.println("Succesfully added to subscription: "+subID+" agent: "+agentID);        
	     } 
	     catch (MySQLIntegrityConstraintViolationException e){
	    	 e.printStackTrace();
	     }
	     catch (SQLException e) {
			e.printStackTrace();
		 } 
	     finally{
	    	 if (stmt != null)
	    		 try{
	    			 stmt.close();
	    		 }catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				 }
	     }
	}
	
	public static void removeAgent(Connection conn, String subID, String agentID){
		String query = "";
	    PreparedStatement stmt = null;
	    try{
	        query = "DELETE FROM subscription_agents_table WHERE (subID = ? AND agentID = ?) ";
			stmt = conn.prepareStatement(query);
			stmt.setString(1,subID);
			stmt.setString(2, agentID);
	        SubscriptionDAO.dbUpdate(conn, stmt);
	      
//        	System.out.println("Succesfully removed from subscription: "+subID+" agent: "+agentID);        
	     } 
	     catch (SQLException e) {
			e.printStackTrace();
		 } 
	     finally{
	    	 if (stmt != null)
	    		 try{
	    			 stmt.close();
	    		 }catch (SQLException e) {
					e.printStackTrace();
				 }
	     }
	}
	
	/**
	 * helper method for executing update queries but NOT SELECT queries since it only returns rows
	 * affected.
	 * @param conn
	 * @param stmt
	 * @return
	 * @throws SQLException
	 */
    private static int dbUpdate(Connection conn, PreparedStatement stmt) throws SQLException {
    	int result = stmt.executeUpdate();
        return result;
    }
}
