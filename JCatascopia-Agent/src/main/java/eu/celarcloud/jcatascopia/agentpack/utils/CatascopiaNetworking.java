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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

import eu.celarcloud.jcatascopia.exceptions.CatascopiaException;

/**
 * CatascopiaNetworking is a library functions related to Networking
 * 
 * @author Demetris Trihinas
 *
 */
public class CatascopiaNetworking{
	/**
	 * 
	 * @return IP address of device MS Agent resides on.
	 * @throws CatascopiaException
	 */
	public static final String getMyIP() throws CatascopiaException{
		String myIP = "";
		ArrayList<String> publicIPs = new ArrayList<String>();
		ArrayList<String> privateIPs = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while(interfaces.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) interfaces.nextElement();
//		        System.out.println("Net interface: "+ni.getName());
		        if (!ni.isUp() || ni.isVirtual() /*|| ni.isLoopback()*/) 
		        	continue;
		        Enumeration<InetAddress> e2 = ni.getInetAddresses();

		        while (e2.hasMoreElements()){
		        	InetAddress ip = e2.nextElement();
		        	myIP = ip.getHostAddress();
//		            System.out.println("ip address: "+myIP); 
		        	if (CatascopiaNetworking.validateIPv4Address(myIP) && 
		        			!myIP.startsWith("127.") && !myIP.startsWith("224.") && !myIP.startsWith("255."))
		        		if (myIP.startsWith("192."))
			        		privateIPs.add(myIP);
		        		else 
		        			publicIPs.add(myIP);
		        }
			} 
		    if (publicIPs.size()>0)
	        	return publicIPs.get(0);
	        else if (privateIPs.size()>0)
	        	return privateIPs.get(0);
	        else
	        	return InetAddress.getLocalHost().getHostAddress();	
		}
		catch (SocketException e){
			throw new CatascopiaException("SocketException thrown", CatascopiaException.ExceptionType.NETWORKING);
		}
		catch (UnknownHostException e) {
			throw new CatascopiaException("UnknownHostException thrown", CatascopiaException.ExceptionType.NETWORKING);
		}
	}
	
	public final static boolean validateIPv4Address(String ipAddress){		
		String[] parts = ipAddress.split("\\.");
	    
	    if (parts.length != 4)
	        return false;

	    for (String s : parts){
	        int i = Integer.parseInt(s);
	        if ((i < 0) || (i > 255))
	            return false;
	    }

	    return true;
	}
}
