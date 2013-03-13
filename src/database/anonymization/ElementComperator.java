package database.anonymization;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.derby.impl.tools.ij.Main;

import soot.toolkits.scalar.Pair;

/**
 * Comperator used foe sorting and searching records in a database
 * @author Kunal_Taneja
 *
 */
public class ElementComperator implements Comparator<Object>{

	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		if(o1 == null && o2 == null)
			 return 0;
		else if(o1 != null && o2 == null)
			 return 1;
		else if(o1 == null && o2 != null)
			 return -1;
		
		String c1 = o1.getClass().toString();
		String c2 = o2.getClass().toString();
		if(!c1.equals(c2)){
			if(o1 instanceof Pair<?,?>){ 
				Pair<Integer, Object> p = (Pair<Integer, Object>)o1;
				return compare(p.getO2(), o2);
			}
			else if (o2 instanceof Pair<?,?>){
				Pair<Integer, Object> p = (Pair<Integer, Object>)o2;
				return compare(o1, p.getO2());
			}
			else
				try {
					throw new Exception("Objects of different types cannot be compared");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		if(o1 instanceof Integer){
			Integer i1 = (Integer) o1;
			Integer i2 = (Integer) o2;
			if(i1 > i2)
				return 1;
			else if(i1.equals(i2))
				return 0;
			else
				return -1;
		}
		else if(o1 instanceof java.math.BigInteger){
			BigInteger i1 = (BigInteger) o1;
			BigInteger i2 = (BigInteger) o2;
			
			if(i1.doubleValue() > i2.doubleValue())
				return 1;
			else if(i1.doubleValue() == i2.doubleValue())
				return 0;
			else
				return -1;
		}
		else if(o1 instanceof String){
			String s1 = (String) o1;
			String s2 = (String) o2;
			return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
		}
		else if(o1 instanceof Date || o1 instanceof Timestamp){
			String s1 = o1.toString();
			String s2 = o2.toString();
			return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
		}
		else if(o1 instanceof Long){
			Long l1 = (Long)o1;
			Long l2 = (Long)o2;
			if(l1 > l2)
				return 1;
			else if(l1.equals(l2))
				return 0;
			else
				return -1;
		}
		else if(o1 instanceof Double){
			Double l1 = (Double)o1;
			Double l2 = (Double)o2;
			if(l1 > l2)
				return 1;
			else if(l1 == l2)
				return 0;
			else
				return -1;
		}
		else if(o1 instanceof Boolean){
			Boolean l1 = (Boolean)o1;
			Boolean l2 = (Boolean)o2;
			if(l1== true && l2 == false)
				return 1;
			else if(l1 == l2)
				return 0;
			else
				return -1;
		}
		else if(o1 instanceof Pair<?,?>){
			Pair<Integer, Object> p1 = (Pair<Integer, Object>)o1;
			Pair<Integer, Object> p2 = (Pair<Integer, Object>)o2;
			return compare(p1.getO2(), p2.getO2());
		}
		
		else{
			try {
				throw new Exception("Not Implemented for : " + o1.getClass());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	/*Uses a modification of binary seatch to find the number of elements that matches
	 * the key in the sorted array*/
    public int getNumberElementsMatchingKey(ArrayList<Object> array, Object key, ArrayList<Integer> matchingIndices){
    		   int bot = 0;
    		   int top = array.size() - 1;
    		   while (bot <= top) {
    		      int mid = (bot + top) / 2;
    		      if (compare(key, array.get(mid)) < 0) 
    		    	  top = mid - 1;
    		      else if (compare(key, array.get(mid)) > 0) 
    		    	  bot = mid + 1;
    		      else{
    		    	  int start = getFirst(array, bot, mid);
    		    	  int end = getLast(array, mid, top);
    		    	  for (int i=start; i<=end; i++) {
    		    		  Pair<Integer, Object> p = (Pair<Integer, Object>) array.get(i);
    		    		  matchingIndices.add(p.getO1());
    		    	  }
    		    	  return end - start+1;
    		      }
    		   }
    		   return -1;
    }
    
	


    /*Gets the index of first element that equals the last element in the given sorted array*/
	private int getFirst(ArrayList<Object> array, int bot, int top) {
		Object key = array.get(top);
		if (compare(key, array.get(bot)) == 0)
			return bot; 
		while (bot <= top) {
			 int mid = (bot + top) / 2;
			 Object val = array.get(mid) ;
			 if (compare(key, array.get(mid)) < 0){ 
		    	  try {
					throw new Exception("Not Possible");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
		     else if (compare(key, array.get(mid)) > 0) 
		    	  bot = mid + 1;
		     else{
		    	  if(compare(key, array.get(mid-1)) > 0)
		    		  return mid;
		    	  top = mid - 1;		     
		     }
		 }
		try {
			throw new Exception("Not Possible");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -Integer.MAX_VALUE;
	}
	
	/*Gets the index of last element that equals the first element in the given sorted array*/
	private int getLast(ArrayList<Object> array, int bot, int top) {
		Object key = array.get(bot);
		if (compare(key, array.get(top)) == 0)
			return top;
		 while (bot <= top) {
			 int mid = (bot + top) / 2;
			 if (compare(key, array.get(mid)) < 0){ 
				 top = mid - 1;
			 }
		     else if (compare(key, array.get(mid)) > 0){
		    	 try {
						throw new Exception("Not Possible");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		     }
		     else{
		    	  if(compare(key, array.get(mid+1)) < 0)
		    		  return mid;
		    	  bot = mid+1;		     
		     }
		 }
		 try {
				throw new Exception("Not Possible");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return -Integer.MAX_VALUE;
	}

	/*A test case for testing the binary search in this class*/
	public static void main(String[] args) {
		ArrayList<Object> array = new ArrayList<Object>();
		array.add("1");
		array.add("1");
		array.add("1");
		array.add("1");
		array.add("2");
		array.add("2");
		array.add("2");
		array.add("2");
		array.add("4");
		array.add("5");
		array.add("6");
		array.add("6");
		array.add("6");
		array.add("7");
		array.add("8");
		array.add("8");
		array.add("8");
		array.add("8");
		array.add("9");
		array.add("0");
		array.add("9");
		array.add("18");
		array.add("55");
		array.add("66");
		array.add("78");
		array.add("66");
		array.add("7");
		ElementComperator comp = new ElementComperator();
		Collections.sort(array, comp);
		ArrayList<Integer> indices = new ArrayList<Integer>();
		int N0 = comp.getNumberElementsMatchingKey(array, "845858", indices);
		int N1 = comp.getNumberElementsMatchingKey(array, "1", indices);
		int N2 = comp.getNumberElementsMatchingKey(array, "18", indices);
		int N3 = comp.getNumberElementsMatchingKey(array, "66", indices);
		int N4 = comp.getNumberElementsMatchingKey(array, "8", indices);
		System.out.println(N0 + " " +  N1 + " " + N2 + " " + N3 + " " +N4);
		
	}
    

}
