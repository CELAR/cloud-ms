package eu.celarcloud.jcatascopia.agentpack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.celarcloud.jcatascopia.probepack.IProbe;
import eu.celarcloud.jcatascopia.probepack.ProbeProperty;

public class RESTServerConnector {		
	
	public static boolean connect(String url,String agentID,String agentIP,HashMap<String,IProbe> probelist){
    	String pingmsg = "{\"agentID\":\""+agentID+"\",\"agentIP\":\""+agentIP+"\"}";
		
    	boolean success = false;
		String response = sendRequest(url,pingmsg);
		System.out.println(response);
		if (response.contains("OK")){
			String metricList = compileMetricList(agentID,agentIP,probelist);
			response = sendRequest(url,metricList);
			System.out.println(response);
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
	
	private static String sendRequest(String url, String json_request){
		Client client = Client.create();
		WebResource webResource = client.resource(url);
		ClientResponse response = webResource.type("application/json").put(ClientResponse.class, json_request);
		String msg = response.getEntity(String.class);
		client.destroy();
    	return msg;
	}
}