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
package eu.celarcloud.jcatascopia.probepack.probeLibrary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

import eu.celarcloud.jcatascopia.probepack.Probe;
import eu.celarcloud.jcatascopia.probepack.ProbeMetric;
import eu.celarcloud.jcatascopia.probepack.ProbePropertyType;

/**
 * 
 * @author Demetris Trihinas
 *
 */
public class MemoryProbe extends Probe{

	private static final String PATH = "/proc/meminfo";
	
	public MemoryProbe(String name, int freq){
		super(name,freq);
		this.addProbeProperty(0,"memTotal",ProbePropertyType.INTEGER,"KB","Total System Memory");
	    this.addProbeProperty(1,"memFree",ProbePropertyType.INTEGER,"KB","Memory Free");
	    this.addProbeProperty(2,"memCache",ProbePropertyType.INTEGER,"KB","Cached Memory");
	    this.addProbeProperty(3,"memUsed",ProbePropertyType.INTEGER,"KB","Memory Used");
	    this.addProbeProperty(4,"memSwapTotal",ProbePropertyType.INTEGER,"KB","Total Swap Memory");
	    this.addProbeProperty(5,"memSwapFree",ProbePropertyType.INTEGER,"KB","Swap Memory Free");
	    this.addProbeProperty(6, "memUsedPercent", ProbePropertyType.DOUBLE, "%", "Memory used percentage");
	}
	
	public MemoryProbe(){
		this("MemoryProbe",20);
	}
		
	public String getDescription(){
		return "Memory Probe collect's memory stats";
	}
	
	public ProbeMetric collect(){
		int memTotal = -1;
		int memFree = -1;
		int memCached = -1;
		int memUsed = -1;
		int memSwapTotal = -1;
		int memSwapFree = -1;
		double memUsedPercent = -1;
        HashMap<Integer,Object> values = new HashMap<Integer,Object>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(PATH)));
			String line;
			while((line = br.readLine())!=null){
				if (line.startsWith("MemTotal")){
					memTotal = Integer.parseInt((line.split("\\W+")[1]));
					continue;
				}
				if (line.startsWith("MemFree")){
					memFree = Integer.parseInt((line.split("\\W+")[1]));
					continue;
				}
				if (line.startsWith("Cached")){
					memCached = Integer.parseInt((line.split("\\W+")[1]));
					continue;
				}
				if (line.startsWith("SwapTotal")){
					memSwapTotal = Integer.parseInt((line.split("\\W+")[1]));
					continue;
				}
				if (line.startsWith("SwapFree")){
					memSwapFree = Integer.parseInt((line.split("\\W+")[1]));
					break;
				}					
			}
			br.close();    
		} 
		catch (FileNotFoundException e){
			this.writeToProbeLog(Level.SEVERE, e);
		} 
		catch (IOException e) {
			this.writeToProbeLog(Level.SEVERE, e);
		}		        
	         
	    memUsed = memTotal - memFree - memCached;
	    memUsedPercent = (100.0*memUsed) / memTotal;
//		    System.out.println("memTotal: "+memTotal);
//		    System.out.println("memFree: "+memFree);
//		    System.out.println("memCached: "+memCached);
//		    System.out.println("memUsed: "+memUsed);
//		    System.out.println("memSwapTotal: "+memSwapTotal);
//		    System.out.println("memSwapFree: "+memSwapFree);
//		    System.out.println("memUsedPercent: "+memUsedPercent);
//		    System.out.println();
	        
	    values.put(0, memTotal);
	    values.put(1, memFree);
	    values.put(2, memCached);
	    values.put(3, memUsed);
	    values.put(4, memSwapTotal);
	    values.put(5, memSwapFree);
	    values.put(6, memUsedPercent);
	    return new ProbeMetric(values);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MemoryProbe memprobe = new MemoryProbe();
		memprobe.activate();
	}
}
