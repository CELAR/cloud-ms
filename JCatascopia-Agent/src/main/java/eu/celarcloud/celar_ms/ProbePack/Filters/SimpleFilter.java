package eu.celarcloud.celar_ms.ProbePack.Filters;

public class SimpleFilter extends Filter{
	
	private double range;
	
	public SimpleFilter(double range){
		this.range = range;
	}
	
	@Override
	public void adjustFilter(double curValue) {
		if (!this.checkFlag){
			this.window_low = curValue - this.range;
			this.window_high = curValue + this.range;
		}
	}
}
