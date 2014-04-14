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
package eu.celarcloud.jcatascopia.agentpack;

import java.util.logging.Level;

import eu.celarcloud.jcatascopia.agentpack.aggregators.IAggregator;
import eu.celarcloud.jcatascopia.agentpack.distributors.IDistributor;
import eu.celarcloud.jcatascopia.exceptions.CatascopiaException;

public class DistributorWorker extends Thread{
	/*
	 * INACTIVE - running but distributing messages is paused
	 * ACTIVE   - running and distributing messages
	 * DYING    - in the process of terminating
	 */
	public enum DistributorStatus{INACTIVE,ACTIVE,DYING};
	private DistributorStatus distributorStatus;
	
	private boolean firstFlag;
	private IDistributor distributor;
	private IAggregator aggregator;
	
	//aggregator settings
	private long INTERVAL;
	private int BUF_SIZE;
	
	private IJCatascopiaAgent agent;
	
	public DistributorWorker(IDistributor distributor,IAggregator aggregator,long interval,int buf_size, IJCatascopiaAgent agent){
		super("Distributor-Thread");
		this.distributor = distributor;

		this.distributorStatus = DistributorStatus.INACTIVE;
		this.firstFlag = true;
		
		this.aggregator = aggregator;
		this.INTERVAL = interval;
		this.BUF_SIZE = buf_size;
		
		this.agent = agent;
	}
	
	@Override
	public void start(){
		this.activate();
	}
	
	public synchronized void activate(){
		if (this.distributorStatus == DistributorStatus.INACTIVE){
			if (this.firstFlag){
				super.start();
				this.firstFlag = false;
			}
			else this.notify();
			this.distributorStatus = DistributorStatus.ACTIVE;	
		}	
	}
	
	public void deactivate(){
		this.distributorStatus = DistributorStatus.INACTIVE;
	}
	
	public synchronized void terminate(){
		this.distributor.terminate();
		this.distributorStatus = DistributorStatus.DYING;
		this.notify();
	}
	
	@Override
	public void run(){
		try{
			long interval = 0; long period = 2000;
			while(this.distributorStatus != DistributorStatus.DYING){
				if(this.distributorStatus == DistributorStatus.ACTIVE){
					try{
						if(this.aggregator.length() > 0){ //check if there are any new messages
							if (interval > INTERVAL || aggregator.length() > BUF_SIZE){ //time to send message to server
								this.distributor.send(aggregator.toMessage());
								interval = 0;
								this.aggregator.clear();
								if (this.agent.inDebugMode())
									System.out.println("DistributorWorker>> Message sent to JCatascopia Monitoring Server");
							}
							else interval += period;
						}
						Thread.sleep(period);
					}
					catch(CatascopiaException e){
						this.agent.writeToLog(Level.SEVERE, e);
						continue;
					}
					catch(Exception e){
						this.agent.writeToLog(Level.SEVERE, e);
						Thread.sleep(5000);
						this.aggregator.clear();
						continue;
					}
				}
				else 
					synchronized(this){
						while(this.distributorStatus == DistributorStatus.INACTIVE)
							this.wait();
					}
			}
		}
		catch (InterruptedException e){
			this.agent.writeToLog(Level.SEVERE, e);
		}
		finally{
			this.distributor.terminate();
			this.agent.writeToLog(Level.SEVERE, "unexpected distributor termination");
		}	
	}
}
