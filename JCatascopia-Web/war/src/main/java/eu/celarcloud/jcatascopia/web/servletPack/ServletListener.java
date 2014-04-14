/*******************************************************************************
 * Copyright 2014, Laboratory of Internet Computing (LInC), Department of Computer Science, University of Cyprus
 * 
 * For any information relevant to JCatascopia Monitoring System,
 * please contact Demetris Trihinas, trihinas{at}cs.ucy.ac.cy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
