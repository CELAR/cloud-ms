package eu.celarcloud.celar_ms.ServerPack.subsciptionPack;

import java.util.List;

public class Functions {

	public static int sumInt(List<String> values){
		int sum = 0;
		for(String s : values)
			sum += Integer.parseInt(s);
		return sum;
	}
	
	public static long sumLong(List<String> values){
		long sum = 0;
		for(String s : values)
			sum += Long.parseLong(s);
		return sum;
	}
	
	public static double sumDouble(List<String> values){
		double sum = 0.0;
		for(String s : values)
			sum += Double.parseDouble(s);
		return sum;
	}

	public static double avgDouble(List<String> values){
		double sum = Functions.sumDouble(values);
		return sum/values.size();
	}
	
	public static int maxInt(List<String> values){
		int max = Integer.parseInt(values.get(0));
		int x;
		for(String s : values){
			x = Integer.parseInt(s);
			if(x > max)
				max = x;
		}
		return max;
	}
	
	public static long maxLong(List<String> values){
		long max = Long.parseLong(values.get(0));
		long x;
		for(String s : values){
			x = Long.parseLong(s);
			if(x > max)
				max = x;
		}
		return max;
	}
	
	public static double maxDouble(List<String> values){
		double max = Double.parseDouble(values.get(0));
		double x;
		for(String s : values){
			x = Double.parseDouble(s);
			if(x > max)
				max = x;
		}
		return max;
	}
	
	public static int minInt(List<String> values){
		int min = Integer.parseInt(values.get(0));
		int x;
		for(String s : values){
			x = Integer.parseInt(s);
			if(x < min)
				min = x;
		}
		return min;
	}
	
	public static long minLong(List<String> values){
		long min = Long.parseLong(values.get(0));
		long x;
		for(String s : values){
			x = Long.parseLong(s);
			if(x < min)
				min = x;
		}
		return min;
	}
	
	public static double minDouble(List<String> values){
		double min = Double.parseDouble(values.get(0));
		double x;
		for(String s : values){
			x = Double.parseDouble(s);
			if(x < min)
				min = x;
		}
		return min;
	}
}
