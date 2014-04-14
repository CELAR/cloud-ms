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
package eu.celarcloud.jcatascopia.agentpack.utils;

import java.util.Date;

/**
 * 
 * @author Demetris Trihinas
 * Utility class for expressing timestamps. 
 * Time is expressed as UNIX time. Seconds from UNIX epoch
 *
 */
public class CatascopiaTimestamp {
	
	private long timestamp;
	
	public CatascopiaTimestamp(){
		//this.timestamp = System.currentTimeMillis()/1000L;
		this.timestamp = System.currentTimeMillis();
	}
	
	public CatascopiaTimestamp(long timestamp){
		this.timestamp = timestamp;
	}
	
	public long getTimestamp(){
		return this.timestamp;
	}
	
	public void setTimestamp(long t){
		this.timestamp = t;
	}
	
	public String getReadableTimestamp(){
		//Date d = new Date(this.timestamp*1000L);
		Date d = new Date(this.timestamp);
		return d.toString();
	}
	
	//for testing
	public static void main(String[] args){
		CatascopiaTimestamp t = new CatascopiaTimestamp();
		System.out.println(t.getTimestamp());
		System.out.println(t.getReadableTimestamp());
	}
}
