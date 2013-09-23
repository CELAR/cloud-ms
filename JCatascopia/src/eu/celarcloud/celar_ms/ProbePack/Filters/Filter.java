package eu.celarcloud.celar_ms.ProbePack.Filters;

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
//		System.out.println(curValue+" "+window_low+" "+window_high);
		if (curValue <= window_high && curValue >= window_low){
			this.checkFlag = true;
//			System.out.println("filtered");
		}
		else{
			this.checkFlag = false;
//			System.out.println("passed");
		}
		this.adjustFilter(curValue);
		return this.checkFlag;
	}
	
	public abstract void adjustFilter(double curValue);

}
