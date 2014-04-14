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
package eu.celarcloud.celar_ms.ServerPack;

import java.util.logging.Level;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.SocketPack.Publisher;
import eu.celarcloud.celar_ms.SocketPack.ISocket;;

public class Distributor extends Thread{
	/*
	 * INACTIVE - running but distributing messages is paused
	 * ACTIVE   - running and distributing messages
	 * DYING    - in the process of terminating
	 */
	public enum DistributorStatus{INACTIVE,ACTIVE,DYING};
	private DistributorStatus distributorStatus;
	
	private boolean firstFlag;
	private Publisher publisher;
	private Aggregator aggregator;
	//aggregator settings
	private long INTERVAL;
	private IJCatascopiaServer server;
	
	public Distributor(String ip, String port, String protocol, long hwm,
			            Aggregator aggregator,long interval, IJCatascopiaServer server){
		super("ReDistributor-Thread");
		this.publisher = new Publisher(ip, port, ISocket.ConnectType.CONNECT);

		this.distributorStatus = DistributorStatus.INACTIVE;
		this.firstFlag = true;
		
		this.aggregator = aggregator;
		this.INTERVAL = interval;
		
		this.server = server;
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
		this.publisher.close();
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
						if (interval > INTERVAL){
							String s = aggregator.createMessage();
//							System.out.println(s);
							if (aggregator.length()>0)
								this.publisher.send(s);
							interval = 0;
							this.aggregator.clear();
							if (this.server.inDebugMode())
								System.out.println("ReDistributor>> Message sent to MS Server...\n");
						}
						else interval += period;
						Thread.sleep(period);
					}
					catch(CatascopiaException e){
						this.server.writeToLog(Level.SEVERE, e);
					}
					catch(Exception e){
						this.server.writeToLog(Level.SEVERE, e);
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
			this.server.writeToLog(Level.SEVERE, e);
		}
		finally{
			this.publisher.close();
		}	
	}
}
