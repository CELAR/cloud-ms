package dbPackage.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import dbPackage.beans.AgentObj;

public class AgentDAO{
	
	/**
	 * Connects to the given database and retrieves a list of all the
	 * agents. If the status parameter is given then only the agents with
	 * this status will be retrieved.
	 * 
	 * @param conn The database connection
	 * @param status [Optional] retrieve agents with this status
	 * @return a list of all the retrieve agents
	 */
	public static synchronized ArrayList<AgentObj> getAgents(Connection conn, String status) {
		String query = "SELECT * FROM agent_table";
        PreparedStatement stmt = null;
        try{
        	query += (status == null || status.length() == 0) ? "" : " WHERE status='" + status + "'";
        	
			stmt = conn.prepareStatement(query);

            ResultSet rs = stmt.executeQuery();
            ArrayList<AgentObj> agentlist = new ArrayList<AgentObj>();
            while(rs.next())
            	agentlist.add(new AgentObj(rs.getString("agentID"),rs.getString("agentIP"),rs.getString("status")));
            return agentlist;
		}
        catch (SQLException e){
        	// TODO Auto-generated catch block
			e.printStackTrace();
		}
        catch (Exception e){
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
        return null;
	}

	/**
	 * Connects to the given database and retrieves a list of all the
	 * agents that are invoked in the specified subscription. 
	 * 
	 * @param conn The database connection
	 * @param subID the subscription id of which invoked agents are retrieved
	 * @return a list of all the agents that are invoked in the given subscription
	 */
	public static synchronized ArrayList<AgentObj> getAgentsForSubscription(Connection conn, String subID) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT b.agentID, c.agentIP FROM subscription_table a ");
		query.append("LEFT JOIN subscription_agents_table b ON a.subID = b.subID AND a.subID = '");
		query.append(subID + "' ");
		query.append("JOIN agent_table c ON b.agentID = c.agentID");

        PreparedStatement stmt = null;
        try{
        	
			stmt = conn.prepareStatement(query.toString());

            ResultSet rs = stmt.executeQuery();
            ArrayList<AgentObj> agentlist = new ArrayList<AgentObj>();
            while(rs.next())
            	agentlist.add(new AgentObj(rs.getString("agentID"),rs.getString("agentIP"),null));
            return agentlist;
		}
        catch (SQLException e){
        	// TODO Auto-generated catch block
			e.printStackTrace();
		}
        catch (Exception e){
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
        return null;
	}
}

