package eu.celarcloud.celar_ms.ProbePack.ProbeLibrary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import eu.celarcloud.celar_ms.ProbePack.Probe;
import eu.celarcloud.celar_ms.ProbePack.ProbeMetric;
import eu.celarcloud.celar_ms.ProbePack.ProbePropertyType;


public class CPUProbe extends Probe{
	
	private static final String PATH = "/proc/stat";
	private HashMap<String,Integer> lastValues;
	
	public CPUProbe(String name, int freq){
		super(name,freq);
		this.addProbeProperty(0,"cpuTotalUsage",ProbePropertyType.DOUBLE,"%","Total system CPU usage");
	    this.addProbeProperty(1,"cpuUserUsage",ProbePropertyType.DOUBLE,"%","system USER usage");
	    this.addProbeProperty(2,"cpuNiceUsage",ProbePropertyType.DOUBLE,"%","system NICE usage");
	    this.addProbeProperty(3,"cpuSystemUsage",ProbePropertyType.DOUBLE,"%","system SYSTEM usage");
	    this.addProbeProperty(4,"cpuIdleUsage",ProbePropertyType.DOUBLE,"%","system IDLE Usage");
	    this.addProbeProperty(5,"cpuIOwaitUsage",ProbePropertyType.DOUBLE,"%","system IO WAIT usage");
	    
	    lastValues = this.calcValues();
	}
	
	public CPUProbe(){
		this("CPUProbe",2);
	}
		
	@Override
	public String getDescription() {
		return "CPUProbe collect's CPU usage stats.";
	}

	@Override
	public ProbeMetric collect() {
		HashMap<String,Integer> curValues = this.calcValues();
		HashMap<String,Integer> diffValues = new HashMap<String,Integer>();
			    
	    for (Entry<String,Integer> entry : lastValues.entrySet()) {
	        String key = entry.getKey();
	        Integer val = entry.getValue();
			diffValues.put(key, (Integer)curValues.get(key) - val);
	    }
	
	    double cpuTotalUsage;
	    double cpuUserUsage;
	    double cpuNiceUsage;
	    double cpuSystemUsage;
	    double cpuIdleUsage;
	    double cpuIOwaitUsage;
	    
	    //remember: Java doesn't throw an exception for zero division on floating points
	    double cpuTotal = 1.0*diffValues.get("cpuTotal");
	    
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
	    values.put(2, cpuNiceUsage);
	    values.put(3, cpuSystemUsage);
	    values.put(4, cpuIdleUsage);
	    values.put(5, cpuIOwaitUsage);
	    
	    this.lastValues = curValues;
	    
		return new ProbeMetric(values);
	}
	
	private HashMap<String,Integer> calcValues(){
		HashMap<String,Integer> stats = new HashMap<String,Integer>();
		
		File cpufile = new File(PATH);
		if (cpufile.isFile()){
		    try {
				BufferedReader br = new BufferedReader(new FileReader(cpufile));
				String line;
				while((line = br.readLine())!=null){
					String[] tokenz = null;
					if (line.startsWith("cpu ")){
						tokenz = line.split("\\W+");
						stats.put("cpuUser", Integer.parseInt(tokenz[1]));
						stats.put("cpuNice", Integer.parseInt(tokenz[2]));
						stats.put("cpuSystem", Integer.parseInt(tokenz[3]));
						stats.put("cpuIdle", Integer.parseInt(tokenz[4]));
						stats.put("cpuIOWait", Integer.parseInt(tokenz[5]));
						stats.put("cpuIrq", Integer.parseInt(tokenz[6]));
						stats.put("cpuSoftIrq", Integer.parseInt(tokenz[7]));
						if(tokenz.length>8)
							stats.put("cpuSteal", Integer.parseInt(tokenz[8]));
						else stats.put("cpuSteal", 0);
						break;
					}	
				}
		        br.close();
			} catch (FileNotFoundException e){
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		    
		    int cpuTotal = 0;
		    for (Integer value : stats.values())
		        cpuTotal += value;
		    stats.put("cpuTotal", cpuTotal);
		}	
		return stats;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CPUProbe cpuprobe = new CPUProbe("CPUProbe1",4);
		cpuprobe.activate();
	}
}
