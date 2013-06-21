package eu.celarcloud.celar_ms.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;

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
	public static String getMyIP() throws CatascopiaException{
		String myIP = "";
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while(interfaces.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) interfaces.nextElement();
		        //System.out.println("Net interface: "+ni.getName());
		        if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) 
		        	continue;

		        Enumeration<InetAddress> e2 = ni.getInetAddresses();

		        while (e2.hasMoreElements()){
		        	InetAddress ip = e2.nextElement();
		            myIP = ip.getHostAddress();
		            //System.out.println("ip address: "+myIP); 
		        }
			}
		    if (myIP.equals(""))
		    	myIP = InetAddress.getLocalHost().getHostAddress();
		}
		catch (SocketException e){
			throw new CatascopiaException("SocketException thrown", CatascopiaException.ExceptionType.NETWORKING);
		}
		catch (UnknownHostException e) {
			throw new CatascopiaException("UnknownHostException thrown", CatascopiaException.ExceptionType.NETWORKING);
		}
        return myIP;
	}
}
