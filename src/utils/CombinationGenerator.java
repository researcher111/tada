package utils;

//--------------------------------------
// Systematically generate combinations.
//--------------------------------------

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class CombinationGenerator {
	private HashMap<String, ArrayList<Object>> column2Records;
	private ArrayList<String> primaryKeys;
	ArrayList<Integer> startIndices;
	Random random = new Random();
	ArrayList<Integer> currentIndices;
	int lastVariedColumn =-1;
	boolean isStart;
	/*Controls the shuffling probability of arrays*/
	double probability = 1.0;
	
	
	private HashMap<String, ArrayList<Object>> column2Records2Shuffle = new HashMap<String, ArrayList<Object>>();
	private ArrayList<String> columnsToShuffle = new ArrayList<String>();;
	
	void randomizeArray(ArrayList<Object> array){
		//--- Shuffle by exchanging each element randomly
		for (int i=0; i<array.size(); i++) {
			double randomNumber = random.nextDouble();
			if(randomNumber <= probability){
				int randomPosition = random.nextInt(array.size());
			    Object temp = array.get(i);
			    array.set(i, array.get(randomPosition));
			    array.set(randomPosition, temp);
			}
		}
		HashSet<Object> set = new HashSet<Object>(array);
		if(set.size() != array.size()){
			try {
				throw new Exception();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public CombinationGenerator(HashMap<String, ArrayList<Object>> column2Records, 
			ArrayList<String> primaryKeys, double prob){
		this.column2Records = column2Records;
		this.primaryKeys = primaryKeys;
		startIndices = new ArrayList<Integer>();
		for (String key : primaryKeys) {
			ArrayList<Object> list = column2Records.get(key);
			if(column2Records.get(key).size() == 0)
				startIndices.add(-1);
			else
				startIndices.add(random.nextInt(column2Records.get(key).size()));
		}
		currentIndices = new ArrayList<Integer>(startIndices);
		isStart = true;
		for (String key : primaryKeys) {
			//randomizeArray(column2Records.get(key));
		}
		this.probability = prob;
	}
	boolean first = true;
	private void incrementCurrentIndices(int indexToUpdate){
		if(indexToUpdate == 0)
			if(!first)
				if(currentIndices.get(0).equals(startIndices.get(0))){
					try {
						throw new Exception("Combinations Exhausted!!");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						for (String  s : primaryKeys) {
							
							ArrayList<Object> list = column2Records.get(s);
							int index = primaryKeys.indexOf(s);
						}
						e.printStackTrace();
					}
				}
				else
					first = false;
		
		int currentValue = currentIndices.get(indexToUpdate);
		int size =  getRecordsAtIndex(indexToUpdate).size();
		if(currentValue < size-1)
			currentIndices.set(indexToUpdate, currentValue+1);
		
		else
			currentIndices.set(indexToUpdate, 0);
		
		
		
	}
	
	private ArrayList<Object> getRecordsAtIndex(int index){
		return column2Records.get(primaryKeys.get(index));
	}
	
	private int getCurrentIndexInColumn(String column){
		return currentIndices.get(primaryKeys.indexOf(column));
	}
	private int getCurrentIndexInColumn(int columnIndex){
		return currentIndices.get(columnIndex);
	}
	
	private ArrayList<Object> getRecordsForCurrentIndices(){
		ArrayList<Object> records = new ArrayList<Object>();
		for (String key : primaryKeys) {
			int currentIndexInColumn = getCurrentIndexInColumn(key);
			Object obj = column2Records.get(key).get(currentIndexInColumn);
			records.add(obj);
		}
		return records;
	}
	
	int numGen =0;
	public ArrayList<Object> getNext(boolean isDummyCall){
		System.out.println(numGen++ + " " + this.currentIndices);
		ArrayList<Object> nextCombo = new ArrayList<Object>();
		//for(int i= primaryKeys.size()-1; i>=0; i--){
		int last = primaryKeys.size()-1;
			if(isStart){
				isStart=false;
				incrementCurrentIndices(last);
				nextCombo = getRecordsForCurrentIndices();
				lastVariedColumn = last;
				return nextCombo;
			}
			if(currentIndices.get(lastVariedColumn).equals(startIndices.get(lastVariedColumn))){
				if(lastVariedColumn==0){
					try {
						throw new Exception("all combinations exhausted");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						/*for (String  s : primaryKeys) {
							
							ArrayList<Object> list = column2Records.get(s);
							int index = primaryKeys.indexOf(s);
						}*/
						e.printStackTrace();
					}
				}
				else{
					incrementCurrentIndices(lastVariedColumn-1);
					lastVariedColumn = lastVariedColumn-1;
					int temp = lastVariedColumn-1;
					nextCombo = getRecordsForCurrentIndices();
					getNext(true);
					nextCombo = getRecordsForCurrentIndices();
					return nextCombo;
				}
				// if (lastVariedColumn == primaryKeys.size()-1)
			
			}
			else{
				if (lastVariedColumn == primaryKeys.size()-1){
					incrementCurrentIndices(lastVariedColumn);
					lastVariedColumn = lastVariedColumn;
					nextCombo = getRecordsForCurrentIndices();
					return nextCombo;
				}
				incrementCurrentIndices(primaryKeys.size()-1);
				lastVariedColumn = primaryKeys.size()-1;
				nextCombo = getRecordsForCurrentIndices();
				return nextCombo;
			
			}
		return nextCombo;
	}
	
	public static void main(String[] args) {
		HashMap<String, ArrayList<Object>> column2Records = new HashMap<String, ArrayList<Object>>();
		ArrayList<String> primaryKeys = new ArrayList<String>();
		primaryKeys.add("a");
		primaryKeys.add("b");
		primaryKeys.add("c");
		ArrayList<Object> recA = new ArrayList<Object>();
		
		for(int i=0; i< 10000; i++){
			recA.add(i);
		}
		
		/*recA.add("a");
		recA.add("b");
		recA.add("c");
		recA.add("d");*/
		
		ArrayList<Object> recB = new ArrayList<Object>();
		recB.add("1");
		column2Records.put("b", recB);
		
		ArrayList<Object> recC = new ArrayList<Object>();
		recC.add("l");
		column2Records.put("c", recC);

		column2Records.put("a", recA);
		
		
		CombinationGenerator cg = new CombinationGenerator(column2Records, primaryKeys, 1.0);
		int i=0;
		while(true){
			System.out.println(i++);
			if(i== 9888){
				@SuppressWarnings("unused")
				int debug =1;
			}
			System.out.println(cg.getNext(false));
		}
		
	}
}