package eu.celarcloud.jcatascopia.web.servletPack;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import eu.celarcloud.jcatascopia.web.queryMaster.database.Cassandra.DBInterface;
import eu.celarcloud.jcatascopia.web.queryMaster.database.MySQL.DBInterfaceWithConnPool;
import eu.celarcloud.jcatascopia.web.queryMaster.database.IDBInterface;

public class ServletListener implements ServletContextListener{
	
	IDBInterface dbInterface;
	
	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent event){
    	ServletContext sc = event.getServletContext();
    	// DBHandler
    	String dbContainer = sc.getInitParameter("dbContainer");
    	String hosts = sc.getInitParameter("hosts");
    	String user = sc.getInitParameter("user");
    	String pass = sc.getInitParameter("pass");
    	String database = sc.getInitParameter("database");
    	List<String> nodelist = new ArrayList<String>();
    	for(String s : hosts.split(","))
			nodelist.add(s);   	
    	
	    String dbPath = "eu.celarcloud.jcatascopia.web.queryMaster.database."+dbContainer;
	    
	    Class<?>[] myArgs = new Class[4];
        myArgs[0] = List.class;
        myArgs[1] = String.class;
        myArgs[2] = String.class;
        myArgs[3] = String.class;
        try{
	        Class<IDBInterface> _tempClass = (Class<IDBInterface>) Class.forName(dbPath);
	        Constructor<IDBInterface> _tempConst = _tempClass.getDeclaredConstructor(myArgs);
			this.dbInterface = _tempConst.newInstance(nodelist,user,pass,database);
			this.dbInterface.dbConnect();
        }
        catch(Exception e){
    		e.printStackTrace();
    	}
    	sc.setAttribute("dbInterface", dbInterface);
    	
    	// Server IP & Port
    	String serverIP = sc.getInitParameter("serverIP");
    	sc.setAttribute("serverIP", serverIP);
    	String serverPort = sc.getInitParameter("serverPort");
    	sc.setAttribute("serverPort", serverPort);
    	
    	System.out.println("ServletListener>> added a DBHandler as an attribute, server IP & Port as an attribute");

    	//debug_mode
    	String debug_mode = sc.getInitParameter("debug_mode");
    	sc.setAttribute("debug_mode", debug_mode);
    	System.out.println("ServletListener>> debug_mode = " + debug_mode);
    }
 
	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {
        this.dbInterface.dbClose();
    }
}