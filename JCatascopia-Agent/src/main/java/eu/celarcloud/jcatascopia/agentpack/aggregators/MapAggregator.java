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
package eu.celarcloud.jcatascopia.agentpack.aggregators;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.celarcloud.jcatascopia.agentpack.IJCatascopiaAgent;

public class MapAggregator implements IAggregator{
	private HashMap<String,String> aggregator;
	private String agentID;
	private String agentIP;
	private IJCatascopiaAgent agent;
	
	//private static final Pattern pattern = Pattern.compile("\"group\":\"[a-zA-Z0-9]*\"");
	private static final Pattern pattern = Pattern.compile("group\":\"[^\\\"]+");
	private int msg_length;
	
	public MapAggregator(String agentID, String agentIP, IJCatascopiaAgent agent){
		this.aggregator = new HashMap<String,String>();
		this.agentID = agentID;
		this.agentIP = agentIP;
		this.agent = agent;
		this.msg_length = 0;
	}

	public void add(String metric){
		Matcher m = pattern.matcher(metric);
		if (m.find()){
			//String probeName = m.group().split(":\"")[1].replace("\"","");
			String probeName = m.group().split("\":\"")[1];
			this.aggregator.put(probeName, metric);
			this.msg_length += metric.length(); 
		}
	}
	
	public String toMessage(){
		StringBuilder message = new StringBuilder();
		message.append("{\"events\":[");
		boolean first = true;
		for(Entry<String,String> entry:aggregator.entrySet()){
			if (!first)
				message.append(",");
			first = false;
			message.append(entry.getValue());
		}
		message.append("],\"agentID\":\""+this.agentID+"\"");
		message.append(",\"agentIP\":\""+this.agentIP+"\"}");
		
		if (this.agent.inDebugMode())
			System.out.println("MapAggregator>> Message Ready for Distribution:\n"+message);
		
		return message.toString();
	}
	
	public void clear(){
		this.aggregator.clear();
		this.msg_length = 0;
	}
	
	public int length(){
		return this.msg_length;
	}
}
