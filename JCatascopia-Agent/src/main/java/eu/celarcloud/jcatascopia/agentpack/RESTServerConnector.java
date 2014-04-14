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
