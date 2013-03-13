package tadadriver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import utils.CollectionsExt;

public  class CoverageDatabaseAttributeMapping {
	public static HashMap<String, Integer> coverageDatabaseAttributeMap;
	HashSet<String> statementsControlled = new HashSet<String>();

	private static HashMap<HashSet<String>, Integer> combinedAttrCoverageMap = 
		new HashMap<HashSet<String>, Integer>(); 
	
	public static TreeMap<Integer, HashSet<String>> getSortedMap(){
		TreeMap<Integer, HashSet<String>> sortedMap = new TreeMap<Integer, HashSet<String>>();
		for (String attribute : coverageDatabaseAttributeMap.keySet()) {
			Integer loc = coverageDatabaseAttributeMap.get(attribute);
			if(sortedMap.containsKey(loc)){
				sortedMap.get(loc).add(attribute);
			}
			else{
				HashSet<String> value = new HashSet<String>();
				value.add(attribute);
				sortedMap.put(loc, value);
			}
		}
		
		return sortedMap;
	}
	
	
	public CoverageDatabaseAttributeMapping(){
		coverageDatabaseAttributeMap = new HashMap<String, Integer>(1);
	}
	
	public HashMap<String, Integer> getMap(){
		return coverageDatabaseAttributeMap;
	}
	
	public Integer getCoverageFor(String table, String column){
		String key = table + "." + column;
		if(coverageDatabaseAttributeMap.get(key) == null)
			return 0;
		return coverageDatabaseAttributeMap.get(key);
	}
	
	public Integer getCoverageFor(String attributeFQN){
		if(coverageDatabaseAttributeMap.get(attributeFQN) == null)
			return 0;
		return coverageDatabaseAttributeMap.get(attributeFQN);
	}
	
	
	public void addCoverageFor(String attributeFQN, Integer loc){
		if(!coverageDatabaseAttributeMap.containsKey(attributeFQN))
			coverageDatabaseAttributeMap.put(attributeFQN, loc);
		else{
			int newLOC = coverageDatabaseAttributeMap.get(attributeFQN) + loc;
			coverageDatabaseAttributeMap.put(attributeFQN, newLOC);
		}
	}
	
	
	public static void addCoverageFor(String table, String column, Integer loc){
		String key = table + "." + column;
		if(!coverageDatabaseAttributeMap.containsKey(key))
			coverageDatabaseAttributeMap.put(key, loc);
		else{
			int newLOC = coverageDatabaseAttributeMap.get(key) + loc;
			coverageDatabaseAttributeMap.put(key, newLOC);
		}
	}
	
	public static void addCombinedCoverage(HashSet<String> attributes, Integer loc){
		for (HashSet<String> attrSet: combinedAttrCoverageMap.keySet()) {
			if(attributes.equals(attrSet)){
				int cov = combinedAttrCoverageMap.get(attrSet);
				combinedAttrCoverageMap.put(attrSet, loc + cov);
				return;
			}
		}
		combinedAttrCoverageMap.put(attributes, loc);
	}


	/**
	 * @return the combinedAttrCoverageMap
	 */
	public static HashMap<HashSet<String>, Integer> getCombinedAttrCoverageMap() {
		return combinedAttrCoverageMap;
	}
	
	public static ArrayList<Object> computeEstimatedCoverageLoss(HashSet<String> quasiIdentifiers){
		double percentage = 0.0;
		int loc=0;
		int total =0;
		Set<Set<String>> allSubsets = CollectionsExt.allSubsets(quasiIdentifiers);
		for (HashSet<String> keys : combinedAttrCoverageMap.keySet()) {
			total = total + combinedAttrCoverageMap.get(keys);
			for  (Set<String> subset: allSubsets) {
				if(subset.size() == 0) continue;
				if(keys.containsAll(subset)){
					loc = loc + combinedAttrCoverageMap.get(keys);
					break;
				}
			}
		}
		percentage = (loc*100)/total;
		ArrayList<Object> locPercent = new ArrayList<Object>(2);
		locPercent.add(loc);
		locPercent.add(percentage);
		return locPercent;
	}
	
	/******
	 * 
	public static void main(String[] args) {
		File f1 = new File("c:\\coverage.txt");
		File f2 = new File("c:\\baseline\\coverage.txt");
		File outputFile = new File("c:\\coverageDiagnose.txt");
		HashSet<String> branches1 = new HashSet<String>();
		HashSet<String> branches2 = new HashSet<String>();
		
		HashSet<String> copy1 = new HashSet<String>();
		HashSet<String> copy2 = new HashSet<String>();
		try {
			PrintStream output = new PrintStream(outputFile);
			FileInputStream stream1 = new FileInputStream(f1);
			FileInputStream stream2 = new FileInputStream(f2);
		    DataInputStream in1 = new DataInputStream(stream1);
		    DataInputStream in2 = new DataInputStream(stream2);
		    BufferedReader br1 = new BufferedReader(new InputStreamReader(in1));
		    BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
		    String strLine;
		    while ((strLine = br1.readLine()) != null)   {
		        // Print the content on the console
		        branches1.add(strLine);
		    }
		    while ((strLine = br2.readLine()) != null)   {
		        // Print the content on the console
		        branches2.add(strLine);
		    }
		    copy1 = new HashSet<String>(branches1);
		    copy2 = new HashSet<String>(branches2);
		     
		    branches2.removeAll(branches1);
		    for (String string : branches2) {
				output.println(string);
			}
		    output.println("##########################################");
		    copy1.removeAll(copy2);
		    for (String string : copy1) {
				output.println(string);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	*/
}
