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
package eu.celarcloud.jcatascopia.probepack.filters;

/**
 * Abstract Filter class used to create customized filters that can be attached to ProbeProperties
 * 
 * @author Demetris Trihinas
 *
 */
public abstract class Filter {

	protected double window_low;
	protected double window_high;
	protected boolean checkFlag;
	
	private boolean globalFilterFlag = false; //if set to true and a metric must be filtered then no other ProbeProperty will be sent to Agent
	
	public Filter(){
		this.window_low = this.window_high;
	}
	
	/**
	 * This method checks if the provided value is in filter window. 
	 * If the current value is in the filter window then the return value is true else false.
	 * The filter window is adjusted based on the filter developer's implementation of the adjustWindow() method
	 * @return
	 */
	public boolean check(Object val){
		double curValue = Double.valueOf(val.toString());
		
//		System.out.println("curValue: "+curValue+", window_low: "+window_low+", window_high: "+window_high);
		
		if (curValue <= window_high && curValue >= window_low)
			this.checkFlag = true;
		else
			this.checkFlag = false;
		
		this.adjustFilter(curValue);
		
//		if (this.checkFlag)
//			System.out.println("filtered");
//		else
//			System.out.println("passed");

		return this.checkFlag;
	}
	
	public abstract void adjustFilter(double curValue);

	public void setGlobalFilterFlag(boolean flag){
		this.globalFilterFlag = flag;
	}
	
	public boolean getGlobalFilterFlag(){
		return this.globalFilterFlag;
	}
}
