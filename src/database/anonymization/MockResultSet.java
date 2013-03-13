package database.anonymization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;

import soot.toolkits.scalar.Pair;
import tadagui.TaDaFinalView;
	
/**
 * Implements a mock ResultSet that can be kept in memory
 * @author Kunal_Taneja
 *
 */

public class MockResultSet {
	
	String tableName;
	private ArrayList<String> columnNames;
	ArrayList<ArrayList<Object>> records;
	//To be used in permuted result set: Stores permuted result set 
	private ArrayList<Integer> numDifferingColumnsFromOriginal;
	int currentPosition =-1;
	ArrayList<Object> currentRecord;
	/*For efficient searching of records in the table*/
	HashMap<Object, Object> recordMap = new HashMap<Object, Object>();
	
	
	public ArrayList<Object> getRecordsInColumn(int index){
		ArrayList<Object> recordsInColumn = new ArrayList<Object>();
		for (ArrayList<Object> record : records) {
			recordsInColumn.add(record.get(index));
		}
		return recordsInColumn;
	}
	
	
	/*Returns the number of */
	public ArrayList<Object> getRecordsInColumnWithIndices(int index){
		ArrayList<Object> recordsWithIndicesInColumn = new ArrayList<Object>(); 
		int i=0;
		for (ArrayList<Object> record : records) {
			Pair<Integer, Object> valueInColumnWithIndex = new Pair<Integer, Object>(i++, record.get(index));
			recordsWithIndicesInColumn.add((Object) valueInColumnWithIndex);
		}
		return recordsWithIndicesInColumn;
	}
	
	
	/*populates recordMap which is a multi level (number of levels = number of columns to index) hashmap
	 * hence, indexes the data for efficient retrieval.
	 * The ColumnsToIndex is the set of QIs that matter */
	public void indexRecords(ArrayList<String> columnsToIndex){
		 long start = System.currentTimeMillis();
		 for (ArrayList<Object> record : records){
			HashMap<Object, Object> previousTable = null;
			Object previousValue = null;
			if(columnsToIndex == null) return;
			int numColumns = columnsToIndex.size();
			
			for(int i=1; i<=numColumns; i++){
				Object value = record.get(columnNames.indexOf(columnsToIndex.get(i-1)));
				
				/*adding to the hashset*/
				if(i==1 && numColumns==1){
					HashMap<Object, Object> hashTable = new HashMap<Object, Object>();
					if(recordMap.get(value) == null){
						recordMap.put(value, 1);
					}
					else
						recordMap.put(value, (Integer)recordMap.get(value)+1);
					previousTable = recordMap;
				}
				if(i==1){
					HashMap<Object, Object> hashTable = new HashMap<Object, Object>();
					if(recordMap.get(value) == null){
						recordMap.put(value, hashTable);
					}
					previousTable = recordMap;
				}
				else if(i+1 <= numColumns){
					Object obj = previousTable.get(previousValue);
					if(obj instanceof HashMap<?, ?>){
						HashMap<Object, Object> hashTable = (HashMap<Object, Object>) obj;
						previousTable = hashTable;
						if(hashTable.get(value) == null){
							HashMap<Object, Object> newTable = new HashMap<Object, Object>();
							hashTable.put(value, newTable);
						}
							
					} else
						try {
							throw new Exception("Not Possible!!!");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
				else{
					Object obj = previousTable.get(previousValue);
					if(obj instanceof HashMap<?, ?>){
						HashMap<Object, Object> hashTable = (HashMap<Object, Object>) obj;
						if(hashTable.get(value) == null){
							hashTable.put(value, 1);
						}
						else{
							hashTable.put(value, (Integer)hashTable.get(value)+1);
						}
					} else
						try {
							throw new Exception("Not Possible!!!");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
				previousValue = value;
			}
		}
		 long end = System.currentTimeMillis();
		 System.out.println("Num Records in table: " +tableName + " =: " + records.size());
		 System.out.println("Time to index records for table: " +tableName + " =: " + (end-start)/1000 + "sec");
	}
		
	
	
	public MockResultSet(ResultSet rs, String table, ArrayList<String> columnsToIndex) throws SQLException{

		this.tableName = table;
		ResultSetMetaData metaData = rs.getMetaData();
		int numColumns = metaData.getColumnCount();
		
		numDifferingColumnsFromOriginal = new ArrayList<Integer>();
		columnNames = new ArrayList<String>(numColumns);
		records = new ArrayList<ArrayList<Object>>(1);
		
		for(int i=1; i<=numColumns; i++)
			columnNames.add(metaData.getColumnName(i).toLowerCase());
		while(rs.next()){
			ArrayList<Object> record = new ArrayList<Object>();
			HashMap<Object, Object> previousTable = null;

			Object previousValue = null;
			for(int i=1; i<=numColumns; i++){
				Object value = null;
				try{
					value = rs.getObject(i);
				}
				catch(java.sql.SQLException e ){
					int type = DatabaseAnonymizer.getTypeofColumn(table, columnNames.get(i-1));
					if(type == Types.TIMESTAMP)
						value = new Timestamp(0);
					else if(type == Types.DATE)
						value = new Date(0);
					else{
						@SuppressWarnings("unused")
						int debug =1;
					}
				}
				record.add(value);
				previousValue = value;
			}
			records.add(record);
		}
		//rs.absolute(0);
		/*Index record to find the records matching a particular record efficiently*/
		indexRecords(columnNames);
	}
	
	/*gets the number of specified record in the database*/
	public int retrieveNumberofMatchingRecords(ArrayList<Object> record){
		HashMap<Object, Object> previousHashTable = recordMap;
		int N = columnNames.size();
		int i=0;
		for (Object object : record) {
			if(i< N-1){
				if(previousHashTable == null){
					return 0;
				}
				previousHashTable = (HashMap<Object, Object>) previousHashTable.get(object);
				if(previousHashTable == null){
					return 0;
				}
			}
			else if(i == N-1){
				if(previousHashTable.get(object) !=null){
					if(!(previousHashTable.get(object) instanceof Integer)){
						@SuppressWarnings("unused")
						int debug =1;
					}
					Integer n = (Integer) previousHashTable.get(object);
					return n;
				}
				return 0;
			}
			i++;
		}
		
		return 0;
	}
	
	/*gets the number of specified record in the database*/
	public int retrieveNumberofRecords(ArrayList<Object> record, ArrayList<String> indexedColumns){
		HashMap<Object, Object> previousHashTable = recordMap;
		int N = indexedColumns.size();
		int i=0;
		for (String column : indexedColumns) {
			Object object = record.get(i);
			if(i< N-1){
				if(previousHashTable == null){
					return 0;
				}
				previousHashTable = (HashMap<Object, Object>) previousHashTable.get(object);
				if(previousHashTable == null){
					return 0;
				}
			}
			else if(i == N-1){
				if(previousHashTable.get(object) !=null){
					if(!(previousHashTable.get(object) instanceof Integer)){
						@SuppressWarnings("unused")
						int debug =1;
					}
					Integer n = (Integer) previousHashTable.get(object);
					return n;
				}
				return 0;
			}
			i++;
		}
		
		return 0;
	}
	
	
	public ArrayList<Object> getCurrentRecord(){
		return currentRecord;
	}
	
	public Object getColumnAtIndex(int index, String column){
		int columnIndex = columnNames.indexOf(column);
		return records.get(index).get(columnIndex);
	}
	
	public boolean next(){
		if(currentPosition == records.size()-1)
			return false;
		currentPosition++;
		currentRecord = records.get(currentPosition);
		return true;	
	}
	
	public Object getObject(int index){
		return currentRecord.get(index-1);
	}
	
	public Object getObject(String columnName){
		return currentRecord.get(columnNames.indexOf(columnName));
	}
	
	public void setObjectAt(String columnName, Object value){
		int index = columnNames.indexOf(columnName);
		currentRecord.set(index, value);
	}
	
	public void setObjectAt(int index, Object value){
		currentRecord.set(index, value);
	}
	
	public void writeDataToFile(BufferedWriter writer) throws IOException{	
			System.out.println("Writing Table: " + tableName );
			writer.write("ALTER TABLE " + "n2aPermuted." + tableName + " DISABLE KEYS; \n");
			tableIndex++;
			int i=0;
			String query = ""; 
			long start = System.currentTimeMillis();
			int numRecs= records.size();
			if(numRecs == 0 ) return;
			query = "INSERT INTO " + "n2aPermuted." +  tableName + " VALUES";
			int recordIndex =0;
			for (ArrayList<Object> record : records) {
				//query = "INSERT INTO " + tableName + " VALUES( ";
				recordIndex++;
				query = query + "( ";
				boolean isFirst = true;
				int columnIndex=0;
				for (Object object : record) {
					columnIndex++;
					if(object instanceof Integer || object instanceof Long 
							|| object instanceof BigInteger || object instanceof Boolean
							|| object instanceof Double || object instanceof BigDecimal){
						if(!isFirst)
							query = query + "," + object;
						else{ 
							isFirst = false;
							query = query + object;
						}
					}
					else if(object instanceof String || object instanceof Timestamp || object instanceof Time){
						
						if(object instanceof String){
							if(((String) object).contains("'")){
								@SuppressWarnings("unused")
								int debug = 1;
								object = ((String) object).replaceAll("'", "");
							}
						}
						if(!isFirst)
							query = query + "," + "'" + object + "'";
						else{ 
							isFirst = false;
							query = query + "'" + object + "'";
						}
						
					}
					else if (object instanceof Date){
						if(!isFirst)
							query = query + "," + "'" + object.toString() + "'";
						else{ 
							isFirst = false;
							query = query + "'" + object.toString() + "'";
						}
					}
					else if (object == null ){
						int type=-100;
						try {
							type = DatabaseAnonymizer.getTypeofColumn(tableName, columnNames.get(columnIndex-1));
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						if(type == java.sql.Types.TIMESTAMP){
							if(!isFirst)
								query = query + ", NULL" ;
							else{ 
								isFirst = false;
								query = query + "NULL";
							}
						}
						else{
							if(!isFirst)
								query = query + "," + "NULL";
							else{ 
								isFirst = false;
								query = query + "NULL";
							}
						}
					}
					else{
						try {
							throw new Exception("Not Implemented for : " + object.getClass());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				if(recordIndex == numRecs)
					query = query + ")";
				else
					query = query + "),";
			}
			//long start = System.currentTimeMillis();
			//stmt.executeBatch();
			
			writer.write(query + ";\n");
			writer.write("ALTER TABLE " + "n2aPermuted." + tableName + " ENABLE KEYS;\n");
			System.out.println("Table: " + tableName + " written");
	}
	
	
	public static int tableIndex=0;
	public void fillData(Statement stmt, String fileName) throws SQLException{
		
		BufferedWriter writer = null;
		try {
			File file = new File(fileName);
			writer = new BufferedWriter(new FileWriter(file));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Inserting Records in table : " + tableName);
		if(TaDaFinalView.progressBar != null){
			TaDaFinalView.progressBar.setString("Inserting Records in table : " + tableName);
			TaDaFinalView.progressBar.setIndeterminate(false);
			TaDaFinalView.progressBar.setMaximum(records.size());
		}
		//stmt.execute("ALTER TABLE " + tableName + " DISABLE KEYS");
		tableIndex++;
		int i=0;
		String query = ""; 
		long start = System.currentTimeMillis();
		int numRecs= records.size();
		if(numRecs == 0 ) return;
		query = "INSERT INTO " + tableName + " VALUES";
		int recordIndex =0;
		for (ArrayList<Object> record : records) {
			//query = "INSERT INTO " + tableName + " VALUES( ";
			recordIndex++;
			if(TaDaFinalView.progressBar != null)
				TaDaFinalView.progressBar.setValue(recordIndex);
			query = query + "( ";
			boolean isFirst = true;
			int columnIndex=0;
			for (Object object : record) {
				columnIndex++;
				if(object instanceof Integer || object instanceof Long 
						|| object instanceof BigInteger || object instanceof Boolean
						|| object instanceof Double || object instanceof BigDecimal){
					if(!isFirst)
						query = query + "," + object;
					else{ 
						isFirst = false;
						query = query + object;
					}
				}
				else if(object instanceof String || object instanceof Timestamp || object instanceof Time){
					
					if(object instanceof String){
						if(((String) object).contains("'")){
							@SuppressWarnings("unused")
							int debug = 1;
							object = ((String) object).replaceAll("'", "");
						}
					}
					if(!isFirst)
						query = query + "," + "'" + object + "'";
					else{ 
						isFirst = false;
						query = query + "'" + object + "'";
					}
					
				}
				else if (object instanceof Date){
					if(!isFirst)
						query = query + "," + "'" + object.toString() + "'";
					else{ 
						isFirst = false;
						query = query + "'" + object.toString() + "'";
					}
				}
				else if (object == null ){
					int type = DatabaseAnonymizer.getTypeofColumn(tableName, columnNames.get(columnIndex-1));
					
					if(type == java.sql.Types.TIMESTAMP){
						if(!isFirst)
							query = query + ", NULL" ;
						else{ 
							isFirst = false;
							query = query + "NULL";
						}
					}
					else{
						if(!isFirst)
							query = query + "," + "NULL";
						else{ 
							isFirst = false;
							query = query + "NULL";
						}
					}
					//columnNames.get
					/*
					int type = DatabaseAnonymizer.getTypeofColumn(tableName, columnNames.get(columnIndex-1));
					if(type == java.sql.Types.CHAR || type == java.sql.Types.VARCHAR || type == java.sql.Types.DATE){
						if(!isFirst)
							query = query + "," + "'" + "'";
						else{ 
							isFirst = false;
							query = query + "'" + "'";
						}
					}
					else{
						if(!isFirst)
							query = query + ",";
						else{ 
							isFirst = false;
							query = query;
						}
					}*/
					
				}
				else{
					try {
						throw new Exception("Not Implemented for : " + object.getClass());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if(recordIndex == numRecs)
				query = query + ")";
			else
				query = query + "),";
			if(recordIndex % 100 == 0){
				try{
					
					int index = query.lastIndexOf(",");
					int l = query.length();
					if(query.lastIndexOf(",") == query.length()-1)
						query = query.substring(0, query.lastIndexOf(','));
					stmt.execute(query);
					writer.write(query + ";\n");
					System.out.println("Executed query = " + query);
					
				}
				catch(Exception e){
					System.out.println("Executed query = " + query);
					e.printStackTrace();
					//return;
				}	
				query = "INSERT INTO " + tableName + " VALUES ";
				System.out.println("Inserted Records =  " + recordIndex + "/" + numRecs);
				System.out.println("Time to insert records: " + (System.currentTimeMillis() - start) + " sec");
			}
			/*stmt.addBatch(query);
			if(recordIndex % 50000 == 0){
				stmt.executeBatch();
				stmt.clearBatch();
				System.out.println("Inserted Records =  " + numRecs );
				System.out.println("Time to insert records: " + (System.currentTimeMillis() - start) + " sec");
			}*/
			//stmt.getB
			//stmt.execute(query);
		}
		//long start = System.currentTimeMillis();
		//stmt.executeBatch();
		try{
			stmt.execute(query);
			System.out.println("Executed query = " + query);
		}
		catch(Exception e){
			System.out.println("Executed query = " + query);
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		//stmt.execute("ALTER TABLE " + tableName + " ENABLE KEYS");
		System.out.println(tableIndex + ": Time to insert records: " + (start-end) + " sec");
		if(TaDaFinalView.progressBar != null)
			TaDaFinalView.progressBar.setIndeterminate(true);
	}

	public double getNumRecords() {
		// TODO Auto-generated method stub
		return records.size();
	}
	
	public void reset(){
		currentPosition =-1;
	}

	public MockResultSet(){
		
	}
	public MockResultSet cloneRS() {
		// TODO Auto-generated method stub
		MockResultSet clonedSet = new MockResultSet();
		clonedSet.columnNames = new ArrayList<String>(this.columnNames);
		clonedSet.currentPosition = this.currentPosition;
		//clonedSet.currentRecord = new ArrayList<Object>(this.currentRecord);
		clonedSet.tableName = this.tableName;
		clonedSet.records = new ArrayList<ArrayList<Object>>();
		for (ArrayList<Object> recds : this.records) {
			clonedSet.records.add(new ArrayList<Object>(recds));
		}
		
		return clonedSet;
	}
	
	public int getIndexofColumn(String column){
		return columnNames.indexOf(column);
	}
	
	public ArrayList<Object> selectColumnsOfCurrentRecord(ArrayList<String> columnsToSelect){
		ArrayList<Object> selectedAttributes = new ArrayList<Object>();
		for (String column : columnsToSelect) {
			Object value = currentRecord.get(getIndexofColumn(column));
			selectedAttributes.add(value);
		}
		return selectedAttributes;
	}

	public void removeRecordsAfterThis() {
		// TODO Auto-generated method stub
		while(currentPosition < records.size()){
			Object o = records.remove(currentPosition);
		}
		currentPosition = -1;
		currentRecord = null;
	}


	public void addNumDifferingColumnsFromOriginal(
			Integer n) {
		this.numDifferingColumnsFromOriginal.add(n);
	}


	public ArrayList<Integer> getNumDifferingColumnsFromOriginal() {
		return numDifferingColumnsFromOriginal;
	}


	public void setColumnNames(ArrayList<String> columnNames) {
		this.columnNames = columnNames;
	}


	public ArrayList<String> getColumnNames() {
		return columnNames;
	}
}
