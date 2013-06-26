package eu.celarcloud.celar_ms.ServerPack;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;

public class SimpleListener extends Listener{
	
	public SimpleListener(String ip,String port,String protocol,long hwm) throws CatascopiaException{
		super(ip,port,protocol,hwm);
	}
	
	@Override
	public void processMessage(String msg) {
		System.out.println(msg);
	}
}
