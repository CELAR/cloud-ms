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
package eu.celarcloud.jcatascopia.agentpack.utils;

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
