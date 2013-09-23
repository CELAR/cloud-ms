package eu.celarcloud.celar_ms.ServerPack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;

import eu.celarcloud.celar_ms.SocketPack.Dealer;



public class InitialServerConnector{
	
	public static boolean connect(String ip, String port, String protocol,String agentID,String agentIP){
		Dealer dealer = new Dealer(ip,port,protocol,16,UUID.randomUUID().toString().replace("-", ""));

		int attempts = 0; 
		boolean connected = false;
    	String json_request;
    	String[] response = null;
    	try {
			json_request = "{\"agentID\":\""+agentID+"\",\"agentIP\":\""+agentIP+"\"}";
			
			while(((attempts++)<3) && (!connected)){
	    		dealer.send("","AGENT.CONNECT",json_request);
	            response = dealer.receive(12000L);
	            if (response != null){
	            	connected = (response[1].contains("OK")) ? true : false;
	            	break;
	            }
				else	
					Thread.sleep(3000);
	    	}
			dealer.close();
		} 
    	catch (InterruptedException e){
			;
		}  
    	return connected;
	}
	
	public static boolean reportAvailableMetrics(String ip, String port, String protocol,String agentID,String agentIP){
		StringBuilder sb = new StringBuilder();
		sb.append("{\"agentID\":\""+agentID+"\",\"agentIP\":\""+agentIP+"\",\"probes\":[");
		sb.append("]}");
    	
		String json_request = sb.toString();
//		System.out.println(json_request);
		
		Dealer dealer = new Dealer(ip,port,protocol,16,UUID.randomUUID().toString().replace("-", ""));
		int attempts = 0; 
		boolean connected = false;
    	String[] response = null;
    	try {			
			while(((attempts++)<3) && (!connected)){
	    		dealer.send("","AGENT.METRICS",json_request);
	            response = dealer.receive(12000L);
	            if (response != null){
	            	connected = (response[1].contains("OK")) ? true : false;
	            	break;
	            }
				else	
					Thread.sleep(3000);
	    	}
			dealer.close();
		} 
    	catch (InterruptedException e){
    		;
		}  
    	return connected;
	}
}