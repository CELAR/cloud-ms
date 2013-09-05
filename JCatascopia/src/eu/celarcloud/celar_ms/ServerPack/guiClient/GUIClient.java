package eu.celarcloud.celar_ms.ServerPack.guiClient;

import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ServerPack.Listener;

/**
 * 
 * @author Demetris Trihinas
 * 
 * GUI Client for receiving and presenting monitoring events from JCatascopia MS.
 * Events are in JSON. Using JSON-simple to process JSON.
 *
 */
public class GUIClient extends Listener{

	private DemoGUI gui;
	
	public GUIClient(String ip,String port,String protocol,long hwm) throws CatascopiaException{		
		super(ip,port,protocol,hwm,1000);
		this.gui = new DemoGUI();
		this.gui.init();
	}

	@Override
	public void listen(String[] msg){
		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(msg[0]);
			JSONArray metrics;
			JSONObject obj;
			String group = (String) json.get("group");
			
			//Memory
			if (group.equals("MemoryProbe")){
				metrics = (JSONArray) json.get("metrics");
				Iterator iter = metrics.iterator();
				while(iter.hasNext()){
					obj = (JSONObject) iter.next();
					if(obj.get("name").equals("memTotal")){
						this.gui.setTotalMemLabelText(obj.get("val").toString()+"    "+obj.get("units").toString());
					}
					else if(obj.get("name").equals("memUsed")){
						this.gui.setUsedMemLabelText(obj.get("val").toString()+"    "+obj.get("units").toString());
					}
					else if(obj.get("name").equals("memFree")){
						this.gui.setFreeMemLabelText(obj.get("val").toString()+"    "+obj.get("units").toString());
					}
					else if(obj.get("name").equals("memCache")){
						this.gui.setCacheMemLabelText(obj.get("val").toString()+"    "+obj.get("units").toString());
					}
					else if(obj.get("name").equals("memSwapTotal")){
						this.gui.setTotalSwapMemLabelText(obj.get("val").toString()+"    "+obj.get("units").toString());
					}
					else if(obj.get("name").equals("memSwapFree")){
						this.gui.setFreeSwapMemLabelText(obj.get("val").toString()+"    "+obj.get("units").toString());
					}
				}
			}
			
			//CPU
			else if (group.equals("CPUProbe")){
				metrics = (JSONArray) json.get("metrics");
				Iterator iter = metrics.iterator();
				while(iter.hasNext()){
					obj = (JSONObject) iter.next();
					String cpu = obj.get("val").toString();
					if (cpu.length()>5) cpu = cpu.substring(0, 5);
					if(obj.get("name").equals("cpuTotalUsage")){
						this.gui.setCPUTotalUsageLabelText(cpu+"    "+obj.get("units").toString());
					}
					else if(obj.get("name").equals("cpuUserUsage")){
						this.gui.setCPUUserUsageLabelText(cpu+"    "+obj.get("units").toString());
					}
					else if(obj.get("name").equals("cpuSystemUsage")){
						this.gui.setCPUSystemUsageLabelText(cpu+"    "+obj.get("units").toString());
					}
					else if(obj.get("name").equals("cpuIdleUsage")){
						this.gui.setCPUIdleUsageLabelText(cpu+"    "+obj.get("units").toString());
					}
					else if(obj.get("name").equals("cpuIOwaitUsage")){
						this.gui.setCPUIOwaitUsageLabelText(cpu+"    "+obj.get("units").toString());
					}
				}
			}
			
			//Network
			else if (group.equals("NetworkProbe")){
				metrics = (JSONArray) json.get("metrics");
				Iterator iter = metrics.iterator();
				while(iter.hasNext()){
					obj = (JSONObject) iter.next();
					String net = obj.get("val").toString();
					if (net.length()>5) net = net.substring(0, 8);
					if(obj.get("name").equals("netBytesIN")){
						this.gui.setBytesINLabelText(net+"    "+obj.get("units").toString());
					}
					else if(obj.get("name").equals("netPacketsIN")){
						this.gui.setPacketsINLabelText(net+"    "+obj.get("units").toString());
					}
					else if(obj.get("name").equals("netBytesOUT")){
						this.gui.setBytesOUTLabelText(net+"    "+obj.get("units").toString());
					}
					else if(obj.get("name").equals("netPacketsOut")){
						this.gui.setPacketsOUTLabelText(net+"    "+obj.get("units").toString());
					}
				}
			}
			
			//Disk
			else if (group.equals("DiskProbe")){
				metrics = (JSONArray) json.get("metrics");
				Iterator iter = metrics.iterator();
				while(iter.hasNext()){
					obj = (JSONObject) iter.next();
					String disk = obj.get("val").toString();
					if(obj.get("name").equals("diskTotal")){
						this.gui.setDiskTotalLabelText(disk+"    "+obj.get("units").toString());
					}
					else if(obj.get("name").equals("diskFree")){
						this.gui.setDiskFreeLabelText(disk+"    "+obj.get("units").toString());
					}
					else if(obj.get("name").equals("diskUsed")){
						this.gui.setDiskUsedLabelText(disk.substring(0, 5)+"    "+obj.get("units").toString());
					}
				}
			}
			
			//Disk IO
			else if (group.equals("DiskStatsProbe")){
				metrics = (JSONArray) json.get("metrics");
				Iterator iter = metrics.iterator();
				while(iter.hasNext()){
					obj = (JSONObject) iter.next();
					String diskIO = obj.get("val").toString();
					if(obj.get("name").equals("readkbps")){
						this.gui.setReadkbpsLabelText(diskIO+"    "+obj.get("units").toString());
					}
					else if(obj.get("name").equals("writekbps")){
						this.gui.setWritekbpsLabelText(diskIO+"    "+obj.get("units").toString());
					}
					else if(obj.get("name").equals("iotime")){
						if (diskIO.length()>5) diskIO = diskIO.substring(0, 5);
						this.gui.setIOtimeText(diskIO+"    "+obj.get("units").toString());
					}
				}
			}
			
		} 
		catch(ParseException e){
			e.printStackTrace();
		}		
	}
	
	public static void main(String[] args) throws CatascopiaException {
		GUIClient client = new GUIClient("localhost","4242","tcp",32);
		client.activate();
	}
}
