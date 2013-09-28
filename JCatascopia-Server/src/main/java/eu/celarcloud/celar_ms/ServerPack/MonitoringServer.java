package eu.celarcloud.celar_ms.ServerPack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
import eu.celarcloud.celar_ms.ServerPack.Database.DBHandler;
import eu.celarcloud.celar_ms.ServerPack.Database.InitializeDB;
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
	public DBHandler dbHandler;
	
	public SubScheduler subscheduler;
	
	private boolean debugMode;
	private boolean redistMode;
	private Distributor redistributor;
	protected Aggregator aggregator;
	
	public MonitoringServer(String serverDirPath) throws CatascopiaException{
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
				this.myLogger = CatascopiaLogging.getLogger(this.JCATASCOPIA_SERVER_HOME,"JCatascopiaMSServer");
				this.myLogger.info("JCatascopiaMSServer"+": Created and Initialized");
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
			this.myLogger.log(level, "JCatascopiaMSServer: "+msg);
	}
	
	/**
	 * initialize the AgentListener
	 * @throws CatascopiaException 
	 */
	private void initAgentListener() throws CatascopiaException{
		String port = this.config.getProperty("agent_port", "4242");
		String protocol = this.config.getProperty("agent_protocol","tcp");
    	String ip = this.config.getProperty("agent_bind_ip","localhost");
    	String hwm = this.config.getProperty("agent_hwm","32");
    	
		this.agentListener = new AgentListener(ip,port,protocol,Long.parseLong(hwm),this);
		this.agentListener.activate();
		
		this.writeToLog(Level.INFO, "AgentListener Initialized with params ("+ip+","+port+","+protocol+")");
	}
	
	/**
	 * initialize the ControlListener
	 * @throws CatascopiaException 
	 */
	private void initControlListener() throws CatascopiaException{
		String port = this.config.getProperty("control_port", "4245");
		String protocol = this.config.getProperty("control_protocol","tcp");
    	String ip = this.config.getProperty("control_bind_ip","localhost");
    	String hwm = this.config.getProperty("control_hwm","32");
    	
		this.controlListener = new ControlListener(ip,port,protocol,Long.parseLong(hwm),this);
		this.controlListener.activate();
		
		this.writeToLog(Level.INFO, "ControlListener Initialized with params ("+ip+","+port+","+protocol+")");
	}
	
	private void initExecutors(){
		//process executor
		int thread_num = Integer.parseInt(this.config.getProperty("num_of_processing_threads", "2"));
		this.processExecutor = new CatascopiaExecutor(thread_num,thread_num,60,32);
		this.writeToLog(Level.INFO, "ProcessExecutor Initialized with ("+thread_num+") threads");
		
		//control executor
		this.controlExecutor = new CatascopiaExecutor(1,2,60,10);
		this.writeToLog(Level.INFO, "ControlExecutor Initialized");
	}
	
	private void initDBHandler(){
		this.databaseFlag = Boolean.parseBoolean(this.config.getProperty("db_use_database", "false"));
		if(this.databaseFlag){
			String HOST = this.config.getProperty("db_host");
			String USER = this.config.getProperty("db_user");
			String PASS = this.config.getProperty("db_pass");
			String DATABASE = this.config.getProperty("db_database");
			
			this.writeToLog(Level.INFO, "DB Handler enabled: ("+HOST+" "+USER+" "+PASS+" "+DATABASE+")");
			this.dbHandler = new DBHandler(HOST,USER,PASS,DATABASE, this);
			
			boolean dropTables = Boolean.parseBoolean(this.config.getProperty("db_drop_tables_on_startup","false"));
			try {
				InitializeDB.createTables(this.dbHandler.getConnection(),dropTables, this);
				this.writeToLog(Level.INFO, "Successfully dropped tables and created new ones");	
			}catch (CatascopiaException e){
				this.writeToLog(Level.SEVERE, e);	
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
			    int agg_buf = Integer.parseInt(this.config.getProperty("aggregator_buffer_size"));
				this.redistributor = new Distributor(destIP,port,protocol,Long.parseLong(hwm),this.aggregator,agg_interval,agg_buf,this);
				redistributor.activate();
				this.writeToLog(Level.INFO, "Redistributor initialized with destination IP: "+destIP);
			}
		}
	}
	
	private void establishConnectionWithServer(String destIP) throws CatascopiaException{
		String port = "4245";
		String protocol = "tcp";
		
		if (!InitialServerConnector.connect(destIP,port,protocol,this.serverID,this.serverIPaddress)){
			this.writeToLog(Level.SEVERE, "FAILED to ping MS Server at"+destIP);
			throw new CatascopiaException("Could not connect to MS Server",CatascopiaException.ExceptionType.CONNECTION);
		}
		this.writeToLog(Level.INFO, "Successfuly ping-ed MS Server at "+destIP);
		
		if (!InitialServerConnector.reportAvailableMetrics(destIP,port,protocol,this.serverID,this.serverIPaddress)){
			this.writeToLog(Level.SEVERE, "FAILED to report available metrics to MS Server at "+destIP);
			throw new CatascopiaException("FAILED to report available metrics to MS Server at "+destIP,
					                       CatascopiaException.ExceptionType.CONNECTION);
		}
		this.writeToLog(Level.INFO, "Successfuly reported available agent metrics to MS Server at "+destIP);
	}

	/**
	 * @param args
	 * @throws CatascopiaException 
	 */
	public static void main(String[] args) throws CatascopiaException {
		try{		
			if (args.length > 0)
				new MonitoringServer(args[0]);
			else
				new MonitoringServer(".");
		}catch(Exception e){
			System.exit(1);
		}
	}

}
