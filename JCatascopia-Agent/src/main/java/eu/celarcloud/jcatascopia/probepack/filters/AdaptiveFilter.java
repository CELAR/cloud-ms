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

public class AdaptiveFilter extends Filter{

	private int windowSize;
	private double minR;
	private double maxR;
	private double stepSize;
	private double aggressiveness;
	
	private int n;
	private int filterCount;
	private double range;
	
	public AdaptiveFilter(int windowSize, double minR, double maxR, double stepSize, double aggressiveness){
		this.windowSize = windowSize;
		this.minR = minR;
		this.maxR = maxR;
		this.stepSize = stepSize;
		this.aggressiveness = aggressiveness;
		this.n = 0;
		this.range = minR;
		
		this.window_low -= this.range;
		this.window_high += this.range;
	}
	
	@Override
	public void adjustFilter(double curValue){
		if (this.checkFlag)
			this.filterCount++;
		else{
			this.window_low = curValue - this.range;
			this.window_high = curValue + this.range;
		}
				
		if (n >= this.windowSize - 1){
			System.out.println("Adaptive Filter>> n is "+n+", checking if window range adjusting is needed");
			System.out.println("Adaptive Filter>> percentage of values filtered = "+((1.0)*filterCount)/windowSize);
			if (((1.0)*filterCount)/windowSize < this.aggressiveness){
				this.range = (this.range + this.stepSize < this.maxR) ? this.range + this.stepSize : this.maxR;
				System.out.println("Adaptive Filter>> range is now " + this.range);
			}
			else if (((1.0)*filterCount)/windowSize > this.aggressiveness){
				this.range = (this.range - this.stepSize > this.minR) ? this.range - this.stepSize : this.minR;
				System.out.println("Adaptive Filter>> range is now " + this.range);
			}

			this.n = -1 ;	
			this.filterCount = 0;	
		}
		n++;
	}

//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		AdaptiveFilter filter = new AdaptiveFilter(20,1,3,1,0.1);
//		double[] vals = new double[]{21,65,69,75,79,85,89,99,94,97,91,89,85,87,88,90,88,86,88,90};
//		for(int i=0;i<vals.length;i++){
//			filter.check((vals[i]));
//		}
//		for(int i=0;i<vals.length;i++){
//			filter.check((vals[i]));
//		}
//		vals[2] = 66;
//		vals[3] = 65.5;
//		for(int i=0;i<vals.length;i++){
//			filter.check((vals[i]));
//		}
//	}
}
