package eu.celarcloud.celar_ms.ProbePack;

public enum ProbePropertyType {
	INTEGER, LONG, CHAR, STRING, FLOAT, DOUBLE, BOOLEAN;
	
	public static boolean isType(ProbePropertyType type,Object obj){
		boolean ans = false;
		switch(type){
			case INTEGER:
				if(obj instanceof Integer)
					ans = true;
				break;
			case LONG:
				if(obj instanceof Long)
					ans = true;
				break;
			case CHAR:
				if(obj instanceof Character)
					ans = true;
				break;
			case STRING:
				if(obj instanceof String)
					ans = true;
				break;
			case FLOAT:
				if(obj instanceof Float)
					ans = true;
				break;
			case DOUBLE:
				if(obj instanceof Double)
					ans = true;
				break;	
			default:
				break;
		}
		return ans;	
	}
}
