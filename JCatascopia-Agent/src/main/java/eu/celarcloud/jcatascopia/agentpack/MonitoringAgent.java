package eu.celarcloud.jcatascopia.agentpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.celarcloud.jcatascopia.agentpack.aggregators.IAggregator;
import eu.celarcloud.jcatascopia.agentpack.distributors.IDistributor;
import eu.celarcloud.jcatascopia.agentpack.utils.CatascopiaLogging;
import eu.celarcloud.jcatascopia.agentpack.utils.CatascopiaNetworking;
import eu.celarcloud.jcatascopia.agentpack.utils.CatascopiaPackaging;
import eu.celarcloud.jcatascopia.agentpack.utils.CatascopiaProbeFactory;
import eu.celarcloud.jcatascopia.exceptions.CatascopiaException;
import eu.celarcloud.jcatascopia.probepack.IProbe;

/**
 * JCatascopia Monitoring Agent manages metric collecting probes
 * and distributes metrics to the Monitoring Server
 *  
 * @author Demetris Trihinas
 *
 */
public class MonitoringAgent implements IJCatascopiaAgent{
	//path to JCatascopia Agent Directory
	private String JCATASCOPIA_AGENT_HOME;
	//path to config file
	private static final String CONFIG_PATH = "resources"+File.separator+"agent.properties";
	//path to internal private file
	private static final String AGENT_PRIVATE_FILE = "resources"+File.separator+"agent_private.properties";
	//path to probe library
	private static final String PROBE_LIB_PATH = "eu.celarcloud.jcatascopia.probepack.probeLibrary.";
	/**
	 * properties read from config file
	 */
	private Properties config;
	/**
	 * give Agent a unique ID
	 */
	private String agentID;
	/**
	 * Agent IP address
	 */
	private String agentIPaddress;
	/**
	 * HashMaps to easy retrieve probes either using ID or probeName
	 */
	private HashMap<String,IProbe> probeNameMap;
	/**
	 * collector grabs metrics from metricQueue to be parsed
	 */
	private MetricCollector collector;
	private final LinkedBlockingQueue<String> metricQueue = new LinkedBlockingQueue<String>(64);
		
	/**
	 * distributor publishes metrics to the MS Server
	 */
	private DistributorWorker distributorWorker;
	
	/**
	 * flag to check if logging available
	 */
	private boolean loggingFlag;
	/**
	 * Logger to handle logging
	 */
	private Logger myLogger;
	
	protected IAggregator aggregator;
	
	private ProbeController probeController;
	
	private boolean debugMode;
	private boolean useServer;
		
	/**
	 * 
	 * @param agentDirPath
	 * @throws CatascopiaException
	 */
	public MonitoringAgent(String agentDirPath) throws CatascopiaException {
		//path to JCatascopia Agent Directory
		this.JCATASCOPIA_AGENT_HOME = agentDirPath;
		//parse config file
		this.parseConfig();
		//get agentID or create one
		this.agentID = this.getAgentIDFromFile();
		//get ip address
		this.agentIPaddress = CatascopiaNetworking.getMyIP();
		
		this.probeNameMap = new HashMap<String,IProbe>();
		
		this.initLogging(); 
		
		this.instantiateProbes();
//		this.detectAvailableProbes();
//		this.initActivateProbes();

		//ping server, tell it we are here and report available metrics
		//if successful then we can start distributing metrics
		try{
			this.useServer = Boolean.parseBoolean(this.config.getProperty("use_server", "true"));
		}catch(Exception e){
			this.useServer= true;
		}
		if (this.useServer)
			this.establishConnectionWithServer();	
		
		this.initAggregator();
		
		this.initDistributor();
		
		this.collector = new MetricCollector(this.metricQueue,this.aggregator,this);
		this.collector.activate();		
		
		this.initProbeController();
		
		try{
			this.debugMode = Boolean.parseBoolean(this.config.getProperty("debug_mode", "false"));
		}catch(Exception e){
			this.debugMode = false;
		}
	}
	
	//parse the configuration file
	private void parseConfig() throws CatascopiaException{
		this.config = new Properties();
		//load config properties file
		try {				
			FileInputStream fis = new FileInputStream(JCATASCOPIA_AGENT_HOME+File.separator+CONFIG_PATH);
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
				this.myLogger = CatascopiaLogging.getLogger(this.JCATASCOPIA_AGENT_HOME, "JCatascopia-Agent");
				this.myLogger.info("JCatascopia-Agent: Created and Initialized");
				this.loggingFlag = true;
			}
			catch (Exception e){
				//Unable to log events
				this.loggingFlag = false;
			}
		else this.loggingFlag = false; //logging turned off by user
	}
	
	/**
	 * method that logs messages to the MS Agent log
	 */
	public void writeToLog(Level level, Object msg){
		if(this.loggingFlag)
			this.myLogger.log(level, "JCatascopia-Agent"+": "+msg);
	}
	
	/**
	 * method that LOADS and ACTIVATES Probes defined in config file
	 * 
	 */
	private void instantiateProbes(){
		String probe_str = this.config.getProperty("probes", "all");
		String probe_exclude_str = this.config.getProperty("probes_exclude", "");
		String probe_external = this.config.getProperty("probes_external", "");
				
		try{
			ArrayList<String> availableProbeList = this.listAvailableProbeClasses();
			//user wants to instantiate all available probes with default params
			//TODO - allow user to parameterize probes when selecting to add all probes
			if (probe_str.equals("all")){
				for(String s:availableProbeList) 
					this.probeNameMap.put(s, null);
				
				//user wants to instantiate all available probes except the ones specified for exclusion
				if (!probe_exclude_str.equals("")){
					String[] probe_list = probe_exclude_str.split(";");
					for(String s:probe_list)
						this.probeNameMap.remove(s.split(",")[0]);
				}
				
				//instantiate
				for(Entry<String,IProbe> p:this.probeNameMap.entrySet()){
					try{
						IProbe tempProbe = CatascopiaProbeFactory.newInstance(PROBE_LIB_PATH,p.getKey());
						tempProbe.attachQueue(this.metricQueue);
						tempProbe.attachLogger(this.myLogger);
						p.setValue(tempProbe);
					}
					catch (CatascopiaException e) {
						this.writeToLog(Level.SEVERE, e);
						continue;
					}	
				}
			}
			//user wants to instantiate specific probes (and may set custom params)
			else{
				String[] probe_list = probe_str.split(";");
				for(String s:probe_list){
					try{
						String[] params = s.split(",");
						IProbe tempProbe = CatascopiaProbeFactory.newInstance(PROBE_LIB_PATH,params[0]);
						tempProbe.attachQueue(this.metricQueue);
						tempProbe.attachLogger(this.myLogger);
						if(params.length>1) //user wants to define custom collecting period
							tempProbe.setCollectPeriod(Integer.parseInt(params[1]));
						this.probeNameMap.put(params[0], tempProbe);
					}
					catch (CatascopiaException e) {
						this.writeToLog(Level.SEVERE, e);
						continue;
					}	
				}
				
			}
						
			//activate Agent probes
			this.activateAllProbes();
			
			//deploy external probes located in a custom user-defined path
			if (!probe_external.equals("")){
				String[] probe_list = probe_external.split(";");
				for(String s:probe_list){
					try{
						String[] params = s.split(",");
						String pclass = params[0];
						String ppath = params[1];
						this.deployProbeAtRuntime(ppath, pclass);
					}
					catch (ArrayIndexOutOfBoundsException e){
						this.writeToLog(Level.SEVERE, "External Probe deployment error. Either the probe class name of classpath are not correctly provided");
					}
				}
			}				
			
			//log probe list added to Agent
			String l = " ";
			for(Entry<String,IProbe> entry:this.probeNameMap.entrySet())
				l += entry.getKey() + ",";
			this.writeToLog(Level.INFO,"Probes Activated: "+l.substring(0, l.length()-1));
		}
		catch (CatascopiaException e){
			this.writeToLog(Level.SEVERE, e);
		}
	}
	
	/**
	 * 
	 * @return list of available probes in Probe Library
	 * @throws CatascopiaException
	 */
	public ArrayList<String> listAvailableProbeClasses() throws CatascopiaException{
		ArrayList<String> list =  CatascopiaPackaging.listClassesInPackage(PROBE_LIB_PATH);
		return list;
	}
	
	/**
	 * DEPRICATED
	 * 
	 * method that loads ALL probes in Probe Library to the MS Agent
	 * this method does NOT activate any Probes
	 */
	private void detectAvailableProbes(){
		ArrayList<String> availableProbeList;
		try{
			availableProbeList = this.listAvailableProbeClasses();
						
			for(int i=0;i<availableProbeList.size();i++){
				try{
					IProbe tempProbe = CatascopiaProbeFactory.newInstance(PROBE_LIB_PATH,availableProbeList.get(i));
					//add detected probe to probe map
					this.addProbe(tempProbe);
				}
				catch (CatascopiaException e) {
					this.writeToLog(Level.SEVERE, e);
					continue;
				}	
			}
		}
		catch (CatascopiaException e){
			this.writeToLog(Level.SEVERE, e);
		}
	}
	
	/**
	 * DEPRICATED
	 * used with detectProbes() to activate user-defined Probes 
	 */
	private void initActivateProbes(){
		String probestr = this.config.getProperty("probes", "all");
		try{
			if (probestr.equals("all"))
				//if all then activate all probes and use default collecting frequency specified by probe developer
				this.activateAllProbes();
			else if (!probestr.equals("")){
				//user specified specific which probes to activate
				String[] probe_list = probestr.split(";");
				String[] params;	
				for(String p:probe_list){
					params = p.split(",");
					if(params.length>1)
						//user wants custom collecting periods
						this.probeNameMap.get(params[0]).setCollectPeriod(Integer.parseInt(params[1]));
					this.activateProbe(params[0]);
				}
			}
			this.getProbe("StaticInfoProbe").pull();
		} 
		catch(CatascopiaException e){
			this.writeToLog(Level.SEVERE, e);
		}
		catch(Exception e){
			//config file error
			this.writeToLog(Level.SEVERE, e);
		}	

	}
	
	private void establishConnectionWithServer() throws CatascopiaException{
		String protocol = this.config.getProperty("control_protocol","tcp").toLowerCase();

		if (protocol.equals("rest")){
			String url = this.config.getProperty("control_url","");

			if (!RESTServerConnector.connect(url,this.agentID,this.agentIPaddress,this.probeNameMap)){
				this.writeToLog(Level.SEVERE, "FAILED connect to Monitoring Server at: "+url);
				throw new CatascopiaException("Could not connect to Monitoring Server",CatascopiaException.ExceptionType.CONNECTION);
			}
			else this.writeToLog(Level.INFO, "Successfuly connected to Server at: "+url);
		}
		else{
			String serverIP = this.config.getProperty("server_ip", "127.0.0.1");
			String port = this.config.getProperty("control_port", "4245");
	//		String protocol = this.config.getProperty("control_protocol","tcp");
			
	/*		if (!ServerConnector.connect(serverIP,port,protocol,this.agentID,this.agentIPaddress)){
				this.writeToLog(Level.SEVERE, "FAILED to ping MS Server at"+serverIP);
				throw new CatascopiaException("Could not connect to MS Server",CatascopiaException.ExceptionType.CONNECTION);
			}
			this.writeToLog(Level.INFO, "Successfuly ping-ed MS Server at "+serverIP);
			
			if (!ServerConnector.reportAvailableMetrics(serverIP,port,protocol,this.agentID,this.agentIPaddress,this.probeNameMap)){
				this.writeToLog(Level.SEVERE, "FAILED to report available metrics to MS Server at "+serverIP);
				throw new CatascopiaException("FAILED to report available metrics to MS Server at "+serverIP,
						                       CatascopiaException.ExceptionType.CONNECTION);
			}
			this.writeToLog(Level.INFO, "Successfuly reported available agent metrics to MS Server at "+serverIP);
		*/
			if (!ServerConnector.connect(serverIP, port, this.agentID, this.agentIPaddress, this.probeNameMap)){
				this.writeToLog(Level.SEVERE, "FAILED to CONNECT to Monitoring Server at: "+serverIP);
				throw new CatascopiaException("Could not CONNECT to Monitoring Server",CatascopiaException.ExceptionType.CONNECTION);
			}
			else this.writeToLog(Level.INFO, "Successfuly CONNECTED to Server at: "+serverIP);
		}
	}
	
	/**
	 * instantiate and initialize Aggregator interface 
	 * @throws CatascopiaException 
	 */
	private void initAggregator() throws CatascopiaException{
		String inter = this.config.getProperty("aggregator_interface", "StringAggregator");
		
		Class<?>[] myArgs = new Class[3];
		myArgs[0] = String.class;
        myArgs[1] = String.class;
        myArgs[2] = IJCatascopiaAgent.class;
        String path = "eu.celarcloud.jcatascopia.agentpack.aggregators."+inter;
        try{
	        Class<IAggregator> _tempClass = (Class<IAggregator>) Class.forName(path);
	        Constructor<IAggregator> _tempConst = _tempClass.getDeclaredConstructor(myArgs);
			this.aggregator = _tempConst.newInstance(this.agentID,this.agentIPaddress,this);
			this.writeToLog(Level.INFO, inter+">> Successully instantiated and initialized");	
        }
        catch (ClassNotFoundException e){
			this.writeToLog(Level.SEVERE, e);
			throw new CatascopiaException(e.getMessage(),CatascopiaException.ExceptionType.AGGREGATOR);
		}
		catch(Exception e){
			this.writeToLog(Level.SEVERE, e);
			throw new CatascopiaException(e.getMessage(),CatascopiaException.ExceptionType.AGGREGATOR);
		}
	}
	
	/**
	 * initialize the Distributer
	 * @throws CatascopiaException 
	 */
	private void initDistributor() throws CatascopiaException{
		//Distributor settings
    	String inter = this.config.getProperty("distributor_interface", "TCPDistributor");
		String port = this.config.getProperty("distributor_port", "4242");
    	String ip = this.config.getProperty("server_ip","localhost");
    	String url = this.config.getProperty("distributor_url","");

    	//Aggregator settings
    	long agg_interval = Long.parseLong(this.config.getProperty("aggregator_interval"))*1000;
    	int agg_buf = Integer.parseInt(this.config.getProperty("aggregator_buffer_size"));
    			
		Class<?>[] myArgs = new Class[3];
		myArgs[0] = String.class;
        myArgs[1] = String.class;
        myArgs[2] = String.class;

        String path = "eu.celarcloud.jcatascopia.agentpack.distributors."+inter;
        try{
	        Class<IDistributor> _tempClass = (Class<IDistributor>) Class.forName(path);
	        Constructor<IDistributor> _tempConst = _tempClass.getDeclaredConstructor(myArgs);
	        IDistributor distributor = _tempConst.newInstance(ip,port,url);
			
	        this.distributorWorker = new DistributorWorker(distributor,this.aggregator,agg_interval,agg_buf,this);
			distributorWorker.activate();
			
			this.writeToLog(Level.INFO, inter+">> Successully instantiated and initialized");	
        }
        catch (ClassNotFoundException e){
			this.writeToLog(Level.SEVERE, e);
			throw new CatascopiaException(e.getMessage(),CatascopiaException.ExceptionType.AGGREGATOR);
		}
		catch(Exception e){
			this.writeToLog(Level.SEVERE, e);
			throw new CatascopiaException(e.getMessage(),CatascopiaException.ExceptionType.AGGREGATOR);
		} 
	}
	
	private void initProbeController(){
		if (this.config.getProperty("probe_controller_turnOn", "false").equals("true")){
			String ip = this.config.getProperty("probe_controller_ip","localhost");
			String port = this.config.getProperty("probe_controller_port","4243");
			
			this.probeController = new ProbeController(ip, port, this.metricQueue, this);
			this.probeController.activate();
			this.writeToLog(Level.INFO, "ProbeController enabled with parameters: "+ip+", "+port);
		}
	}
	
	/**
	 * 
	 * @return ms agent ID
	 */
	public String getAgentID(){
		return this.agentID;
	}
		
	/**
	 * 
	 * @return ms agent IP address
	 */
	public String getAgentIP(){
		return this.agentIPaddress;
	}
	
	/**
	 * add probe to agent
	 * @param p
	 */
	public void addProbe(IProbe p){
		this.probeNameMap.put(p.getProbeName(), p);
		p.attachQueue(this.metricQueue);
		p.attachLogger(this.myLogger);
	}
	
	/**
	 * remove probe from agent
	 * @param probeID
	 * @throws CatascopiaException
	 */
	public void removeProbeByName(String probeName) throws CatascopiaException{
		if (this.probeNameMap.containsKey(probeName)){
			IProbe probe = this.probeNameMap.get(probeName);
			probe.removeQueue();
			probe.terminate();
			this.probeNameMap.remove(probeName);
		}
		else
			throw new CatascopiaException("Remove Probe Failed, probe name given does not exist: "+probeName, 
										   CatascopiaException.ExceptionType.KEY);
	}
	
	/**
	 * 
	 * @param probeName
	 * @return probe specified by probeName
	 * @throws CatascopiaException
	 */
	public IProbe getProbe(String probeName) throws CatascopiaException{
		if (this.probeNameMap.containsKey(probeName))
			return this.probeNameMap.get(probeName);
		else
			throw new CatascopiaException("Get Probe Failed, probe name given does not exist: "+probeName, 
										   CatascopiaException.ExceptionType.KEY);
	}
	
	/**
	 * terminate agent. all probes are first terminated.
	 */
	public void terminate(){
		//terminate probes
		for (Entry<String,IProbe> entry :this.probeNameMap.entrySet())
			entry.getValue().terminate();
		//terminate collector
		this.collector.terminate();
		this.distributorWorker.terminate();
	}
		
	public HashMap<String,IProbe> getProbeMap(){
		return this.probeNameMap;
	}
		
	public void activateProbe(String probeName) throws CatascopiaException{
		if (this.probeNameMap.containsKey(probeName)){
			this.probeNameMap.get(probeName).activate();
		}
		else
			throw new CatascopiaException("Activate Probe Failed, probe name given does not exist: "+probeName, 
										   CatascopiaException.ExceptionType.KEY);
	}
	
	public void activateAllProbes(){
		for(String name : this.probeNameMap.keySet())
			try{
//				if (!name.equals("StaticInfoProbe"))
					this.activateProbe(name);
			} 
			catch (CatascopiaException e) {
				continue;
			}
	}
	
	public void deactivateProbe(String probeName) throws CatascopiaException{
		if (this.probeNameMap.containsKey(probeName)){
			this.probeNameMap.get(probeName).deactivate();
		}
		else
			throw new CatascopiaException("Deactivate Probe Failed, probe name given does not exist: "+probeName, 
										   CatascopiaException.ExceptionType.KEY);
	}
		
	private String getAgentIDFromFile() throws CatascopiaException{
    	Properties prop = new Properties();
    	String id;
    	try {
			String agentfile = JCATASCOPIA_AGENT_HOME + File.separator + AGENT_PRIVATE_FILE;
			if((new File(agentfile).isFile())){
				//load agent_private properties file
				FileInputStream fis = new FileInputStream(agentfile);
				prop.load(fis);
				if (fis != null)
		    		fis.close();
				id = prop.getProperty("agentID",UUID.randomUUID().toString().replace("-", ""));
			}
			else{
				//first time agent started. Store assigned id to file
				id = UUID.randomUUID().toString().replace("-", "");
				prop.setProperty("agentID", id);
				prop.store(new FileOutputStream(agentfile), null);
			}
    	} 
		catch (FileNotFoundException e){
			throw new CatascopiaException("agent_private file not found", CatascopiaException.ExceptionType.FILE_ERROR);
		} 
		catch (IOException e){
			throw new CatascopiaException("agent_file parsing error", CatascopiaException.ExceptionType.FILE_ERROR);
		}	
    	return id;
	}
	
	public boolean inDebugMode(){
		return this.debugMode;
	}
	
	public Properties getConfig(){
		return this.config;
	}
	
	public void deployProbeAtRuntime(String probeClassContainer, String probeClass) throws CatascopiaException{		
		try {
			URL myurl = new URL("file://"+probeClassContainer);
			URLClassLoader classloader = URLClassLoader.newInstance(new URL[]{myurl});
			Class<IProbe> myclass = (Class<IProbe>) classloader.loadClass(probeClass);
			IProbe p = myclass.newInstance();
			p.attachQueue(this.metricQueue);
			p.attachLogger(this.myLogger);
			p.activate();
			this.probeNameMap.put(p.getProbeName(), p);
			this.writeToLog(Level.INFO, "new Probe activated: " + p.getProbeName() + ", container path: " + probeClassContainer);
		} 
		catch (MalformedURLException e) {
			throw new CatascopiaException(e.getMessage(),CatascopiaException.ExceptionType.PROBE_EXISTANCE);
		} 
		catch (ClassNotFoundException e) {
			throw new CatascopiaException(e.getMessage(),CatascopiaException.ExceptionType.PROBE_EXISTANCE);
		} 
		catch (InstantiationException e) {
			throw new CatascopiaException(e.getMessage(),CatascopiaException.ExceptionType.PROBE_EXISTANCE);
		} 
		catch (IllegalAccessException e) {
			throw new CatascopiaException(e.getMessage(),CatascopiaException.ExceptionType.PROBE_EXISTANCE);
		}
	}
	
	/**
	 * @param args
	 * @throws CatascopiaException 
	 */
	public static void main(String[] args) throws CatascopiaException{
		try{	
			if (args.length > 0)
				new MonitoringAgent(args[0]);
			else
				new MonitoringAgent(".");
		}catch(Exception e){
			System.exit(1);
		}
	}
}