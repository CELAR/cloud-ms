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
package eu.celarcloud.jcatascopia.agentpack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;

import eu.celarcloud.jcatascopia.agentpack.sockets.Dealer;
import eu.celarcloud.jcatascopia.probepack.IProbe;
import eu.celarcloud.jcatascopia.probepack.ProbeProperty;


public class ServerConnector{
	
	public static boolean connect(String ip, String port, String agentID,String agentIP,HashMap<String,IProbe> probelist){
    	String pingmsg = "{\"agentID\":\""+agentID+"\",\"agentIP\":\""+agentIP+"\"}";
		
    	boolean success = false;
		String response = sendRequest(ip,port,"AGENT.CONNECT",pingmsg);
		if (response.contains("OK")){
			String metricList = compileMetricList(agentID,agentIP,probelist);
			response = sendRequest(ip,port,"AGENT.METRICS",metricList);
			if (response.contains("OK"))
				success = true;
		}
		else if(response.contains("CONNECTED"))
			success = true;
		else
			success = false;
				
		return success;	
	}
	
	private static String compileMetricList(String agentID, String agentIP, HashMap<String,IProbe> probelist){
		StringBuilder sb = new StringBuilder();
		sb.append("{\"agentID\":\""+agentID+"\",\"agentIP\":\""+agentIP+"\",\"probes\":[");
		for (Entry<String, IProbe> entry : probelist.entrySet()){
			String probeName = entry.getKey();
			IProbe probe = entry.getValue();
			if(probe.getProbeStatus() == IProbe.ProbeStatus.ACTIVE || probe.metricsPullable()){
				sb.append("{\"probeName\":\""+probeName+"\",\"metrics\":[");
				ArrayList<ProbeProperty> propertylist = probe.getProbePropertiesAsList();
				for(int i=0; i<propertylist.size();i++)
					sb.append("{\"name\":\""+propertylist.get(i).getPropertyName()+"\""+
							   ",\"type\":\""+propertylist.get(i).getPropertyType()+"\""+
							   ",\"units\":\""+propertylist.get(i).getPropertyUnits()+"\"},");
				sb.replace(sb.length()-1, sb.length(), "");
				sb.append("]},");
			}
		}
		sb.replace(sb.length()-1, sb.length(), "");
		sb.append("]}");
    	
		return sb.toString();
	}
	
	/*
	 * Deprecated
	 */
	public static boolean reportAvailableMetrics(String ip, String port, String protocol,String agentID,String agentIP, HashMap<String,IProbe> probelist){
		StringBuilder sb = new StringBuilder();
		sb.append("{\"agentID\":\""+agentID+"\",\"agentIP\":\""+agentIP+"\",\"probes\":[");
		for (Entry<String, IProbe> entry : probelist.entrySet()){
			String probeName = entry.getKey();
			IProbe probe = entry.getValue();
			if(probe.getProbeStatus() == IProbe.ProbeStatus.ACTIVE || probe.metricsPullable()){
				sb.append("{\"probeName\":\""+probeName+"\",\"metrics\":[");
				ArrayList<ProbeProperty> propertylist = probe.getProbePropertiesAsList();
				for(int i=0; i<propertylist.size();i++)
					sb.append("{\"name\":\""+propertylist.get(i).getPropertyName()+"\""+
							   ",\"type\":\""+propertylist.get(i).getPropertyType()+"\""+
							   ",\"units\":\""+propertylist.get(i).getPropertyUnits()+"\"},");
				sb.replace(sb.length()-1, sb.length(), "");
				sb.append("]},");
			}
		}
		sb.replace(sb.length()-1, sb.length(), "");
		sb.append("]}");
		String json_request = sb.toString();
		
		String response = sendRequest(ip,port,"AGENT.METRICS",json_request);
		boolean success = (response.contains("OK")) ? true : false;
		return success;
	}
	
	private static String sendRequest(String ip, String port, String header, String json_request){
		Dealer dealer = new Dealer(ip, port, UUID.randomUUID().toString().replace("-", ""));
		int attempts = 0; 
		boolean connected = false;
    	String[] response = null;
    	String resp = "";
    	try {			
			while(((attempts++)<3) && (!connected)){
	    		dealer.send("",header,json_request);
	            response = dealer.receive(12000L);
	            if (response != null){
	            	connected = true;
	            	resp = response[1];
	            	break;
	            }
				else	
					Thread.sleep(3000);
	    	}
		} 
    	catch(InterruptedException e){
    		e.printStackTrace();	
    	}  
    	catch(Exception e){	
    		e.printStackTrace();
    	}
    	finally{
    		dealer.close();
    	}
    	
    	return resp;
	}
}
