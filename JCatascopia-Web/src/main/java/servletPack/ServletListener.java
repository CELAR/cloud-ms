package servletPack;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import dbPackage.DBHandler;

public class ServletListener implements ServletContextListener{
	 
	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent event){
    	ServletContext sc = event.getServletContext();
    	// DBHandler
    	String host = sc.getInitParameter("host");
    	String user = sc.getInitParameter("user");
    	String pass = sc.getInitParameter("pass");
    	String database = sc.getInitParameter("database");
    	DBHandler dbHandler = new DBHandler(host,user,pass,database);
    	sc.setAttribute("dbHandler", dbHandler);
    	
    	// Server IP & Port
    	String serverIP = sc.getInitParameter("serverIP");
    	sc.setAttribute("serverIP", serverIP);
    	String serverPort = sc.getInitParameter("serverPort");
    	sc.setAttribute("serverPort", serverPort);

    	System.out.println("Created ServletListener, added a DBHandler as an attribute, server IP&port as an attribute");
 
    }
 
	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {
        // TODO Auto-generated method stub
    }
}