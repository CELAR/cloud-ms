package eu.celarcloud.celar_ms.ProbePack.ProbeLibrary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;

import eu.celarcloud.celar_ms.ProbePack.Probe;
import eu.celarcloud.celar_ms.ProbePack.ProbeMetric;
import eu.celarcloud.celar_ms.ProbePack.ProbePropertyType;

/**
 * 
 * @author Demetris Trihinas
 *
 */
public class DiskStatsProbe extends Probe{
	
	private static final String PATH = "/proc/diskstats";
	private static final int BYTES_PER_SECTOR = 512;

	private HashMap<String,Long> lastValues;
	private long lastTime;
	
	public DiskStatsProbe(String name, int freq){
		super(name,freq);
		this.addProbeProperty(0,"readkbps",ProbePropertyType.DOUBLE,"KB/s","The number of KBytes read per second");
		this.addProbeProperty(1,"writekbps",ProbePropertyType.DOUBLE,"KB/s","The number of KBytes written per second");
		this.addProbeProperty(2,"iotime",ProbePropertyType.DOUBLE,"%","The percent of disk time spent on I/O operations");
	
		this.lastValues = this.calcValues();
		this.lastTime = System.currentTimeMillis()/1000; //in seconds
	}
	
	public DiskStatsProbe(){
		this("DiskStatsProbe",40);
	}
	
	@Override
	public String getDescription(){
		return "Probe that collects Disk IO Stats";
	}
	
	@Override
	public ProbeMetric collect(){
		HashMap<String,Long> curValues = this.calcValues();
		long curTime = System.currentTimeMillis()/1000;
		
		HashMap<String,Long> diffValues = new HashMap<String,Long>();
				
		for (Entry<String, Long> entry : lastValues.entrySet()) {
			String key = entry.getKey();
		    Long val = entry.getValue();
		    diffValues.put(key, (Long)curValues.get(key) - val);
		}
//		System.out.println("diffreads: " + diffValues.get("merge_reads")+" diffwrites: "+diffValues.get("merge_writes")+" diffiotime: "+diffValues.get("iotime"));
	    long timediff = curTime - this.lastTime;
	    double readbps;
	    double writebps;
	    double iotime;
	    
	    if (timediff > 0){
	    	readbps = (0.001 * diffValues.get("merge_reads") * BYTES_PER_SECTOR) / timediff; //we want kbps so 0.001
	    	writebps = (0.001 * diffValues.get("merge_writes") * BYTES_PER_SECTOR) / timediff;
	    	iotime = (0.1 * diffValues.get("iotime")) / timediff; //iotime is in ms and we want percentage so 100/1000=0.1
	    }
	    else{
	    	readbps = 0.0;
	    	writebps = 0.0;
	    	iotime = 0.0;
	    }	    
//		System.out.println("readbps: " + readbps +" writebps: "+writebps+" iotime: "+iotime);
		
		this.lastValues = curValues;
	    this.lastTime = curTime;
	    
	    HashMap<Integer,Object> values = new HashMap<Integer,Object>();
        values.put(0, readbps);
	    values.put(1, writebps);
	    values.put(2, iotime);
	    
		return new ProbeMetric(values);
	}
	
	private HashMap<String,Long> calcValues(){
		HashMap<String,Long> stats = new HashMap<String,Long>();
		long merge_reads = 0;
		long merge_writes = 0;
		long iotime = 0;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(PATH)));
			String line;
			while((line = br.readLine())!=null){
				if(line.contains("sda ") || line.contains("vda ")){
					String[] tokenz = line.split("\\s+");
					//[white space]8 0 sda 25485 11086 1885590 273084 10149 13330 1076312 471656 0 100500 743000
					//[white space]253 0 vda 5065 1304 195506 15835 105722 154245 2068512 812391 0 288836 828134
					//field 5 -- # of reads merged
					//field 9 -- # of writes merged
					//field 12 -- iotime in millisecs
					merge_reads += Long.parseLong(tokenz[6]);
					merge_writes += Long.parseLong(tokenz[10]);
					iotime += Long.parseLong(tokenz[13]); 
				}
			}
			br.close();
		}
		catch (FileNotFoundException e){
			this.writeToProbeLog(Level.SEVERE, "couldn't read from disk IO stats from "+PATH);
		}
		catch (IOException e){
			this.writeToProbeLog(Level.SEVERE, "couldn't read from disk IO stats from "+PATH);
		}
			
		//System.out.println("merge_reads: " + merge_reads + " merge_writes: " + merge_writes + " iotime: " + iotime);
		stats.put("merge_reads", merge_reads);
		stats.put("merge_writes", merge_writes);
		stats.put("iotime", iotime);			
		return stats;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DiskStatsProbe diskstats = new DiskStatsProbe();
		diskstats.activate();
	}
}
