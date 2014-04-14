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
package JCatascopia.XProbePack;

import java.util.UUID;

public class XProbe{
	private static final String AGENT_IP = "localhost";
	private static final String AGENT_PORT = "4243";
	public static final String USAGESTATUS = "java -jar XProbe --name:<name> --units:<units> " +
			                                 "--type:<type> --value:<value> [--group:<group>]";

	public enum Type {INTEGER, LONG, CHAR, STRING, FLOAT, DOUBLE, BOOLEAN};

	private String name;
	private String units;
	private Type type;
	private Object value;
	private String group;
	private Dealer dealer;
		
	public XProbe(String name, String units, String type, String value, String group) throws CatascopiaException{
		try{
			this.name =name;
			this.units = units;
			this.value = value;
			this.group = group;
			this.type = Type.valueOf(type);
			
			this.initDealer();
			this.distribute();
		}
		catch(IllegalArgumentException e){
			throw new CatascopiaException(type+" is NOT a valid JCatascopia type",CatascopiaException.ExceptionType.ATTRIBUTE);
		}
		catch(Exception e){
			throw new CatascopiaException("Error occured while distributing metric",CatascopiaException.ExceptionType.CONNECTION);
		}
	}
	
	private void initDealer(){
		this.dealer = new Dealer(AGENT_IP,AGENT_PORT,"tcp",5,UUID.randomUUID().toString().replace("-", ""));
	}
	
	private String toJSON(){
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"timestamp\":\""+System.currentTimeMillis()+"\",");
		sb.append("\"group\":\""+this.group+"\",");
		sb.append("\"metrics\":[");
		sb.append("{\"name\":\""+this.name+"\",");
		sb.append("\"units\":\""+this.units+"\",");
		sb.append("\"type\":\""+this.type.name()+"\",");
		sb.append("\"val\":\""+this.value+"\"},");

		sb.replace(sb.length()-1, sb.length(), "");
		sb.append("]}");
		return sb.toString();
	}
	
	public String toString(){
		String s = "metric>> name: "+this.name+" units: "+this.units+" type: "+this.type+" value: "+this.value;
		if (!group.equals(""))
			s += " group: "+this.group;
		else s += " group: none";
		return s;
	}
	
	private boolean distribute() throws CatascopiaException{
		int attempts = 0; 
		boolean connected = false;
    	String[] response = null;
    	try {			
			while(((attempts++)<3) && (!connected)){
	    		dealer.send("","XPROBE.METRIC",this.toJSON());
	    		System.err.println("XProbe>> sending metric, awaiting for responce...");
	            response = dealer.receive(12000L);
	            if (response != null){
	            	if (response[1].contains("OK")){ 
	            		connected = true;
	    	    		System.err.println("XProbe>> JCatascopia Monitoring Agent received metric SUCCESSFULLY");
	            	} 
	            	else{ 
	            		connected = false;
	    	    		System.err.println("XProbe>> metric sending FAILED");
	            	}
	            	
	            	break;
	            }
				else	
					Thread.sleep(3000);
	    	}
			dealer.close();
		} 
    	catch (InterruptedException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    	if (connected) 
    		return true; 
    	else 
    		throw new CatascopiaException("Could NOT connect to Agent ",CatascopiaException.ExceptionType.CONNECTION);
	}
	
	/**
	 * @param args
	 * @throws CatascopiaException 
	 */
	public static void main(String[] args) throws CatascopiaException{
		String name = "",units = "",type = "",value = "",group = "";
		if (args.length == 4  || args.length == 5){
			int i=0;
			while(i<args.length){
				String[] p = args[i].split(":");
				if(p[0].equals("--name"))
					name = p[1];
				else if(p[0].equals("--units"))
					units = p[1];
				else if(p[0].equals("--type"))
					type = p[1];
				else if(p[0].equals("--value"))
					value = p[1];
				else if(p[0].equals("--group"))
					group = p[1];
				else
					throw new CatascopiaException(XProbe.USAGESTATUS,CatascopiaException.ExceptionType.ARGUMENT);
				i++;
			}
//			System.out.println(name+" "+units+" "+type+" "+value+" ");
		}
		else
			throw new CatascopiaException(XProbe.USAGESTATUS,CatascopiaException.ExceptionType.ARGUMENT);
		
		XProbe xprobe = new XProbe(name,units,type,value,group);
	}
}
