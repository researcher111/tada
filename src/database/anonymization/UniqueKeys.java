package database.anonymization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class UniqueKeys {
	public static HashMap<String, ArrayList<ArrayList<String>>> uniqueKeyMap = new HashMap<String, ArrayList<ArrayList<String>>>(1);
	public static String scriptLocation = "C:\\Subject Applications\\accenture\\n2a\\DB_INIT_SCRIPTS\\N2A-full-permuted.SQL";
	//public static String scriptLocation = "C:\\N2Apermuted.SQL";
	
	
	
	public static void addUniqueKey(String table, ArrayList<String> uniqueKey){
		ArrayList<String> uniqueKeyToLower = new ArrayList<String>();
		for (String string : uniqueKey) {
			uniqueKeyToLower.add(string.toLowerCase());
		}
		
		ArrayList<ArrayList<String>> uniqueKeys = uniqueKeyMap.get(table.toLowerCase());
		
		if(uniqueKeys != null){
			uniqueKeyMap.get(table.toLowerCase()).add(uniqueKeyToLower);
		}
		else{
			uniqueKeys = new ArrayList<ArrayList<String>>();
			uniqueKeys.add(uniqueKeyToLower);
			uniqueKeyMap.put(table.toLowerCase(), uniqueKeys);
		}
		
		/*
		ArrayList<ArrayList<String>> uniqueKeys = uniqueKeyMap.get(table);
		
		if(uniqueKeys != null){
			uniqueKeyMap.get(table).add(uniqueKey);
		}
		else{
			uniqueKeys = new ArrayList<ArrayList<String>>();
			uniqueKeys.add(uniqueKey);
			uniqueKeyMap.put(table, uniqueKeys);
		}*/
	}
	
	public static void main(String[] args) {
		loadKeysFromFile();
		
		
	}
	
	public static void loadKeysFromFile(){
		//specific for n2a application
		File file = new File(scriptLocation);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			int tableCounter =0;
			String currentTable = null;
			String line = reader.readLine();
			
			while(line != null){
				if(line.contains("-- Table structure for table")){
					String[] parts = line.split("Table structure for table");
					@SuppressWarnings("unused")
					String tableName = parts[1].trim();
					int last = tableName.length()-1;
					currentTable = tableName.substring(1, last);
					System.out.println(tableCounter++ + " " + currentTable);
				}
				else if(line.contains("UNIQUE KEY")){
					int start = line.indexOf('(');
					int end = line.indexOf(')');
					String keys = line.substring(start+1, end);
					System.out.println(keys);
					ArrayList<String> uniqueKey = extractUniqueKeys(keys); 
					addUniqueKey(currentTable, uniqueKey);
					System.out.println("Updating " + currentTable + "->"+ uniqueKey);
					@SuppressWarnings("unused")
					int debug =1;
				}
					
				line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static ArrayList<String> extractUniqueKeys(String keys) {
		ArrayList<String> uniqueKey = new ArrayList<String>();
		String[] columns = keys.split(",");
		for (String column : columns) {
			column = column.substring(1, column.length()-1) ;
			int debug =1;
			uniqueKey.add(column);
		}
		int debug =1;
		return uniqueKey;
	}
	
}
