package eu.celarcloud.celar_ms.ServerPack.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;


public class MetricDAO {
	
	public static synchronized void createMetric(Connection conn, MetricObj metric) throws CatascopiaException{
		String query = "";
        PreparedStatement stmt = null;

        try{
            query = "INSERT INTO metric_table (metricID,agentID,name,mgroup,units,type) VALUES (?,?,?,?,?,?)";
			stmt = conn.prepareStatement(query);
			stmt.setString(1, metric.getMetricID()); 
			stmt.setString(2, metric.getAgentID()); 
			stmt.setString(3, metric.getName()); 
			stmt.setString(4, metric.getGroup());
			stmt.setString(5, metric.getUnits()); 
			stmt.setString(6, metric.getType()); 			

             int res = MetricDAO.dbUpdate(conn, stmt);
             if (res != 1) {
//            	 System.out.println("Metric already exists");
                 throw new CatascopiaException("MetricDAO>> Metric already exists: "+metric.toString(), CatascopiaException.ExceptionType.DATABASE);
             }
//             System.out.println("Succesfully inserted to catascopiaDB new Metric with id: "+metric.getMetricID()+" and name: "+metric.getName());
		 }
        catch (SQLException e){
			e.printStackTrace();
		 }
        finally{
       	 if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} 
        }
	}
	
	public static void deleteMetric(Connection conn, String metricID) throws CatascopiaException{
		String query = "DELETE FROM metric_table WHERE (metricID = ?) ";
	    PreparedStatement stmt = null;
	    try{
	       stmt = conn.prepareStatement(query);
	       stmt.setString(1, metricID); 

	       int res = MetricDAO.dbUpdate(conn, stmt);
	       if (res == 0){
//	        	System.out.println("Metric with id: "+metricID+" was not found in catascopiaDB");        
                throw new CatascopiaException("MetricDAO>> Metric with id:"+metricID+"was not found in catascopiaDB: ", CatascopiaException.ExceptionType.DATABASE);
	       }
//       	   System.out.println("Succesfully deleted from catascopiaDB Agent with id: "+metricID);    
	    } 
	    catch (SQLException e) {
			e.printStackTrace();
		} 
	    finally{
	    	if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
	    }
	}
	
	public static void insertValue(Connection conn, String metricID, long timestamp, String val){
		String query = "";
        PreparedStatement stmt = null;
        try{
            query = "INSERT INTO metric_value_table (metricID,timestamp,value) VALUES (?,?,?)";
			stmt = conn.prepareStatement(query);
			stmt.setString(1, metricID); 
			stmt.setTimestamp(2, new java.sql.Timestamp(timestamp)); 
			stmt.setString(3, val); 
			
             int res = MetricDAO.dbUpdate(conn, stmt);
//             if (res != 1){
//            	 System.out.println("Metric value already exists");
//             }
//             System.out.println("Succesfully inserted to catascopiaDB new Metric value");
		 }
        catch (SQLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }
        finally{
       	 if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
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
