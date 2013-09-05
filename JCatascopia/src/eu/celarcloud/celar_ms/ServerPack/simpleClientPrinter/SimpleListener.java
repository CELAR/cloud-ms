package eu.celarcloud.celar_ms.ServerPack.simpleClientPrinter;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ServerPack.Listener;

public class SimpleListener extends Listener{
	
	public SimpleListener(String ip,String port,String protocol,long hwm) throws CatascopiaException{
		super(ip,port,protocol,hwm,1000);
	}
	
	@Override
	public void listen(String[] msg) {
		System.out.println(msg[0]);
	}
}
