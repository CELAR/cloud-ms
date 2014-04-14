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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CatascopiaExecutor {
	
	private ExecutorService executor; 
	/**
	 * when threads are full and thread pool queue is full we use a 
	 * oldest object discarding policy. This way, we process only the most recent 
	 * metrics
	 * 
	 * @param core_pool_size core thread pool size
	 * @param max_pool_size  max thread pool size
	 * @param resize_time    time to wait for resizing pool in seconds
	 * @param queue_size     number of runnable objects to store in queue
	 */
	public CatascopiaExecutor(int core_pool_size,int max_pool_size,long resize_time,int queue_size){
		this.executor = new ThreadPoolExecutor(
				            	core_pool_size, 
				            	max_pool_size, 
				            	resize_time, 
				            	TimeUnit.SECONDS,
				            	new ArrayBlockingQueue<Runnable>(queue_size,true),
				            	new ThreadPoolExecutor.DiscardOldestPolicy()
							);
	}
	
	public void process(Runnable r){
		this.executor.execute(r);
	}
	
	public void terminate(){
		this.executor.shutdown();
		try {
			this.executor.awaitTermination(1, TimeUnit.MINUTES);
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
