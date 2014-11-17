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
 * The AdaptiveFilter is used to filter values in the window [previousValue-Range,previousValue+Range].
 * The difference of this filter from other range filters, is that the Range is not fixed but is can
 * vary and it depends on thresholds defined by users. 
 * 
 * @author Demetris Trihinas
 *
 */
public class AdaptiveFilter extends Filter{

	private int windowSize; //number of samples to take into consideration when adapting window range
	private double minR;   //min range
	private double maxR;   //max range
	private double stepSize; //step to change range i.e. range += stepSize
	private double aggressiveness; //threshold
	
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
		this.filterCount = 0;
		this.range = minR;
		
		this.window_low -= this.range;
		this.window_high += this.range;
	}
	
	@Override
	public void adjustFilter(double curValue){
		if (!this.checkFlag){
			this.window_low = curValue - this.range;
			this.window_high = curValue + this.range;
		}//regular RangeFilter until here
		else
			this.filterCount++;
		
		//after n samples re-calculate the window range
		if (n >= this.windowSize - 1){
//			System.out.println("Adaptive Filter>> n: " + n + ", checking if window range adjusting is needed");
//			System.out.println("Adaptive Filter>> percentage of values filtered = "+((1.0) * filterCount) / windowSize);
			
			if (((1.0) * filterCount) / windowSize < this.aggressiveness){
				this.range = (this.range + this.stepSize < this.maxR) ? this.range + this.stepSize : this.maxR;
//				System.out.println("Adaptive Filter>> expanding range to: " + this.range);
			}
			else if (((1.0) * filterCount) / windowSize > this.aggressiveness){
				this.range = (this.range - this.stepSize > this.minR) ? this.range - this.stepSize : this.minR;
//				System.out.println("Adaptive Filter>> narrowing range to: " + this.range);
			}

			this.n = -1 ;	
			this.filterCount = 0;	
		}
		n++;
	}
}
