package eu.celarcloud.celar_ms.ProbePack.ProbeLibrary;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.Level;

import eu.celarcloud.celar_ms.ProbePack.Probe;
import eu.celarcloud.celar_ms.ProbePack.ProbeMetric;
import eu.celarcloud.celar_ms.ProbePack.ProbePropertyType;

/**
 * 
 * @author Demetris Trihinas
 *
 */
public class DiskProbe  extends Probe{
	
	private Runtime runtime;
	private Process p;
	
	public DiskProbe(String name, int freq){
		super(name,freq);
		
		this.addProbeProperty(0,"diskTotal",ProbePropertyType.LONG,"MB","disk total capacity in MB");
		this.addProbeProperty(1,"diskFree",ProbePropertyType.LONG,"MB","available disk space in MB");
		this.addProbeProperty(2,"diskUsed",ProbePropertyType.DOUBLE,"%","disk space used (%)");
		 
		this.runtime = Runtime.getRuntime();
	}
	
	public DiskProbe(){
		this("DiskProbe",60);
	}
	
	@Override
	public String getDescription() {
		return "DiskProbe collect's Disk usage stats.";
	}
	
	@Override
	public ProbeMetric collect(){
		long diskTotal = 0;
		long diskFree = 0;
		double diskUsed = 0.0; 
		HashMap<Integer,Object> values = new HashMap<Integer,Object>();

		try {
			p = runtime.exec("df -k");
			p.waitFor();
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = b.readLine(); //first line is just header, dont want it
			String[]  tokenz;
			while ((line = b.readLine()) != null){
				tokenz = line.split("\\s+");
				//dont want "none" filesystems and remote partitions. remote partitions contain ':'
				if(!tokenz[0].startsWith("/") || tokenz[0].contains(":"))
					continue;
							
				diskTotal += Long.parseLong(tokenz[1])/1024; //in MB
				diskFree  += Long.parseLong(tokenz[3])/1024;  //in MB
				diskUsed  += (diskTotal - diskFree)/(diskTotal * 1.0) * 100;
				
//				System.out.println("diskTotal(MB): "+ diskTotal + " diskFree(MB): "+ diskFree + " diskUsed(%): " + diskUsed);
			}
			b.close();
			values.put(0, diskTotal);
			values.put(1, diskFree);
			values.put(2, diskUsed);
			return new ProbeMetric(values);
		}
		catch(Exception e){
			this.writeToProbeLog(Level.SEVERE, "Couldn't read disk stats");
		}
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		DiskProbe diskprobe = new DiskProbe();
		diskprobe.activate();
	}
}
