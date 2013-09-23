package eu.celarcloud.celar_ms.ServerPack.Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import eu.celarcloud.celar_ms.ServerPack.IJCatascopiaServer;

public class DBHandler {

	private Connection conn = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	private IJCatascopiaServer server;
	
	public DBHandler(String host, String user, String pass, String database, IJCatascopiaServer server){
		this.server = server;
		this.dbConnect(host,user,pass,database);
	}
	
	public void dbConnect(String host, String user, String pass, String database){
		try {
			//load the MySQL driver
			Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
//	        System.out.println("Creating a connection...");
			this.server.writeToLog(Level.INFO, "DBHandler>> creating a connection...");
		    conn = DriverManager.getConnection("jdbc:mysql://"+host+"/"+database+"?"
		              							 +"user="+user+"&password="+pass+"");
		    
		    //connected, but does database exist?
		    resultSet = conn.getMetaData().getCatalogs();
		    boolean found = false;
		    while (resultSet.next()) {
		    	String databaseName = resultSet.getString(1);
		        if(databaseName.equals(database)){
//		        	System.out.println("Database exists...");
					this.server.writeToLog(Level.INFO, "DBHandler>> database exists...");
		        	found = true;
		        }
		    }
		    if(!found) 
//		    	System.out.println("Database doesn't exist...");
				this.server.writeToLog(Level.INFO, "DBHandler>> database doesn't exist...");
		    this.resultSet.close();
		} 
		catch(ClassNotFoundException e) {
			this.server.writeToLog(Level.SEVERE, e);
		} 
		catch(SQLException e) {
			this.server.writeToLog(Level.SEVERE, e);
		}
		catch(Exception e){
			this.server.writeToLog(Level.SEVERE, e);
		}
	}
	
	// You need to close the resultSet
	public void dbDisconnect(){
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
	
	public Connection getConnection(){
		return this.conn;
	}
}
