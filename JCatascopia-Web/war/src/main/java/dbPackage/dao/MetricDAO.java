package dbPackage.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dbPackage.beans.MetricObj;

public class MetricDAO {
	
	/**
	 * Connects to the given database and retrieves a list of all the available
	 * metrics for the specified agent along with their metadata(metricID,name,mgroup,units,type).
	 * 
	 * @param conn The database connection
	 * @param agentID The agent which's available metrics are requested
	 * @return a list of all the available metrics along with their metadata
	 */
	public static synchronized ArrayList<MetricObj> getAvailableMetricsMetaData(Connection conn, String agentIDs[])
	{
        PreparedStatement stmt = null;
        StringBuilder query = new StringBuilder();
        
        try{
        	query.append("SELECT metricID,name,mgroup,units,type FROM metric_table WHERE ");
        	boolean first = true;
        	for(String s: agentIDs) {
        		if(!first) query.append(" || ");
        		query.append("agentID='"+s+"'");
        		first = false;
        	}
        	
			stmt = conn.prepareStatement(query.toString());

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
        	MetricDAO.release(stmt, conn);	
        }
        return null;
	}

	/**
	 * Connects to the given database and retrieves all the available metrics.
	 * 
	 * @param conn The database connection
	 * @return a list of all the available metrics along with their type and units
	 */
	public static synchronized ArrayList<MetricObj> getSubscriptionsAvailableMetrics(Connection conn)
	{
		String query = "";
		PreparedStatement stmt = null;

		try{
			query = "SELECT metricID,name,mgroup,units,type FROM metric_table WHERE (type='INTEGER' OR type='DOUBLE') AND subID IS NULL GROUP BY name";

			stmt = conn.prepareStatement(query);

			ResultSet rs = stmt.executeQuery();
			ArrayList<MetricObj> metriclist = new ArrayList<MetricObj>();
			while (rs.next())
				metriclist.add(new MetricObj(rs.getString("metricID"), rs.getString("name"),
						rs.getString("units"),rs.getString("type"),null));
			return metriclist;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
        	MetricDAO.release(stmt, conn);	
        }
		return null;
	}
	
	/**
	 * Connects to the given database and retrieves the last value stored into the database for each of the
	 * metric in the registered metrics array.
	 * 
	 * @param conn The database connection
	 * @param registeredMetrics An array of all the metrics for which to get the latest value
	 * @return a list of the latest values of the given metrics
	 */
	public static synchronized ArrayList<MetricObj> getMetrics(Connection conn, String[] registeredMetrics)
	{

		if(registeredMetrics==null || registeredMetrics.length==0) return null;
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT m1.*, m3.name, m3.units, m3.type, m3.mgroup FROM metric_value_table as m1 ");
		query.append("JOIN (SELECT MAX(valueID) as latest FROM metric_value_table GROUP BY metricID) as m2 ON m1.valueID = latest ");
		query.append("JOIN (SELECT * FROM metric_table ");
		query.append("WHERE ");
		// where clause building
		boolean first = true;
		for(String s: registeredMetrics) {
			if(!first) query.append("|| ");
			query.append("metricID =  '"+ s +"' ");
			first = false;
		}
		query.append(") as m3 ON m1.metricID=m3.metricID");
		
		//System.out.println(query);
		PreparedStatement stmt = null;
		
		try {
			stmt = conn.prepareStatement(query.toString());

            ResultSet rs = stmt.executeQuery();
            ArrayList<MetricObj> metriclist = new ArrayList<MetricObj>();
            while (rs.next())
            	metriclist.add(new MetricObj(rs.getString("metricID"), rs.getString("name"),
            					rs.getString("units"),rs.getString("type"),rs.getString("mgroup"),rs.getString("value"),rs.getString("timestamp")));
            return metriclist;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
        catch(Exception e){
        	e.printStackTrace();
        }
		finally{
        	MetricDAO.release(stmt, conn);	
        }
        return null;
	}
	
	/**
	 * Connects to the given database and retrieves the all the values of this metric from x seconds before until now.
	 * 
	 * @param conn The database connection
	 * @param metricID The id of the metric to retrieve
	 * @param interval The number of seconds until now.
	 * @return a list of all the values in this interval
	 */
	public static synchronized ArrayList<MetricObj> getMetricByTime(Connection conn, String metricID, String interval) {
		StringBuilder query = new StringBuilder();
		if (interval !=null){
			query.append("SELECT m1.metricID,m1.value,m1.timestamp, m2.name, m2.type, m2.units, m2.mgroup FROM metric_table as m2 ");
			query.append("JOIN (SELECT * FROM metric_value_table WHERE metricID='"+metricID+"' AND timestamp>DATE_SUB(now(), INTERVAL "+interval+" SECOND)) ");
			query.append("as m1 ON m1.metricID=m2.metricID;");
		}
		else{
			query.append("SELECT m1.*,m3.name, m3.units, m3.type, m3.mgroup FROM metric_value_table as m1 ");
			query.append("JOIN (SELECT MAX(valueID) as latest FROM metric_value_table WHERE metricID='"+metricID+"') as m2 ON m1.valueID = latest ");
			query.append("JOIN (SELECT * FROM metric_table) as m3 ON m1.metricID=m3.metricID");
		}
		PreparedStatement stmt = null;
		
		try {
			//System.out.println(query);
			stmt = conn.prepareStatement(query.toString());

            ResultSet rs = stmt.executeQuery();
            ArrayList<MetricObj> metriclist = new ArrayList<MetricObj>();
            while (rs.next())
            	metriclist.add(new MetricObj(rs.getString("metricID"), rs.getString("name"),
    					rs.getString("units"),rs.getString("type"),rs.getString("mgroup"),rs.getString("value"),rs.getString("timestamp")));
            return metriclist;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
        catch(Exception e){
        	e.printStackTrace();
        }
		finally{
        	MetricDAO.release(stmt, conn);	
        }
		
		return null;
	}
	
	/**
	 * Connects to the given database and retrieves all the values between the given timerange.
	 * 
	 * @param conn The database connection
	 * @param metricIDs The metric ids for which to retrieve values
	 * @param start The time to start from
	 * @param end The time to end at
	 * @return a list of all the values between this timerange
	 */
	public static synchronized ArrayList<MetricObj> getMetricByTime(Connection conn, String metricIDs[], String start, String end) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT m1.metricID,m1.value,m1.timestamp, m2.name, m2.type, m2.units, m2.mgroup FROM metric_table as m2 ");
		query.append("JOIN (SELECT * FROM metric_value_table WHERE ");
		boolean first = true;
		for(String s: metricIDs) {
			if(!first) query.append(" || ");
			query.append(" metricID='"+s+"'");
			first = false;
		}
		query.append(" AND timestamp>='" + start + "'");
		if(end!=null && end.length()>0)
			query.append(" AND timestamp<='" + end + "'");
		query.append(") as m1 ON m1.metricID=m2.metricID;");

		PreparedStatement stmt = null;
		
		try {
//			System.out.println(query);
			stmt = conn.prepareStatement(query.toString());

            ResultSet rs = stmt.executeQuery();
            ArrayList<MetricObj> metriclist = new ArrayList<MetricObj>();
            while (rs.next()) 
            	metriclist.add(new MetricObj(rs.getString("metricID"), rs.getString("name"),
    					rs.getString("units"),rs.getString("type"),rs.getString("mgroup"),rs.getString("value"),rs.getString("timestamp")));
            return metriclist;
		}
		catch(SQLException e){
			e.printStackTrace();
		}
        catch(Exception e){
        	e.printStackTrace();
        }
		finally{
        	MetricDAO.release(stmt, conn);	
        }
		
		return null;
	}

	/**
	 * Connect to the given database and retrieves the latest values of all metrics of the given agents.
	 * 
	 * @param conn The database connection
	 * @param agentIDs The agents of which to retrieve the metric values
	 * @param start The time to start
	 * @param end The time to end
	 * @return an array with a list of all the  latest values between for each agent
	 */
	public static synchronized JSONArray getAgentsLatestMetrics(Connection conn, String agentIDs[]) {
		JSONArray jarray = new JSONArray();
		JSONObject jresponse;
		
		ArrayList<MetricObj> metriclist = null;
		for(String s: agentIDs) {
			metriclist = MetricDAO.getAvailableMetricsMetaData(conn, new String[]{s});
		
			jresponse = new JSONObject();
			try {
				jresponse.put("agentID",s);
				JSONArray metrics = new JSONArray();
	
				if(metriclist != null) {
					ArrayList<MetricObj> valuesList;
					StringBuilder val = new StringBuilder();
					boolean first = true;
					for(MetricObj mo: metriclist) {
						valuesList = MetricDAO.getMetricByTime(conn, mo.getMetricID(), null);
						if (valuesList != null && valuesList.size()>0)
							mo.setValue(valuesList.get(0).getValue());
						if(!first) val.append(",");
						val.append(mo.getMetricID());
						first = false;
	
						metrics.put(new JSONObject(mo.toJSON()));
					}
					jresponse.put("metrics", metrics);				
					jarray.put(jresponse);
				}
			} 
			catch (JSONException e) {
				e.printStackTrace();
			}
			finally{
	        	MetricDAO.release(null, conn);	
	        }
		}
		return jarray;
	}
	
	private static void release(PreparedStatement stmt, Connection conn){
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
}
