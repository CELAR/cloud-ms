package eu.celarcloud.celar_ms.ServerPack;

import java.util.UUID;
import eu.celarcloud.celar_ms.SocketPack.Dealer;

public class InitialServerConnector{
	
	public static boolean connect(String ip, String port, String protocol,String serverID,String serverIP){
		Dealer dealer = new Dealer(ip,port,protocol,16,UUID.randomUUID().toString().replace("-", ""));

		int attempts = 0; 
		boolean connected = false;
    	String json_request;
    	String[] response = null;
    	try {
			json_request = "{\"serverID\":\""+serverID+"\",\"serverIP\":\""+serverIP+"\"}";
			
			while(((attempts++)<3) && (!connected)){
	    		dealer.send("","SERVER.CONNECT",json_request);
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