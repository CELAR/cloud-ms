package eu.celarcloud.celar_ms.AgentPack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ProbePack.IProbe;
import eu.celarcloud.celar_ms.ProbePack.ProbeMetric;
import eu.celarcloud.celar_ms.utils.CatascopiaLogging;
import eu.celarcloud.celar_ms.utils.CatascopiaNetworking;
import eu.celarcloud.celar_ms.utils.CatascopiaPackaging;
import eu.celarcloud.celar_ms.utils.CatascopiaProbeFactory;

/**
 * JCatascopia Monitoring Agent manages metric collecting probes
 * and distributes metrics to the Application Monitoring Server
 *  
 * @author Demetris Trihinas
 *
 */
public class MonitoringAgent implements IAgent{
	//path to config file
	private static final String CONFIG_PATH = "resources"+File.separator+"agent.properties";
	//path to probe library
	private static final String PROBE_LIB_PATH = "eu.celarcloud.celar_ms.ProbePack.ProbeLibrary";
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
	 * a human readable name to identify MS Agent
	 */
	private String agentName;
	/**
	 * HashMaps to easy retrieve probes either using ID or probeName
	 */
	private HashMap<String,IProbe> probeIDMap;
	private HashMap<String,IProbe> probeNameMap;
	/**
	 * collector grabs metrics from metricQueue to be parsed
	 */
	private MetricCollector collector;
	private final LinkedBlockingQueue<ProbeMetric> metricQueue = new LinkedBlockingQueue<ProbeMetric>(128);
	
	private AgentAggregator aggregator;
	
	/**
	 * distributor publishes metrics to the MS Application Server
	 */
	private Distributor distributor;
	private final LinkedBlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(64);
	
	private ConsoleScanner consoleScanner;
	/**
	 * flag to check if logging available
	 */
	private boolean loggingFlag;
	/**
	 * Logger to handle logging
	 */
	private Logger myLogger;
	
	/**
	 * JCatascopia Monitoring Agent constructor
	 * @throws CatascopiaException
	 */
	public MonitoringAgent() throws CatascopiaException {
		//parse config file
		this.parseConfig();
		this.agentID = UUID.randomUUID().toString();
		this.agentIPaddress = CatascopiaNetworking.getMyIP();
		//if no name specified us ip address
		this.agentName = this.config.getProperty("agent_name",this.agentIPaddress);

		this.probeIDMap = new HashMap<String,IProbe>();
		this.probeNameMap = new HashMap<String,IProbe>();
		
		this.aggregator = new AgentAggregator(this.msgQueue);
		
		this.collector = new MetricCollector(this.metricQueue,this, this.probeIDMap,this.aggregator);
		this.collector.start();
		this.initProbes();
		
		this.initDistributor();
		
//		this.consoleScanner = new ConsoleScanner(this);
//		this.consoleScanner.start();
		
		this.initLogging(); 
	}
	
	//parse the configuration file
	private void parseConfig() throws CatascopiaException{
		this.config = new Properties();
		//load config properties file
		try {
			FileInputStream fis = new FileInputStream(CONFIG_PATH);
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
				this.myLogger = CatascopiaLogging.getLogger("ms_agent-"+this.agentName);
				this.myLogger.info(this.agentName+": Created and Initialized");
				this.loggingFlag = true;
			}
			catch (Exception e){
				//Unable to log events
				this.loggingFlag = false;
			}
		else this.loggingFlag = false; //logging turned off by user
	}
	
	/**
	 * initialize the Distributer
	 */
	private void initDistributor(){
		String port = this.config.getProperty("port", "4242");
		String protocol = this.config.getProperty("protocol","tcp");
    	String ip = this.config.getProperty("server_ip","localhost");
    	String hwm = this.config.getProperty("hwm","16");
		this.distributor = new Distributor(ip,port,protocol,Long.parseLong(hwm),this.msgQueue);
		distributor.activate();
	}
	
	/**
	 * method that activates ALL available probes in Probe Library at once
	 */
	private void initProbes(){
		ArrayList<String> list;
		try{
			list = this.listAvailableProbeClasses();
			for(int i=0;i<list.size();i++){
				try{
					IProbe tempProbe = CatascopiaProbeFactory.newInstance(list.get(i));
					this.addProbe(tempProbe);
				}
				catch (CatascopiaException e) {
					//TODO log this exception. Couldn't instantiate this probe
					continue;
				}	
			}
		}
		catch (CatascopiaException e){
			//TODO log this exception. Couldn't retrieve list with available probes
			e.printStackTrace();
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
	 * 
	 * @return ms agent ID
	 */
	public String getAgentID(){
		return this.agentID;
	}
	
	/**
	 * 
	 * @return ms agent name
	 */
	public String getAgentName(){
		return this.agentName;
	}
	
	/**
	 * sets ms agent name to specified by parameter name
	 * @param name
	 */
	public void setAgentName(String name){
		this.agentName = name;
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
		this.probeIDMap.put(p.getProbeID(), p);
		this.probeNameMap.put(p.getProbeName(), p);
		p.attachQueue(this.metricQueue);
	}
	
	/**
	 * remove probe from agent
	 * @param probeID
	 * @throws CatascopiaException
	 */
	public void removeProbeByID(String probeID) throws CatascopiaException{
		if (this.probeIDMap.containsKey(probeID)){
			IProbe probe = this.probeIDMap.get(probeID);
			String probeName = probe.getProbeName();
			probe.removeQueue();
			probe.terminate();
			this.probeIDMap.remove(probeID);
			this.probeNameMap.remove(probeName);
		}
		else
			throw new CatascopiaException("Remove Probe Failed, probeID given does not exist: "+probeID, 
										   CatascopiaException.ExceptionType.KEY);
	}
	
	/**
	 * remove probe from agent
	 * @param probeID
	 * @throws CatascopiaException
	 */
	public void removeProbeByName(String probeName) throws CatascopiaException{
		if (this.probeNameMap.containsKey(probeName)){
			IProbe probe = this.probeNameMap.get(probeName);
			String probeID = probe.getProbeID();
			probe.removeQueue();
			probe.terminate();
			this.probeNameMap.remove(probeName);
			this.probeIDMap.remove(probeID);
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
		for (Entry<String,IProbe> entry :this.probeIDMap.entrySet()){
			entry.getValue().terminate();
		}
		//terminate collector
		this.collector.terminate();
	}
		
	public HashMap<String,IProbe> getProbeMap(){
		return this.probeIDMap;
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
		
	public void changeProbeName(IProbe probe,String name){
		this.probeNameMap.remove(probe.getProbeName());
		probe.setProbeName(name);
		this.probeNameMap.put(name, probe);
	}
	
	public synchronized void setCollectorWritingStatus(boolean status){
		this.collector.setConsoleWriting(status);
	}
	
	/**
	 * @param args
	 * @throws CatascopiaException 
	 */
	public static void main(String[] args) throws CatascopiaException{
		MonitoringAgent magent = new MonitoringAgent();
		magent.activateAllProbes();
	}
}
