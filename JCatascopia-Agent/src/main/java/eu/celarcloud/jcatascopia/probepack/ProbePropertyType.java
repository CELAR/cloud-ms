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
package eu.celarcloud.jcatascopia.probepack;

public enum ProbePropertyType {
	INTEGER, LONG, CHAR, STRING, FLOAT, DOUBLE, BOOLEAN;
	
	public static boolean isType(ProbePropertyType type,Object obj){
		boolean ans = false;
		switch(type){
			case INTEGER:
				if(obj instanceof Integer)
					ans = true;
				break;
			case LONG:
				if(obj instanceof Long)
					ans = true;
				break;
			case CHAR:
				if(obj instanceof Character)
					ans = true;
				break;
			case STRING:
				if(obj instanceof String)
					ans = true;
				break;
			case FLOAT:
				if(obj instanceof Float)
					ans = true;
				break;
			case DOUBLE:
				if(obj instanceof Double)
					ans = true;
				break;	
			default:
				break;
		}
		return ans;	
	}
}
