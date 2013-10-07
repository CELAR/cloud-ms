package eu.celarcloud.celar_ms.ProbePack.ProbeLibrary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

import eu.celarcloud.celar_ms.ProbePack.Probe;
import eu.celarcloud.celar_ms.ProbePack.ProbeMetric;
import eu.celarcloud.celar_ms.ProbePack.ProbePropertyType;
import eu.celarcloud.celar_ms.ProbePack.Filters.AdaptiveFilter;

/**
 * 
 * @author Demetris Trihinas
 *
 */
public class TestMemProbe extends Probe{

	private static final String PATH = "/proc/meminfo";
	
	public TestMemProbe(String name, int freq){
		super(name,freq);
		this.addProbeProperty(0,"memTotal",ProbePropertyType.INTEGER,"KB","Total System Memory");
	    this.addProbeProperty(1,"memFree",ProbePropertyType.INTEGER,"KB","Memory Free");
	    this.addProbeProperty(2, "memUsedPercent", ProbePropertyType.DOUBLE, "%", "Memory used percentage");
	    this.turnFilteringOn(2, new AdaptiveFilter(10,0.2,0.8,0.1,0.5));
	}
	
	public TestMemProbe(){
		this("TestMemProbe",10);
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
		    System.out.println("memTotal: "+memTotal);
		    System.out.println("memFree: "+memFree);
//		    System.out.println("memCached: "+memCached);
//		    System.out.println("memUsed: "+memUsed);
//		    System.out.println("memSwapTotal: "+memSwapTotal);
//		    System.out.println("memSwapFree: "+memSwapFree);
		    System.out.println("memUsedPercent: "+memUsedPercent);
//		    System.out.println();
	        
	    values.put(0, memTotal);
	    values.put(1, memFree);
	    values.put(2, memUsedPercent);
	    return new ProbeMetric(values);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestMemProbe memprobe = new TestMemProbe();
		memprobe.activate();
	}
}
