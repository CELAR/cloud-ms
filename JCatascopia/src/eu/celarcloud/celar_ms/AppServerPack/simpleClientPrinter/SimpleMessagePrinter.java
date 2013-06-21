package eu.celarcloud.celar_ms.AppServerPack.simpleClientPrinter;

import eu.celarcloud.celar_ms.AppServerPack.Listener;
import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;

public class SimpleMessagePrinter extends Listener{

	public SimpleMessagePrinter() throws CatascopiaException {
		super();
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
		SimpleMessagePrinter printer = new SimpleMessagePrinter();
		printer.activate();
	}
}
