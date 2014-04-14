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
package eu.celarcloud.celar_ms.ServerPack;

import java.util.Map.Entry;

import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;

public class Aggregator{

	private StringBuffer aggregator;
	private MonitoringServer server;
	
	public Aggregator(MonitoringServer server){
		this.aggregator = new StringBuffer();
		this.server = server;
	}

	public String createMessage(){
		this.aggregator.append("{\"serverID\":\""+server.getServerID()+"\",\"serverIP\":\""+server.getServerIP()+"\",\"agents\":[");
		AgentObj a;
		for (Entry<String,AgentObj> entry : server.agentMap.entrySet()){
			a = entry.getValue();
			this.aggregator.append("{");
			this.aggregator.append("\"agentID\":\""+a.getAgentID()+"\",");
			this.aggregator.append("\"agentIP\":\""+a.getAgentIP()+"\",");
			
			//add metrics
			this.aggregator.append("\"metrics\":[");
			MetricObj m = null;
			for (Entry<String,MetricObj> entry2 : server.metricMap.entrySet()){
				m = entry2.getValue();
				this.aggregator.append("{");
				this.aggregator.append("\"timestamp\":\""+m.getTimestamp()+"\",");
				this.aggregator.append("\"group\":\""+m.getGroup()+"\",");
				this.aggregator.append("\"name\":\""+m.getName()+"\",");
				this.aggregator.append("\"units\":\""+m.getUnits()+"\",");
				String t = m.getType();
				this.aggregator.append("\"type\":\""+t+"\",");
				if (t.equals("DOUBLE") || t.equals("INTEGER") || t.equals("LONG") || t.equals("FLOAT"))
					this.aggregator.append("\"val\":\""+m.getAvg()+"\"},");
				else
					this.aggregator.append("\"val\":\""+m.getValue()+"\"},");
			}
			if (m != null)
				this.aggregator.replace(this.aggregator.length()-1, this.aggregator.length(), "");
			this.aggregator.append("]},");
		}
		if (!server.agentMap.isEmpty())
			this.aggregator.replace(this.aggregator.length()-1, this.aggregator.length(), "");
		this.aggregator.append("]}");
		
		if (this.server.inDebugMode())
			System.out.println("Aggregator>> Message Ready for Distribution...\n"+this.aggregator.toString());
		return this.aggregator.toString();
	}
	
	public void clear(){
		this.aggregator.setLength(0);
	}
	
	public int length(){
		return this.aggregator.length();
	}
}
