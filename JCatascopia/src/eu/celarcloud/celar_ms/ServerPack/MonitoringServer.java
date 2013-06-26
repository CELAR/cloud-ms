package eu.celarcloud.celar_ms.ServerPack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.utils.CatascopiaLogging;
import eu.celarcloud.celar_ms.utils.CatascopiaNetworking;

/**
 * JCatascopia Monitoring Server receives metrics and manages monitoring 
 * collecting Agents
 *  
 * @author Demetris Trihinas
 *
 */
public class MonitoringServer {
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
	/**
	 * a human readable name to identify MS Server
	 */
	private String serverName;
	
	private Listener listener;
	
	/**
	 * flag to check if logging available
	 */
	private boolean loggingFlag;
	/**
	 * Logger to handle logging
	 */
	private Logger myLogger;
	
	public MonitoringServer() throws CatascopiaException{
		//parse config file
		this.parseConfig();
		this.serverID = UUID.randomUUID().toString();
		//if no name specified us ip address
		this.serverName = this.config.getProperty("server_name",this.serverIPaddress);
		this.serverIPaddress = CatascopiaNetworking.getMyIP();

		this.initListener(); 
				
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
				this.myLogger = CatascopiaLogging.getLogger("ms_server-"+this.serverIPaddress);
				this.myLogger.info(this.serverName+": Created and Initialized");
				this.loggingFlag = true;
			}
			catch (Exception e){
				//Unable to log events
				this.loggingFlag = false;
			}
		else this.loggingFlag = false; //logging turned off by user
	}
	
	/**
	 * initialize the Listener
	 * @throws CatascopiaException 
	 */
	private void initListener() throws CatascopiaException{
		String port = this.config.getProperty("sub_port", "4245");
		String protocol = this.config.getProperty("sub_protocol","tcp");
    	String ip = this.config.getProperty("sub_bind_ip","localhost");
    	String hwm = this.config.getProperty("sub_hwm","32");
		this.listener = new SimpleListener(ip,port,protocol,Long.parseLong(hwm));
		this.listener.activate();
	}

	/**
	 * @param args
	 * @throws CatascopiaException 
	 */
	public static void main(String[] args) throws CatascopiaException {
		MonitoringServer ms_server = new MonitoringServer();
	}

}
