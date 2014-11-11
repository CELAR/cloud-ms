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
package eu.celarcloud.celar_ms.ServerPack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.SubObj;
import eu.celarcloud.celar_ms.utils.CatascopiaLogging;
import eu.celarcloud.celar_ms.utils.CatascopiaNetworking;
import eu.celarcloud.celar_ms.ServerPack.Database.IDBHandler;
import eu.celarcloud.celar_ms.ServerPack.subsciptionPack.SubScheduler;

/**
 * JCatascopia Monitoring Server receives monitoring metrics and manages 
 * JCatascopia Monitoring Agents
 *  
 * @author Demetris Trihinas
 *
 */
public class MonitoringServer implements IJCatascopiaServer{
	//path to JCatascopia Agent Directory
	private String JCATASCOPIA_SERVER_HOME;
	//path to config file
	private static final String CONFIG_PATH = "resources"+File.separator+"server.properties";
	/**
	 * properties read from config file
	 */
	private Properties config;
	/**
	 * give Server a unique ID
	 */
	private String serverID;
	/**
	 * Agent IP address
	 */
	private String serverIPaddress;
		
	private Listener agentListener;
	private Listener controlListener;
	
	/**
	 * flag to check if logging available
	 */
	private boolean loggingFlag;
	/**
	 * Logger to handle logging
	 */
	private Logger myLogger;
	
	protected ConcurrentHashMap<String,AgentObj> agentMap;
	protected ConcurrentHashMap<String,MetricObj> metricMap;
	protected ConcurrentHashMap<String,SubObj> subMap;

	protected CatascopiaExecutor processExecutor;
	protected CatascopiaExecutor controlExecutor;

	private HeartBeatMonitor heartBeatMonitor;
	
	private boolean databaseFlag;
	//public DBHandlerWithConnPool dbHandler;
	public IDBHandler dbHandler;
	
	public SubScheduler subscheduler;
	
	private boolean debugMode;
	private boolean redistMode;
	private Distributor redistributor;
	protected Aggregator aggregator;
	
	public MonitoringServer(String serverDirPath, String lockPath) throws CatascopiaException{
		//remove daemon lock when JVM terminates if Server is ran in daemon mode
		if (lockPath != null){
			File f = new File (lockPath);
			f.deleteOnExit();
		}
		this.JCATASCOPIA_SERVER_HOME = serverDirPath;
		//parse config file
		this.parseConfig();
		
		this.serverID = UUID.randomUUID().toString().replace("-", "");
		this.serverIPaddress = CatascopiaNetworking.getMyIP();

		this.initLogging(); 
		this.initDataStructures();
		this.initExecutors();
		this.initDBHandler();
		this.initHeartBeatMonitor();
		this.initAgentListener(); 
		
		this.subscheduler = new SubScheduler();
		this.initControlListener();
		
		try{
			this.debugMode = Boolean.parseBoolean(this.config.getProperty("debug_mode", "false"));
		}catch(Exception e){
			this.debugMode = false;
		}
		
		this.initRedistributor();
	}
	
	public String getServerID(){
		return this.serverID;
	}
	
	public String getServerIP(){
		return this.serverIPaddress;
	}
	
	//parse the configuration file
	private void parseConfig() throws CatascopiaException{
		this.config = new Properties();
		//load config properties file
		try {
			FileInputStream fis = new FileInputStream(JCATASCOPIA_SERVER_HOME+File.separator+CONFIG_PATH);
			config.load(fis);
			if (fis != null)
	    		fis.close();
		} 
		catch (FileNotFoundException e){
			throw new CatascopiaException("config file not found", CatascopiaException.ExceptionType.FILE_ERROR);
		} 
		catch (IOException e){
			throw new CatascopiaException("config file parsing error", CatascopiaException.ExceptionType.FILE_ERROR);
		}
	}
	
	//initialize logging
	private void initLogging(){
		this.loggingFlag = Boolean.parseBoolean(this.config.getProperty("logging", "false"));
		if (this.loggingFlag)
			try{
				this.myLogger = CatascopiaLogging.getLogger(this.JCATASCOPIA_SERVER_HOME,"JCatascopia Server");
				this.myLogger.info("JCatascopia Server"+": Created and Initialized");
				this.loggingFlag = true;
			}
			catch (Exception e){
				//Unable to log events
				this.loggingFlag = false;
			}
		else this.loggingFlag = false; //logging turned off by user
	}
	
	/**
	 * method that logs messages to the MS Server log
	 */
	public void writeToLog(Level level, Object msg){
		if(this.loggingFlag)
			this.myLogger.log(level, "JCatascopia Server: "+msg);
	}
	
	/**
	 * initialize the AgentListener
	 * @throws CatascopiaException 
	 */
	private void initAgentListener() throws CatascopiaException{
		String port = this.config.getProperty("agent_port", "4242");
    	String ip = this.config.getProperty("agent_bind_ip","localhost");
    	
		this.agentListener = new AgentListener(ip, port, this);
		this.agentListener.activate();
		
		this.writeToLog(Level.INFO, "AgentListener>> Initialized with params: ("+ip+","+port+")");
	}
	
	/**
	 * initialize the ControlListener
	 * @throws CatascopiaException 
	 */
	private void initControlListener() throws CatascopiaException{
		String port = this.config.getProperty("control_port", "4245");
    	String ip = this.config.getProperty("control_bind_ip","localhost");
    	
		this.controlListener = new ControlListener(ip, port, this);
		this.controlListener.activate();
		
		this.writeToLog(Level.INFO, "ControlListener>> Initialized with params: ("+ip+","+port+")");
	}
	
	private void initExecutors(){
		//process executor
		int thread_num = Integer.parseInt(this.config.getProperty("num_of_processing_threads", "2"));
		this.processExecutor = new CatascopiaExecutor(thread_num,2*thread_num,60,128);
		this.writeToLog(Level.INFO, "ProcessExecutor Initialized with ("+thread_num+") threads");
		
		//control executor
		this.controlExecutor = new CatascopiaExecutor(2,4,60,30);
		this.writeToLog(Level.INFO, "ControlExecutor Initialized");
	}
	
	private void initDBHandler() throws CatascopiaException{
		this.databaseFlag = Boolean.parseBoolean(this.config.getProperty("db_use_database", "false"));
		if(this.databaseFlag){
			String dbInterface = this.config.getProperty("db_interface","Basic.DBHandler");
			String hosts = this.config.getProperty("db_host","localhost");
			String user = this.config.getProperty("db_user",null);
			String pass = this.config.getProperty("db_pass",null);
			String database = this.config.getProperty("db_database");
			Integer conn_num = new Integer(Integer.parseInt(this.config.getProperty("num_of_processing_threads", "1")));

			List<String> endpoints = new ArrayList<String>();
			for(String s : hosts.split(","))
				endpoints.add(s);
			
		    String dbPath = "eu.celarcloud.celar_ms.ServerPack.Database."+dbInterface;
		    
		    Class<?>[] myArgs = new Class[6];
	        myArgs[0] = List.class;
	        myArgs[1] = String.class;
	        myArgs[2] = String.class;
	        myArgs[3] = String.class;
	        myArgs[4] = Integer.class;
	        myArgs[5] = IJCatascopiaServer.class;
			try{
		        Class<IDBHandler> _tempClass = (Class<IDBHandler>) Class.forName(dbPath);
		        Constructor<IDBHandler> _tempConst = _tempClass.getDeclaredConstructor(myArgs);
				this.dbHandler = _tempConst.newInstance(endpoints,user,pass,database,conn_num,this);
			    dbHandler.dbConnect();
			    this.writeToLog(Level.INFO, "DBHandler>> Successfully connected to database: "+dbInterface+ " host(s): "+hosts
			    		                     +"\nwith params ("+hosts+", "+user+", "+pass+", "+database+")");
			    
				boolean dropTables = Boolean.parseBoolean(this.config.getProperty("db_drop_tables_on_startup","false"));				
				dbHandler.dbInit(dropTables);
				this.writeToLog(Level.INFO, "DBHandler>> Successfully dropped tables and created new ones");	
			}
			catch (ClassNotFoundException e){
				this.writeToLog(Level.SEVERE, e);
				throw new CatascopiaException(e.getMessage(),CatascopiaException.ExceptionType.DATABASE);
			}
			catch(Exception e){
				this.writeToLog(Level.SEVERE, e);
				throw new CatascopiaException(e.getMessage(),CatascopiaException.ExceptionType.DATABASE);
			}
		}
	}
	
	public boolean getDatabaseFlag(){
		return this.databaseFlag;
	}
	
	private void initDataStructures(){
		this.agentMap = new ConcurrentHashMap<String,AgentObj>();
		this.metricMap = new ConcurrentHashMap<String,MetricObj>();
		this.subMap = new ConcurrentHashMap<String,SubObj>();
	}
	
	private void initHeartBeatMonitor(){
		int period = Integer.parseInt(this.config.getProperty("period","60"));
		int retrys = Integer.parseInt(this.config.getProperty("retry","3"));
		this.heartBeatMonitor = new HeartBeatMonitor(this,period,retrys);
		this.heartBeatMonitor.activate();
		
		this.writeToLog(Level.INFO, "HeartBeatMonitor Initialized");
	}
	
	public ConcurrentHashMap<String,AgentObj> getAgentMap(){
		return this.agentMap;
	}
	
	public ConcurrentHashMap<String,MetricObj> getMetricMap(){
		return this.metricMap;
	}
	
	public ConcurrentHashMap<String,SubObj> getSubMap(){
		return this.subMap;
	}
	
	public void terminate(){
		//TODO close everything gracefully
	}
	
	public boolean inDebugMode(){
		return this.debugMode;
	}
	
	public boolean inRedistributeMode(){
		return this.redistMode;
	}
	
	private void initRedistributor() throws CatascopiaException{
		try{
			this.redistMode = Boolean.parseBoolean(this.config.getProperty("redist_turnOn", "false"));
		}catch(Exception e){
			this.redistMode = false;
		}
		if(this.redistMode){
			String destIP = this.config.getProperty("redist_destIP",null);
			if(destIP == null || destIP.equals("")){
				this.writeToLog(Level.SEVERE, "Redistributor destination IP not set in config file...going to terminate");
				throw new CatascopiaException("Redistributor destination IP not set in config file",CatascopiaException.ExceptionType.ARGUMENT);
			}
			else{
				this.establishConnectionWithServer(destIP);	
				//Distributor settings
				String port = "4242";
				String protocol = "tcp";
			    String hwm ="16";
			    //Aggregator settings
				this.aggregator = new Aggregator(this);
			    long agg_interval = Long.parseLong(this.config.getProperty("aggregator_interval"))*1000;
				this.redistributor = new Distributor(destIP,port,protocol,Long.parseLong(hwm),this.aggregator,agg_interval,this);
				redistributor.activate();
				this.writeToLog(Level.INFO, "Redistributor initialized with destination IP: "+destIP);
			}
		}
	}
	
	private void establishConnectionWithServer(String destIP) throws CatascopiaException{
		String port = "4245";
		String protocol = "tcp";
		
		if (!InitialServerConnector.connect(destIP,port,protocol,this.serverID,this.serverIPaddress)){
			this.writeToLog(Level.SEVERE, "FAILED to ping MS Server at "+destIP);
			throw new CatascopiaException("Could not connect to MS Server ",CatascopiaException.ExceptionType.CONNECTION);
		}
		this.writeToLog(Level.INFO, "Successfuly ping-ed MS Server at "+destIP);
	}

	/**
	 * @param args
	 * @throws CatascopiaException 
	 */
	public static void main(String[] args) throws CatascopiaException {
		try{		
			if (args.length > 0)
				new MonitoringServer(args[0], args[1]);
			else
				new MonitoringServer(".", null);
		}catch(Exception e){
			System.exit(0);
		}
	}

}
