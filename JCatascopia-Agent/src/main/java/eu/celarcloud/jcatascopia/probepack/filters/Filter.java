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

public abstract class Filter {

	protected double window_low;
	protected double window_high;
	protected boolean checkFlag;
	
	public Filter(){
		this.window_low = this.window_high;
	}
	
	/**
	 * checks if provided value is in filter window. 
	 * if in window then it returns true else it returns false.
	 * window is then adjusted based on the filter developer's implementation
	 * of the adjustWindow() method
	 * @return
	 */
	public boolean check(double curValue){
		//System.out.println(curValue+" "+window_low+" "+window_high);
		if (curValue <= window_high && curValue >= window_low){
			this.checkFlag = true;
			//System.out.println("filtered");
		}
		else{
			this.checkFlag = false;
			//System.out.println("passed");
		}
		this.adjustFilter(curValue);
		return this.checkFlag;
	}
	
	public abstract void adjustFilter(double curValue);

}
