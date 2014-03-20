package eu.celarcloud.jcatascopia.agentpack.distributors;

import eu.celarcloud.jcatascopia.exceptions.CatascopiaException;

public interface IDistributor {
	public void send(String msg) throws CatascopiaException;
	public void terminate();
}
