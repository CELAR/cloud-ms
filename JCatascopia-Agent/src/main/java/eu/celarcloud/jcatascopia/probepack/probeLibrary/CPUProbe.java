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
import java.util.Map.Entry;
import java.util.logging.Level;

import eu.celarcloud.jcatascopia.probepack.Probe;
import eu.celarcloud.jcatascopia.probepack.ProbeMetric;
import eu.celarcloud.jcatascopia.probepack.ProbePropertyType;

/**
 * 
 * @author Demetris Trihinas
 *
 */
public class CPUProbe extends Probe{
	
	private static final String PATH = "/proc/stat";
	private HashMap<String,Long> lastValues;
	
	public CPUProbe(String name, int freq){
		super(name,freq);
		this.addProbeProperty(0,"cpuTotal",ProbePropertyType.DOUBLE,"%","Total system CPU usage");
	    this.addProbeProperty(1,"cpuUser",ProbePropertyType.DOUBLE,"%","system USER usage");
	    this.addProbeProperty(2,"cpuSystem",ProbePropertyType.DOUBLE,"%","system SYSTEM usage");
	    this.addProbeProperty(3,"cpuIdle",ProbePropertyType.DOUBLE,"%","system IDLE Usage");
	    this.addProbeProperty(4,"cpuIOwait",ProbePropertyType.DOUBLE,"%","system IO WAIT usage");
	    
	    lastValues = this.calcValues();
	}
	
	public CPUProbe(){
		this("CPUProbe",10);
	}
		
	@Override
	public String getDescription() {
		return "CPUProbe collect's CPU usage stats.";
	}

	@Override
	public ProbeMetric collect() {
		HashMap<String,Long> curValues = this.calcValues();
		HashMap<String,Long> diffValues = new HashMap<String,Long>();
			    
	    for (Entry<String,Long> entry : lastValues.entrySet()) {
	        String key = entry.getKey();
	        Long val = entry.getValue();
			diffValues.put(key, (Long)curValues.get(key) - val);
	    }
	
	    double cpuTotalUsage;
	    double cpuUserUsage;
	    double cpuNiceUsage;
	    double cpuSystemUsage;
	    double cpuIdleUsage;
	    double cpuIOwaitUsage;
	    
	    //remember: Java doesn't throw an exception for zero division on floating points
	    double cpuTotal = 1.0 * diffValues.get("cpuTotal");
	    
	    if(cpuTotal != 0){
	    	 //cpuTotalUsage = (diffValues.get("cpuUser")+diffValues.get("cpuNice")+diffValues.get("cpuSystem")) / cpuTotal * 100;
		    cpuUserUsage = diffValues.get("cpuUser") / cpuTotal * 100;
	        cpuNiceUsage = diffValues.get("cpuNice") / cpuTotal * 100;
	        cpuSystemUsage = diffValues.get("cpuSystem") / cpuTotal * 100;
	        cpuTotalUsage = cpuUserUsage + cpuNiceUsage + cpuSystemUsage;
	        cpuIdleUsage = diffValues.get("cpuIdle") / cpuTotal * 100;
	        cpuIOwaitUsage = diffValues.get("cpuIOWait") /cpuTotal * 100;
	    }
	    else{
	       	cpuTotalUsage = 0.0;
	 	    cpuUserUsage = 0.0;
	 	    cpuNiceUsage = 0.0;
	 	    cpuSystemUsage = 0.0;
	 	    cpuIdleUsage = 0.0;
	 	    cpuIOwaitUsage = 0.0;
	    }
	       
//	    System.out.println("cpuTotalUsage: " + cpuTotalUsage);
//	    System.out.println("cpuUserUsage: " + cpuUserUsage);
//	    System.out.println("cpuNiceUsage: " + cpuNiceUsage);
//	    System.out.println("cpuSystemUsage: " + cpuSystemUsage);
//	    System.out.println("cpuIdleUsage: " + cpuIdleUsage);
//	    System.out.println("cpuI0waitUsage: " + cpuIOwaitUsage);
// 	    System.out.println();
 
        HashMap<Integer,Object> values = new HashMap<Integer,Object>();
        values.put(0, cpuTotalUsage);
	    values.put(1, cpuUserUsage);
	    values.put(2, cpuSystemUsage);
	    values.put(3, cpuIdleUsage);
	    values.put(4, cpuIOwaitUsage);
	 
	    this.lastValues = curValues;
	    
		return new ProbeMetric(values);
	}
	
	private HashMap<String,Long> calcValues(){
		HashMap<String,Long> stats = new HashMap<String,Long>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(PATH)));
			String line;
			while((line = br.readLine())!=null){
				String[] tokenz = null;
				if (line.startsWith("cpu ")){
					tokenz = line.split("\\W+");
					stats.put("cpuUser", Long.parseLong(tokenz[1]));
					stats.put("cpuNice", Long.parseLong(tokenz[2]));
					stats.put("cpuSystem", Long.parseLong(tokenz[3]));
					stats.put("cpuIdle", Long.parseLong(tokenz[4]));
					stats.put("cpuIOWait", Long.parseLong(tokenz[5]));
					stats.put("cpuIrq", Long.parseLong(tokenz[6]));
					stats.put("cpuSoftIrq", Long.parseLong(tokenz[7]));
					if(tokenz.length>8)
						stats.put("cpuSteal", Long.parseLong(tokenz[8]));
					else stats.put("cpuSteal", 0L);
					break;
				}	
			}
	        br.close();
		} catch (FileNotFoundException e){
			this.writeToProbeLog(Level.SEVERE, e);
		} catch (IOException e) {
			this.writeToProbeLog(Level.SEVERE, e);
		}
		    
	    long cpuTotal = 0;
	    for (Long value : stats.values())
	        cpuTotal += value;
	    stats.put("cpuTotal", cpuTotal);	
		return stats;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CPUProbe cpuprobe = new CPUProbe();
		cpuprobe.activate();
	}
}
