package dbPackage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * DBHandler with Connection Pool
 * @author Demetris Trihinas
 *
 */
public class DBHandlerWithConnPool{
	private BasicDataSource dataSource = null;
	private Connection conn = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	
	private String host;
	private String user;
	private String pass;
	private String database;
	private int CONN_NUM;
	
	public DBHandlerWithConnPool(String host, String user, String pass, String database, int cnum){
		this.host = host;
		this.user = user;
		this.pass = pass;
		this.database = database;
		this.CONN_NUM = cnum;
		
		this.dbConnect(this.host,this.user,this.pass,this.database);
	}
	
	public DBHandlerWithConnPool(String host, String user, String pass, String database){
		this(host, user, pass, database, 1);
	}
	
	public void dbConnect(String host, String user, String pass, String database){
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
	        
	        System.out.println("Creating a DB connection...");
	        	    
		    //connected, but does database exist?
	        conn = dataSource.getConnection();
		    resultSet = conn.getMetaData().getCatalogs();
		    boolean found = false;
		    while (resultSet.next()) {
		    	String databaseName = resultSet.getString(1);
		        if(databaseName.equals(database)){
		        	System.out.println("Connected to JCatascopia DB...");
		        	found = true;
		        }
		    }
		    if(!found)
		    	System.out.println("DBHandler>> JCatascopia database doesn't exist...");
		    this.resultSet.close();
		} 
		catch(SQLException e) {
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{conn.close();} catch (SQLException e) {e.printStackTrace();}
		}
	}
	
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
			e.printStackTrace();
	    }
	}
	
	public Connection getConnection(){
		Connection c = null;
		try {
			c = this.dataSource.getConnection();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}
}
