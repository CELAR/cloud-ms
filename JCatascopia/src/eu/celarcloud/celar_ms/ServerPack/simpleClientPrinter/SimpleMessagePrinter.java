package eu.celarcloud.celar_ms.ServerPack.simpleClientPrinter;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ServerPack.Listener;

public class SimpleMessagePrinter extends Listener{

	public SimpleMessagePrinter(String ip,String port,String protocol,long hwm) throws CatascopiaException {
		super(ip,port,protocol,hwm);
	}

	@Override
	public void processMessage(String msg) {
		System.out.println(msg);	
	}
	
	/**
	 * @param args
	 * @throws CatascopiaException 
	 */
	public static void main(String[] args) throws CatascopiaException{
		SimpleMessagePrinter printer = new SimpleMessagePrinter("localhost","4242","tcp",32);
		printer.activate();
	}
}
