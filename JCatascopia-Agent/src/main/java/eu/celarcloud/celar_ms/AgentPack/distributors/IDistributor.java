package eu.celarcloud.celar_ms.AgentPack.distributors;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;

public interface IDistributor {
	public void send(String msg) throws CatascopiaException;
	public void terminate();
}
