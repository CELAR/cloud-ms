package eu.celarcloud.celar_ms.ServerPack.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;


public class AgentDAO{

	public static synchronized void createAgent(Connection conn, AgentObj agent){
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
            	System.out.println("Agent already exists");
                throw new SQLException("Agent already exists");
            	//throw catascopia exception
            }
      		System.out.println("Succesfully inserted to catascopiaDB new Agent with id: "+agent.getAgentID()
      				          +" and ip: "+agent.getAgentIP());
		}
        catch (SQLException e){
        	// TODO Auto-generated catch block
			e.printStackTrace();
		}
        finally{
        	if (stmt != null)
        		try{
        			stmt.close();
				}catch (SQLException e){
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
        }
	}
	
	public static void deleteAgent(Connection conn, String agentID){
		String query = "DELETE FROM agent_table WHERE (agentID = ?) ";
	    PreparedStatement stmt = null;
	    try{
	    	stmt = conn.prepareStatement(query);
	    	stmt.setString(1, agentID); 

	        int res = AgentDAO.dbUpdate(conn, stmt);
	        if (res == 0){
	        	System.out.println("Agent with id: "+agentID+" was not found in catascopiaDB");        
				//throw new NotFoundException("Object could not be deleted! (PrimaryKey not found)");
	        }
        	System.out.println("Succesfully deleted from catascopiaDB Agent with id: "+agentID);        

//	       if (res > 1){
//	    	   System.out.println("PrimaryKey Error when updating DB! (Many objects were deleted!)");
//	           throw new SQLException("PrimaryKey Error when updating DB! (Many objects were deleted!)");
//	       }
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
	
	public static void updateAgent(Connection conn, String agentID, String status){
		String query = "UPDATE agent_table SET status=? WHERE agentID=?";
	    PreparedStatement stmt = null;
	    try{
	    	stmt = conn.prepareStatement(query);
	    	stmt.setString(1, status); 
	    	stmt.setString(2, agentID);
	    	
	        int res = AgentDAO.dbUpdate(conn, stmt);
	        if (res == 0){
	        	System.out.println("Agent with id: "+agentID+" was not found in catascopiaDB");        
				//throw new NotFoundException("Object could not be deleted! (PrimaryKey not found)");
	        }
        	System.out.println("Succesfully updated catascopiaDB Agent with id: "+agentID);        

//	       if (res > 1){
//	    	   System.out.println("PrimaryKey Error when updating DB! (Many objects were deleted!)");
//	           throw new SQLException("PrimaryKey Error when updating DB! (Many objects were deleted!)");
//	       }
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
