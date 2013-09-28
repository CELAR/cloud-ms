package eu.celarcloud.celar_ms.ServerPack.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;


public class AgentDAO{

	public static synchronized void createAgent(Connection conn, AgentObj agent) throws CatascopiaException{
		String query = "";
        PreparedStatement stmt = null;

        try{
        	query = "INSERT INTO agent_table (agentID,agentIP,status) VALUES (?,?,?)";

			stmt = conn.prepareStatement(query);
			stmt.setString(1, agent.getAgentID()); 
			stmt.setString(2, agent.getAgentIP()); 
			stmt.setString(3, agent.getStatus().name());

            int res = AgentDAO.dbUpdate(conn, stmt);
            if (res != 1){
//            	System.out.println("AgentDAO>> Agent with IP address: "+agent.getAgentID()+" already exists");
                throw new CatascopiaException("AgentDAO>> Agent with IP address: "+agent.getAgentID()+" already exists in DB", CatascopiaException.ExceptionType.DATABASE);
            }
//      		System.out.println("AgentDAO>> Succesfully inserted to catascopiaDB new Agent with id: "+agent.getAgentID() +" and ip: "+agent.getAgentIP());
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
	
	public static void deleteAgent(Connection conn, String agentID) throws CatascopiaException{
		String query = "DELETE FROM agent_table WHERE (agentID = ?) ";
	    PreparedStatement stmt = null;
	    try{
	    	stmt = conn.prepareStatement(query);
	    	stmt.setString(1, agentID); 

	        int res = AgentDAO.dbUpdate(conn, stmt);
	        if (res == 0){
	        	//System.out.println("Agent with id: "+agentID+" was not found in catascopiaDB");        
	        	throw new CatascopiaException("Agent with id: "+agentID+" was not found in catascopiaDB",CatascopiaException.ExceptionType.DATABASE);
	        }
//        	System.out.println("Succesfully deleted from catascopiaDB Agent with id: "+agentID);        
	     } 
	     catch (SQLException e) {
	    	// TODO Auto-generated catch block
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
	
	public static void updateAgent(Connection conn, String agentID, String status) throws CatascopiaException{
		String query = "UPDATE agent_table SET status=? WHERE agentID=?";
	    PreparedStatement stmt = null;
	    try{
	    	stmt = conn.prepareStatement(query);
	    	stmt.setString(1, status); 
	    	stmt.setString(2, agentID);
	    	
	        int res = AgentDAO.dbUpdate(conn, stmt);
	        if (res == 0){
//	        	System.out.println("Agent with id: "+agentID+" was not found in catascopiaDB");        
				throw new CatascopiaException("Agent with id: "+agentID+" was not found in catascopiaDB",CatascopiaException.ExceptionType.DATABASE);
	        }
//        	System.out.println("Succesfully updated catascopiaDB Agent with id: "+agentID);        
	     } 
	     catch (SQLException e) {
	    	// TODO Auto-generated catch block
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
