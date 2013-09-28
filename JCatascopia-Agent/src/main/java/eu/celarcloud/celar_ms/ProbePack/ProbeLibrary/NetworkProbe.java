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
public class NetworkProbe extends Probe{
	
	private static final String path = "/proc/net/dev";
	private HashMap<String,Long> lastValues;
	private long lasttime;
	
	public NetworkProbe(String name, int freq){
		super(name,freq);
		this.addProbeProperty(0,"netBytesIN",ProbePropertyType.DOUBLE,"bytes/s","Bytes IN per Second");
		this.addProbeProperty(1,"netPacketsIN",ProbePropertyType.DOUBLE,"packets/s","Packets OUT per Second");
		this.addProbeProperty(2,"netBytesOUT",ProbePropertyType.DOUBLE,"bytes/s","Bytes IN per Second");
		this.addProbeProperty(3,"netPacketsOut",ProbePropertyType.DOUBLE,"packets/s","Packets OUT per Second");
	    
		this.lastValues = this.calcValues();
		this.lasttime = System.currentTimeMillis()/1000;
	}
	
	public NetworkProbe(){
		this("NetworkProbe",35);
	}
	
	@Override
	public String getDescription() {
		return "NetworkProbe collect's Network usage stats.";
	}

	@Override
	public ProbeMetric collect() {
		HashMap<String,Long> curValues = this.calcValues();
		long curtime = System.currentTimeMillis()/1000;
		HashMap<String,Long> diffValues = new HashMap<String,Long>();
		
		 for (Entry<String,Long> entry : lastValues.entrySet()) {
		        String key = entry.getKey();
		        Long val = entry.getValue();
		        diffValues.put(key, (Long)curValues.get(key) - val);
		 }
		
	    long timediff = curtime - this.lasttime;
	   
	    double netBytesIN; 
	    double netPacketsIN;
	    double netBytesOUT;
	    double netPacketsOUT;
        
	    if(timediff > 0){
	    	netBytesIN = (1.0 * diffValues.get("bytesIN")) / timediff;
	    	netPacketsIN = (1.0 * diffValues.get("packetsIN")) / timediff;
	    	netBytesOUT = (1.0 * diffValues.get("bytesOUT")) / timediff;
	    	netPacketsOUT = (1.0 * diffValues.get("packetsOUT")) / timediff;
	    }
	    else{
	    	netBytesIN = 0.0;
	    	netPacketsIN = 0.0;
	    	netBytesOUT = 0.0;
	    	netPacketsOUT = 0.0;
	    }
//	    System.out.println("netBytesIN: " + netBytesIN);
//	    System.out.println("netPacketsIN: " + netPacketsIN);
//	    System.out.println("netBytesOUT: " + netBytesOUT);
//	    System.out.println("netPacketsOUT: " + netPacketsOUT);
//	    System.out.println();
	    
	    HashMap<Integer,Object> values = new HashMap<Integer,Object>();
        values.put(0, netBytesIN);
	    values.put(1, netPacketsIN);
	    values.put(2, netBytesOUT);
	    values.put(3, netPacketsOUT);
	    
	    this.lastValues = curValues;
	    this.lasttime = curtime;
		
		return new ProbeMetric(values);
	}
	
	private HashMap<String,Long> calcValues(){
		HashMap<String,Long> stats = new HashMap<String,Long>();
		long bytesIN = 0;
        long packetsIN = 0;
        long bytesOUT = 0;
        long packetsOUT = 0;
        
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(path)));
			String line;
			int c = 0;
			while((line = br.readLine())!=null){
				if (c<2){
                    c++;
                    continue;
				}
				//grab the interface name
				String[] tokenz = line.split(":");
                //don't want in calculation localhost or bond
                if (tokenz[0].endsWith(" lo") || tokenz[0].endsWith(" bond"))
                    continue;
                //now for grabbing data
                tokenz = tokenz[1].split("\\W+");
                bytesIN += Long.parseLong(tokenz[1]);
                packetsIN += Long.parseLong(tokenz[2]);
                bytesOUT += Long.parseLong(tokenz[9]);
                packetsOUT += Long.parseLong(tokenz[10]);		
			}
			br.close();
			stats.put("bytesIN", bytesIN);
            stats.put("packetsIN", packetsIN);
            stats.put("bytesOUT", bytesOUT);
            stats.put("packetsOUT", packetsOUT);  
		}
		catch (FileNotFoundException e){
			this.writeToProbeLog(Level.SEVERE, e);
		}
		catch (IOException e) {
			this.writeToProbeLog(Level.SEVERE, e);
		}		
		return stats;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NetworkProbe netprobe = new NetworkProbe();
		netprobe.activate();
	}
}
