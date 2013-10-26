package servletPack;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import dbPackage.DBHandlerWithConnPool;

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
    	DBHandlerWithConnPool dbHandler = new DBHandlerWithConnPool(host,user,pass,database,10);
    	sc.setAttribute("dbHandler", dbHandler);
    	
    	// Server IP & Port
    	String serverIP = sc.getInitParameter("serverIP");
    	sc.setAttribute("serverIP", serverIP);
    	String serverPort = sc.getInitParameter("serverPort");
    	sc.setAttribute("serverPort", serverPort);
    	
    	System.out.println("ServletListener>> added a DBHandler as an attribute, server IP & Port as an attribute");

    	//debug_mode
    	String debug_mode = sc.getInitParameter("debug_mode");
    	sc.setAttribute("debug_mode", debug_mode);
    	System.out.println("ServletListener>> debug_mode = "+debug_mode);
    }
 
	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {
        // TODO Auto-generated method stub
    }
}