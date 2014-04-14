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

import eu.celarcloud.jcatascopia.agentpack.IJCatascopiaAgent;

public class StringAggregator implements IAggregator{

	private StringBuffer aggregator;
	private String agentID;
	private String agentIP;
	private IJCatascopiaAgent agent;
	
	public StringAggregator(String agentID, String agentIP, IJCatascopiaAgent agent){
		this.aggregator = new StringBuffer();
		this.agentID = agentID;
		this.agentIP = agentIP;
		this.agent = agent;
	}

	public void add(String metric){
		if(this.aggregator.length()>0)
			this.aggregator.append(","+metric);
		else
			this.aggregator.append("{\"events\":["+metric);
	}
	
	public String toMessage(){
		if(this.aggregator.length()==0)
			this.add("");
		this.aggregator.append("],\"agentID\":\""+this.agentID+"\"");
		this.aggregator.append(",\"agentIP\":\""+this.agentIP+"\"}");
		
		if (this.agent.inDebugMode())
			System.out.println("StringAggregator>> Message Ready for Distribution:\n"+this.aggregator.toString());
		return this.aggregator.toString();
	}
	
	public void clear(){
		this.aggregator.setLength(0);
	}
	
	public int length(){
		return this.aggregator.length();
	}
}
