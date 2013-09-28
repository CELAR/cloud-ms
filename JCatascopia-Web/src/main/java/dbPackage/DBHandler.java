package dbPackage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBHandler {

	private Connection conn = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	
	public DBHandler(String host, String user, String pass, String database){
		this.dbConnect(host,user,pass,database);
	}
	
	public void dbConnect(String host, String user, String pass, String database){
		try {
			//load the MySQL driver
			Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
	        System.out.println("Creating a connection...");
		    conn = DriverManager.getConnection("jdbc:mysql://"+host+"/"+database+"?"
		              							 +"user="+user+"&password="+pass+"");
		    
		    //connected, but does database exist?
		    resultSet = conn.getMetaData().getCatalogs();
		    boolean found = false;
		    while (resultSet.next()) {
		    	String databaseName = resultSet.getString(1);
		        if(databaseName.equals(database)){
		        	System.out.println("Database exists...");
		        	found = true;
		        }
		    }
		    if(!found) 
		    	System.out.println("Database doesn't exist...");
		    this.resultSet.close();
		} 
		catch(ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch(SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	    catch (Exception e){}
	}
	
	public Connection getConnection(){
		return this.conn;
	}
}
