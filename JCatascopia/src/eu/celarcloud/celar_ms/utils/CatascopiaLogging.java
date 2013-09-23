package eu.celarcloud.celar_ms.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CatascopiaLogging {
	private static final int FILE_SIZE = 2*1024*1024;
	
	/**
	 * initiate logging.
	 * LOG FILE PATH = "./logs"
	 * @param name - log file name
	 * @return - a logger
	 * @throws SecurityException
	 * @throws IOException
	 */
	public static Logger getLogger(String path, String name) throws SecurityException, IOException{
		File logfolder = new File(path+File.separator+"logs");
		if (!logfolder.isDirectory())
			logfolder.mkdir();			
		
		String logpath = logfolder+File.separator+name+".log";
		Logger LOGGER = Logger.getLogger(name); 
		FileHandler fileHandler = new FileHandler(logpath,FILE_SIZE, 5, true); 
		fileHandler.setFormatter(new SimpleFormatter());
		LOGGER.addHandler(fileHandler);
		return LOGGER;
	}
}
