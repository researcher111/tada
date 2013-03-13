package utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


import soot.Value;
import soot.jimple.spark.pag.ArrayElement;

public class HashMapUtils {
	
	
	public static HashMap<Value, HashSet<Value>> deepMapCopy(HashMap<Value, HashSet<Value>> map){
		HashMap<Value, HashSet<Value>> cloneMap = new HashMap<Value, HashSet<Value>>();
		for (Value val : map.keySet())
			cloneMap.put(val, new HashSet<Value>(map.get(val)));
		return cloneMap;
	}
	
	public static boolean AreCopies(HashMap<Value, HashSet<Value>> map1, HashMap<Value, HashSet<Value>> map2){
		
		if(map1.size() != map2.size())
			return false;
		for (Value v : map1.keySet()) {
			if(!map2.containsKey(v))
				return false;
			HashSet<Value> set1 = map1.get(v);
			HashSet<Value> set2 = map2.get(v);
			if(set1.size() != set2.size()) return false;
			for (Value value : set2) {
				if(!set1.contains(value))
					return false;
			}
			
		}
		
		return true;
	}
}
