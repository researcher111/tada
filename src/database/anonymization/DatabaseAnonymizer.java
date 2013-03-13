package database.anonymization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.text.TabExpander;

import org.apache.derby.client.net.NetDatabaseMetaData40;
import org.apache.derby.impl.sql.compile.GetCurrentConnectionNode;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;



import soot.JastAddJ.PrimaryExpr;
import soot.jimple.BreakpointStmt;
import soot.toolkits.scalar.Pair;
import tadagui.AbstractComboBoxModel;
import tadagui.ProgressBar;
import tadagui.TaDaCmdline;
import tadagui.TaDaFinalView;
import tadagui.TaDaView;
import utils.CombinationGenerator;


public class DatabaseAnonymizer implements Runnable {
	/*A set of quasi identifiers that we need to anonymize*/
	private HashMap<String, ArrayList<String>> quasiIdentifiers = new HashMap<String, ArrayList<String>>(1);
	/*A set of sensitive attributes*/
	private HashSet<String> sensitiveAttributes = new HashSet<String>(1);
	
	private HashMap<String, Double> anonymizationScores = new HashMap<String, Double>(1);
	private static Logger logger;
	
	public static Connection dbConnection;
	public static Connection permutedDBConnection;
	private double permutationProbability = 1.0;
	Random random = new Random();
	private HashMap<String, MockResultSet> permutedData = new HashMap<String, MockResultSet>(1);
	//private String permutedDatabaseConnectionString = "jdbc:mysql://localhost:3306/n2aPermuted";
	//private String permutedDatabaseConnectionString = "jdbc:derby://localhost:1527/riskitPermuted;create=true;user=se549;password=se549";
	private String permutedDatabaseConnectionString = "jdbc:mysql://localhost:33061/priest_durbodax_permuted";
	//private String permutedDatabaseConnectionString = "jdbc:derby://localhost:1527/UnixUserPermuted;create=true;user=me;password=mine";
	//public static String schema = "n2a";
	//public static String schema = "me";
	//public static String schema = "se549";
	public static String schema = "priest_durbodax_permuted";
	static HashMap<String, ArrayList<ArrayList<String>>> uniqueKeyMap;
	
	private double totalRecords=0;
	double cumulativeAnonymizationScore=0;
	double cumulativeUniquenessScore=0;
	private double unchangedScore =0;
	private double totalUniqueRecords=0;
	
	/*Set if the permuted database is not written but only the scores are calculated*/
	private boolean scoreOnly = false;
	/*Set if no score calculation is to be done*/
	private boolean noScoreCalculation = false;
	private boolean uniqueScoreOnly = false;
	
	
	boolean writeRecordsToFile = false;
	BufferedWriter writer;
	
	JProgressBar progressBar = TaDaFinalView.progressBar;
	
	public DatabaseAnonymizer(double probability, String scriptFile) throws IOException{
		
		writeRecordsToFile = true;
		FileWriter fw = new FileWriter(new File(scriptFile));
		writer = new BufferedWriter(fw);
		
		dbConnection = AbstractComboBoxModel.getConnection();
		if(dbConnection == null) dbConnection = TaDaCmdline.dbConnection;
		try {
				//if(permutedDatabaseConnectionString.contains("mysql")) 
					permutedDBConnection =  DriverManager.getConnection(permutedDatabaseConnectionString, "root", "-----");
				//else
					//permutedDBConnection = DriverManager.getConnection(permutedDatabaseConnectionString);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.permutationProbability = probability;
		try {
			addQISFRomFile();
			//used for n2a since unique keys cannot be found using the meta model
			//UniqueKeys.loadKeysFromFile();
			//uniqueKeyMap = UniqueKeys.uniqueKeyMap;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public DatabaseAnonymizer(double probability){
		logger = Logger.getLogger(DatabaseAnonymizer.class.getName());
		logger.setLevel(Level.OFF);
		dbConnection = AbstractComboBoxModel.getConnection();
		if(dbConnection == null) dbConnection = TaDaCmdline.dbConnection;
		try {
			//if(permutedDatabaseConnectionString.contains("mysql"))
				permutedDBConnection =  DriverManager.getConnection(permutedDatabaseConnectionString, "root", "-----");
				//else
				//permutedDBConnection =  DriverManager.getConnection(permutedDatabaseConnectionString);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.permutationProbability = probability;
		//try {
		//	addQISFRomFile();
		//} catch (IOException e) {
		//	e.printStackTrace();
		//}
		addQISFromUI();
		if(permutedDatabaseConnectionString.contains("mysql")){
			UniqueKeys.loadKeysFromFile();
			uniqueKeyMap = UniqueKeys.uniqueKeyMap;
		}
		
	}
	
	public String outputSQLFile = "permutedDB.sql";
	boolean isInvokedThroughCommandLine = false;
	public DatabaseAnonymizer(Connection connection, double probability,
			String permutedDatabaseConnectionString, String outputSQLFIle,
			boolean commandLine, boolean scoreonly, boolean uniqueScoreOnly) {
		this.dbConnection = connection;
		this.permutationProbability = probability;
		this.permutedDatabaseConnectionString = permutedDatabaseConnectionString;
		this.isInvokedThroughCommandLine = commandLine;
		this.outputSQLFile = outputSQLFIle;
		this.scoreOnly = scoreonly;
		this.uniqueScoreOnly = uniqueScoreOnly;
		
		try {
			addQISFRomFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			permutedDBConnection = DriverManager.getConnection(permutedDatabaseConnectionString);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			FileHandler handler = new FileHandler("/home/cmc/projects/priest/workspace/DatabaseAnonimizationLog.txt", true);
			handler.setFormatter(new SimpleFormatter());
			logger = Logger.getLogger(DatabaseAnonymizer.class.getName());
			logger.addHandler(handler);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	public void addQISFRomFile() throws IOException{
		File file = new File("/home/cmc/projects/priest/workspace/coverage.txt");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = reader.readLine();
		while (line != null) {
			String[] parts = line.split(" ->");
			int separator = parts[0].indexOf('.');
			String table = parts[0].substring(0, separator);
			String column = parts[0].substring(separator+1, parts[0].length());
			line = reader.readLine();
			addQI(table, column);
			System.out.println("Added QI " + table + "." + column);
			//if(!isInvokedThroughCommandLine)
				//progressBar.setString("Added QI " + table + "." + column);
		} 
	}
	public void addQISFromUI(){
		//for (Object o : TaDaFinalView.arQI) {
		for (Object o : TaDaCmdline.arQI) {
			String s = (String)o;
			int separator = s.indexOf('.');
			String table = s.substring(0, separator);
			String column = s.substring(separator+1, s.length());
			addQI(table, column);
			System.out.println("Added QI " + table + "." + column);
			//if(!isInvokedThroughCommandLine)
				//progressBar.setString("Added QI " + table + "." + column);
		}
	}
	
	
	
	/**
	 * Updates the QIs by looking by adding the foreign attributes associated with the QIs 
	 */
	private void updateQIsUsingForeignConstraints() {
		// TODO Auto-generated method stub

		HashMap<String , ArrayList<String>> toAdd = new HashMap<String, ArrayList<String>>();
		/*To allow concurrent changes to QI map*/
		HashMap<String, ArrayList<String>> qiCopy = new HashMap<String, ArrayList<String>>(quasiIdentifiers);
		
		for (String table : qiCopy.keySet()) {
			ArrayList<String> attrs =  qiCopy.get(table);
			
			try {
				getTransitiveExports(table, attrs);
			/*	HashMap<String, String> foreignTables = getForeignKeysInTable(dbConnection.getMetaData(), table);
				for (String attribute : attrs) {
					
						if(!foreignTables.keySet().contains(attribute))
							continue;
						for (String foreignKey : foreignTables.keySet()) {
							String value =  foreignTables.get(foreignKey);
							String[] tableColumn = value.split("-");
							String foreignTable = tableColumn[0];
							String foreignAttribute = tableColumn[1];
							if(quasiIdentifiers.get(foreignTable) == null){
								ArrayList<String> QISet = new ArrayList<String>();
								QISet.add(foreignAttribute);
								//quasiIdentifiers.put(foreignTable, QISet);
								//To avoid concurrent changes to quasiIdentifiers map
								if(toAdd.get(foreignTable) == null){
									toAdd.put(foreignTable, QISet);
								}
								else
									toAdd.get(foreignTable).add(foreignAttribute);
							}
							else{
								addQI(foreignTable, foreignAttribute);
							}
						}
					
				}*/
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		quasiIdentifiers.putAll(toAdd);
	}
	
	public void getTransitiveExports(String table, ArrayList<String> QIsInTable) throws SQLException{
		HashMap<String, HashSet<String>> exports = getExportedKeys(dbConnection.getMetaData(), table);
		HashMap<String , ArrayList<String>> toAdd = new HashMap<String, ArrayList<String>>();
		
		for (String qi : QIsInTable) {
			if(!exports.keySet().contains(qi))
				continue;
			HashSet<String> exportedSet =  exports.get(qi);
			for (String value : exportedSet) {
				String[] tableColumn = value.split("-");
				String foreignTable = tableColumn[0];
				String foreignAttribute = tableColumn[1];
				if(quasiIdentifiers.get(foreignTable) == null){
					ArrayList<String> QISet = new ArrayList<String>();
					QISet.add(foreignAttribute);
					if(toAdd.get(foreignTable) == null){
						toAdd.put(foreignTable, QISet);
					}
					else
						toAdd.get(foreignTable).add(foreignAttribute);
				}
				else{
					addQI(foreignTable, foreignAttribute);
				}
			}
		}
		quasiIdentifiers.putAll(toAdd);
		for (String fTable : toAdd.keySet()) {
			getTransitiveExports(fTable, toAdd.get(fTable));
		}
	}
	/**
	 * Returns an ordered list of tables in an order anonymization could be applied without violating foreign key constraints
	 * @throws SQLException
	 * 
	 * */
	public ArrayList<String> orderTablesForAnonymization() throws SQLException{
		DatabaseMetaData meta = dbConnection.getMetaData();
        ResultSet res = meta.getTables(schema, null, null,new String[] {"TABLE"});
        ArrayList<String> tables = new ArrayList<String>(1);
        ArrayList<String> sortedTableList = new ArrayList<String>(1);
        while(res.next()){
        	tables.add(res.getString("TABLE_NAME").toLowerCase());
        }
        sortedTableList = sortTables(tables, meta);
        	
        return sortedTableList;
	}

	private ArrayList<String> getSortedImpactedTableList(String table, ArrayList<String> sortedTables, DatabaseMetaData meta) throws SQLException{
		ArrayList<String> impactedTables = new ArrayList<String>();
		impactedTables.add(table);
		ArrayList<String> dependentTables =  ForeignTableMap.getTablesHavingForeignTableAs(table);
		if(dependentTables == null)
			dependentTables = new ArrayList<String>();
		while(dependentTables.size() != 0){
			//impactedTables.addAll(dependentTables);
			for (String tab : dependentTables) {
				if(!impactedTables.contains(tab))
					impactedTables.add(tab);
			}
			ArrayList<String> newDependents  = new ArrayList<String>();
			for (String dt : dependentTables) {
				ArrayList<String> tables = ForeignTableMap.getTablesHavingForeignTableAs(dt);
				if(tables != null)
					newDependents.addAll(ForeignTableMap.getTablesHavingForeignTableAs(dt));
			}
			dependentTables = new ArrayList<String>();
			for (String dependent : newDependents) {
				if(!impactedTables.contains(dependent) && !dependentTables.contains(dependent)){
					dependentTables.add(dependent);
				}
			}
		}
		//sortTablesUsingMap(impactedTables);
		return impactedTables;
	}
	
	private ArrayList<String> sortTables(ArrayList<String> tables, DatabaseMetaData meta
			) throws SQLException {
		ArrayList<String> sorted = new ArrayList<String>(tables.size());
		for (String table : tables) {
			ArrayList<String> foreignTables = getForeignTables(meta, table);
			ForeignTableMap.updateMap(table, foreignTables);
			
			if(sorted.contains(table))
				continue;
			
			
			//if(foreignTables.size() == 0)
			for (int i=0; i< foreignTables.size(); i++) {
				
				String foreignTable = foreignTables.get(i);
				if(sorted.contains(foreignTable)) continue;
				ArrayList<String> fT = getForeignTables(meta, foreignTable);
				if(fT.size()== 0 ) sorted.add(foreignTable);
				for (String ft : fT) {
					if(!foreignTables.contains(ft))
						foreignTables.add(ft);
				}
			}
			for(int i= foreignTables.size()-1; i>=0; i--){
				String foreign = foreignTables.get(i);
				if(!sorted.contains(foreign))
					sorted.add(foreign);
			}
//			for (String foreign : foreignTables) {
//				if(!sorted.contains(foreign))
//					sorted.add(foreign);
//			}
			if(!sorted.contains(table))
				sorted.add(table);
		}
		return sorted;
	}
	
	/*Uses ForeignTableMap instead of querying db for meta data*/
	private ArrayList<String> sortTablesUsingMap(ArrayList<String> tables) throws SQLException {
		ArrayList<String> sorted = new ArrayList<String>(tables.size());
		for (String table : tables) {
			
			if(sorted.contains(table))
				continue;
			
			
			ArrayList<String> foreignTables = ForeignTableMap.getForeignTablesFor(table);
			if(foreignTables == null)
				foreignTables = new ArrayList<String>();
			//ForeignTableMap.updateMap(table, foreignTables);
			//if(foreignTables.size() == 0)
			for (int i=0; i< foreignTables.size(); i++) {
				
				String foreignTable = foreignTables.get(i);
				if(sorted.contains(foreignTable)) continue;
				 
				ArrayList<String> fT = ForeignTableMap.getForeignTablesFor(foreignTable);
				if(fT==null)
					fT = new ArrayList<String>();
				if((fT.size()== 0) && tables.contains(foreignTable)) 
					sorted.add(foreignTable);
				for (String ft : fT) {
					if(!foreignTables.contains(ft))
						foreignTables.add(ft);
				}
			}
			for(int i= foreignTables.size()-1; i>=0; i--){
				String foreign = foreignTables.get(i);
				if(!sorted.contains(foreign) && tables.contains(foreign))
					sorted.add(foreign);
			}
		//	for (String foreign : foreignTables) {
		//		if(!sorted.contains(foreign))
		//			sorted.add(foreign);
		//	}
			if(!sorted.contains(table) && tables.contains(table))
				sorted.add(table);
		}
		return sorted;
	}

	public static ArrayList<String> getForeignTables(DatabaseMetaData meta, String table) throws SQLException {
		ResultSet forKeys = meta.getImportedKeys(schema, null, table);//.toUpperCase());
		ArrayList<String> tables = new ArrayList<String>(1);
		while(forKeys.next()){
			String foreignTable = forKeys.getString("FKTABLE_NAME").toLowerCase();
			String pkTable = forKeys.getString("PKTABLE_NAME").toLowerCase();
			tables.add(pkTable);
		}
		return tables;
	}
	
	/**
	 * Returns a HashMap whose keys are the attributes the given table that are foreign keys
	 * and the values are the corresponding attributes in the foreign table (in the format tablename-columnname)
	 * */
	public static HashMap<String, String> getForeignKeysInTable(DatabaseMetaData meta, String table) throws SQLException {
		ResultSet forKeys = meta.getImportedKeys(schema, null, table);//.toUpperCase());
		HashMap<String, String> keyMap = new HashMap<String, String>(1);
		if(table.equals("CUPON_CONDITION_ITEM".toLowerCase())){
			@SuppressWarnings("unused")
			int debug =1;
		}
		while(forKeys.next()){
			String foreignKey = forKeys.getString("FKCOLUMN_NAME").toLowerCase();
			String primaryKey = forKeys.getString("PKCOLUMN_NAME").toLowerCase();
			String pkTable = forKeys.getString("PKTABLE_NAME").toLowerCase();
			short pos = forKeys.getShort("KEY_SEQ");
			keyMap.put(foreignKey, pkTable + "-" + primaryKey + "-" + pos);
		}
		return keyMap;
	}
	
	
	/*Gets the list of keys exported to other tables*/
	public static HashMap<String, HashSet<String>> getExportedKeys(DatabaseMetaData meta, String table) throws SQLException {
		ResultSet forKeys = meta.getExportedKeys(schema, null, table);//.toUpperCase());
		HashMap<String, HashSet<String>> keyMap = new HashMap<String, HashSet<String>>(1);
		
		while(forKeys.next()){
			String foreignKey = forKeys.getString("FKCOLUMN_NAME").toLowerCase();
			String primaryKey = forKeys.getString("PKCOLUMN_NAME").toLowerCase();
			String pkTable = forKeys.getString("PKTABLE_NAME").toLowerCase();
			String fkTable = forKeys.getString("FKTABLE_NAME").toLowerCase();
			short pos = forKeys.getShort("KEY_SEQ");
			
			if(keyMap.get(primaryKey) == null){
				HashSet<String> exportedKeys = new HashSet<String>();
				exportedKeys.add(fkTable + "-" + foreignKey + "-" + pos);
				keyMap.put(primaryKey, exportedKeys);
			}
			else{
				keyMap.get(primaryKey).add(fkTable + "-" + foreignKey + "-" + pos);
			}
			//keyMap.put(primaryKey, pkTable + "-" + primaryKey + "-" + pos);
		}
		return keyMap;
	}
	
	
	
	public void addQI(String table, String attribute){
		if(quasiIdentifiers.containsKey(table)){
			ArrayList<String> attrs = quasiIdentifiers.get(table);
			if(!attrs.contains(attribute))
				attrs.add(attribute);
		}
		else{
			ArrayList<String> attributes = new ArrayList<String>();
			attributes.add(attribute);
			quasiIdentifiers.put(table, attributes);
		}
	}
	
	
	
	public ResultSet getAllRecordsInTable(String table) throws SQLException{
		Statement stmt = dbConnection.createStatement();
		ResultSet rs =  stmt.executeQuery("Select * from "+ table);
		return rs;
	}
	
	public ResultSet getAllDistinctValues(String table, String columnName) throws SQLException{
		Statement stmt = dbConnection.createStatement();
		ResultSet rs =  stmt.executeQuery("Select distinct("+ columnName +") from "+ table);
		return rs;
	}
	
	public ResultSet getAllDistinctValues(String table, ArrayList<String> columnNames) throws SQLException{
		Statement stmt = dbConnection.createStatement();
		String columns = "";
		boolean isFirst = true;
		for (String string : columnNames) {
			if(isFirst){
				columns = string;
				isFirst = false;
			}
			else
				columns = columns + ", " + string;
		}
		ResultSet rs =  stmt.executeQuery("Select distinct "+ columns +" from "+ table);
		return rs;
	}
	
	
	/*Permutes the data in all the tables*/
	public synchronized void permuteDataInAllTables() throws SQLException{
		System.out.println("Start Anonymization.. with p = " + permutationProbability);
		//if(!isInvokedThroughCommandLine)
			//progressBar.setString("Starting Anonymization.. with p = " + permutationProbability);
		logger.info("Start Anonymization.. with p = " + permutationProbability);
		if(quasiIdentifiers.size() ==0 && getTotalQIs()==0){
			System.out.println("No QI Selected: Nothing to permute");
			return;
		}
		updateQIsUsingForeignConstraints();
		int numErrors =0;
		long start = System.currentTimeMillis();
		
		
		
		ArrayList<String> sortedTables = orderTablesForAnonymization();
		
		ArrayList<String> impactedTables = new ArrayList<String>();
		for (String qiTable : quasiIdentifiers.keySet()) {
			ArrayList<String> dependentTables = getSortedImpactedTableList(qiTable, sortedTables, dbConnection.getMetaData());
			for (String dt : dependentTables) {
				if(!impactedTables.contains(dt))
					impactedTables.add(dt);
			}
		}
		impactedTables = sortTablesUsingMap(impactedTables);
		
		
		
		/* use this piece of code for deleting and copying in all the tables
		 * 
		 * for (int i=sortedTables.size()-1; i>=0; i--) {
			String table = sortedTables.get(i);
				Statement stmt = permutedDBConnection.createStatement();
				try{
					stmt.execute("TRUNCATE TABLE " + table);
				}
				catch (MySQLIntegrityConstraintViolationException e) {
					int index = sortedTables.indexOf("inv_control_document_supplier");
					numErrors++;
					e.printStackTrace();
				}
		}*/
		if(!scoreOnly)
			for (int i=impactedTables.size()-1; i>=0; i--) {
				String table = impactedTables.get(i);
					Statement stmt = permutedDBConnection.createStatement();
					try{
						System.out.println("Deleting " + table);
						//if(!isInvokedThroughCommandLine)
							//progressBar.setString("Deleting " + table);
						if(permutedDatabaseConnectionString.contains("mysql"))
							stmt.execute("TRUNCATE TABLE " + table); //in mysql use truncate
						else{
							//stmt.execute("CREATE TABLE " + table); 
							try{
								stmt.execute("DELETE FROM " + table);
							}catch (Exception e) {
								e.printStackTrace();
							}
							//stmt.execute("CREATE TABLE " + table); 
						}
						
					}
					catch (MySQLIntegrityConstraintViolationException e) {
						numErrors++;
						e.printStackTrace();
					}
			}
		
		
		
		
		int numPermutedTable =0;
		/*
		 * use this piece of code for deleting and copying in all the tables
		 * 
		 * for (int i=0; i<sortedTables.size(); i++) {
			String table = sortedTables.get(i);
			if(!quasiIdentifiers.keySet().contains(table)){
				//copyTableToPermutedDB(table);
				permutedData.put(table, null);
				continue;
			}
			numPermutedTable++;
			permuteDataInTable(table);
			//System.out.println(getAnonimizationScore(dbConnection.createStatement(), table, table + "_PERMUTED"));	
		}*/
		for (int i=0; i<impactedTables.size(); i++) {
			String table = impactedTables.get(i);
			if(table.equals("service_item")){
				int debug =1;
			}
			if(!quasiIdentifiers.keySet().contains(table)){
				//copyTableToPermutedDB(table);
				//permutedData.put(table, null);
				//continue;
			}
			numPermutedTable++;
			if(table.equals("cupon")){
				@SuppressWarnings("unused")
				int debug =1;
			}
			//if(!isInvokedThroughCommandLine)
				//progressBar.setString("Permuting data in table " + table);
			permuteDataInTable(table);
			//System.out.println(getAnonimizationScore(dbConnection.createStatement(), table, table + "_PERMUTED"));	
		}
		
		
		
		cumulativeAnonymizationScore = cumulativeAnonymizationScore/totalRecords;
		cumulativeUniquenessScore = cumulativeUniquenessScore/totalRecords;
		long end = System.currentTimeMillis();
		System.out.println("Time to permute = " + (end-start)/60000 + " Minutes");
		
		logger.info("cumulativeUniquenessScore =  " + cumulativeUniquenessScore);
		logger.info("cumulativeAnonymizationScore =  " + cumulativeAnonymizationScore);
		logger.info("TotalUniqueRecds =  " + totalUniqueRecords);
		logger.info("numDiffLess =  " + numDiffLess);
		logger.info("TotalRecds =  " + totalRecords);
		logger.info("Time to permute = " + (end-start)/60000 + " Minutes");

		if(!isInvokedThroughCommandLine){
			displayAnonymizationResults(totalRecords, cumulativeAnonymizationScore, (end-start)/60000, totalUniqueRecords, cumulativeUniquenessScore);	
			setAnonymizationResults(totalRecords, cumulativeAnonymizationScore, (end-start)/60000, totalUniqueRecords);
			/*int result = showConfirmationDialog();
			if(result == 0){
				System.out.println("Writing Records");
				if(!scoreOnly)
					fillPermutedDataInAllTables(permutedData, impactedTables);
			}*/
			fillPermutedDataInAllTables(permutedData, impactedTables);
		}
		else if(!scoreOnly){
			System.out.println("Writing Records");
			fillPermutedDataInAllTables(permutedData, impactedTables);
		}
			
		//if(!isInvokedThroughCommandLine)
			//progressBar.setString("Finished Writing Records");
		System.out.println("Finished Writing Records");
			//fillPermutedDataInAllTables(permutedData, sortedTables);
	}

	private void copyTableToPermutedDB(String table) {
		// TODO Auto-generated method stub
		Statement stmt;
		try {
			stmt = dbConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
	                ResultSet.CONCUR_UPDATABLE);
			String query = "SELECT * FROM " + table;
			ResultSet rs = stmt.executeQuery(query);
			System.out.println("Reading table in memory" + table);
			//if(!isInvokedThroughCommandLine)
				//progressBar.setString("Reading table in memory" + table);
			long start = System.currentTimeMillis();
			MockResultSet mockRS = new MockResultSet(rs, table, new ArrayList<String>());
			long end = System.currentTimeMillis();
			System.out.println("Read " + mockRS.getNumRecords() + " in " + (end - start)/1000 + "sec");
			if(!writeRecordsToFile)
				mockRS.fillData(permutedDBConnection.createStatement(), outputSQLFile);
			else
				mockRS.writeDataToFile(writer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}




	public void permuteDataInTable(String table) throws SQLException{
		DatabaseMetaData metaData = dbConnection.getMetaData();
		ResultSet keys = metaData.getPrimaryKeys(schema, null, table);//.toUpperCase());
		ArrayList<String> primaryKeys =  new ArrayList<String>();
		
		while(keys.next()){
			String name = keys.getString("COLUMN_NAME").toLowerCase();
			primaryKeys.add(name);
		}
		if(table.equals("service_item".toLowerCase()) || table.equals("item".toLowerCase()) || table.contains("item_stock")){
			@SuppressWarnings("unused")
			int debug =1; 
		}
		
		HashMap<String, String>  foreignKeyMap = getForeignKeysInTable(metaData, table);
		
		
		//ResultSet res = metaData.getColumns(schema, null, table, null);
		ResultSet res = metaData.getColumns(schema, null, table/*.toUpperCase()*/, null);
		HashMap<String, ArrayList<Object>> column2DistinctRecordMap = new HashMap<String, ArrayList<Object>>(1);
		
		/*To handle more than one foreign key*/
		/*if(table.equals("ITEM_BULKS_SERVICE_FORMAT".toLowerCase())){
			@SuppressWarnings("unused")
			int debug =1;
		}*/
		
		if(table.equals("item")){
			int debug =1;
		}
		
		HashSet<HashSet<String>> foreignColumnGroups = new HashSet<HashSet<String>>();
		MockResultSet foreignData = null;
		HashMap<String, HashSet<String>> tableGroupMap = getGroupsFromForeignKeys(table, metaData);
		for (String key : tableGroupMap.keySet()) {
			foreignData = permutedData.get(key);
			if(foreignData == null){
				Statement stmt = dbConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
		                ResultSet.CONCUR_UPDATABLE);
				String query = "SELECT * FROM " + key;
				if(!isInvokedThroughCommandLine)
					progressBar.setString("Selecting records from table : " + key);
				ResultSet rs = stmt.executeQuery(query);
				System.out.println("Reading table in memory" + key);
				if(!isInvokedThroughCommandLine)
					progressBar.setString("Reading table in memory" + key);
				long start = System.currentTimeMillis();
				foreignData = new MockResultSet(rs, key, new ArrayList<String>());
			}
			foreignColumnGroups.add(tableGroupMap.get(key));
		}
		
		
        while (res.next()){
        	String column = res.getString("COLUMN_NAME").toLowerCase();
        	ArrayList<Object> list;
        	if(column.equals("item_id")){
        		@SuppressWarnings("unused")
				int debug =1;
        	}
        	if(foreignKeyMap.keySet().contains(column)){
        		String foreign =  foreignKeyMap.get(column);
        		String[] foreignTableColumn = foreign.split("-");
        		String foreignTable = foreignTableColumn[0];
        		String foreignColumn = foreignTableColumn[1];
        		if(table.equals("ITEM_BULKS_SERVICE_FORMAT".toLowerCase()) && column.equals("item_id")){
        			list = getRecordsInFirstColumn(getAllDistinctValues(foreignTable, foreignColumn));
        		}
        		else
	        		if(permutedData.get(foreignTable) == null)
	        			list = getRecordsInFirstColumn(getAllDistinctValues(foreignTable, foreignColumn));
	        		else
	        	    	list = getAllDistinctValues(permutedData.get(foreignTable), foreignColumn);
	        		
        	}
        	else
        		list = getRecordsInFirstColumn(getAllDistinctValues(table, column));
        	column2DistinctRecordMap.put(column, list);
        }
        permuteDataInTable(table, column2DistinctRecordMap, primaryKeys, foreignColumnGroups, foreignData, foreignKeyMap);
	}
	
	
	private ArrayList<Object> getAllDistinctValues(MockResultSet mockResultSet,
			String foreignColumn) {
		// TODO Auto-generated method stub
		HashSet<Object> distinctRcds = new HashSet<Object>();
		mockResultSet.reset();
		while(mockResultSet.next()){
			distinctRcds.add(mockResultSet.getObject(foreignColumn));
		}
		return new ArrayList<Object>(distinctRcds);
	}

	public String getForeignName(String column, String table,  HashMap<String, String>  foreignKeyMap){
			String foreign =  foreignKeyMap.get(column);
    		String[] foreignTableColumn = foreign.split("-");
    		String foreignTable = foreignTableColumn[0];
    		String foreignColumn = foreignTableColumn[1]; 	
    		return foreignColumn;
	}
	
	private HashMap<String, HashSet<String>> getGroupsFromForeignKeys(
			String tableName, DatabaseMetaData meta) throws SQLException {
		// TODO Auto-generated method stub
		ResultSet forKeys = meta.getImportedKeys(schema, null, tableName);//.toUpperCase());
		HashMap<String, String> foreignKeyMap = new HashMap<String, String>(1);
		ArrayList<String> orderedKeys = new ArrayList<String>();
		if(tableName.equals("CUPON_CONDITION_ITEM".toLowerCase())){
			@SuppressWarnings("unused")
			int debug =1;
		}
		while(forKeys.next()){
			String foreignKey = forKeys.getString("FKCOLUMN_NAME").toLowerCase();
			String primaryKey = forKeys.getString("PKCOLUMN_NAME").toLowerCase();
			String pkTable = forKeys.getString("PKTABLE_NAME").toLowerCase();
			short pos = forKeys.getShort("KEY_SEQ");
			foreignKeyMap.put(foreignKey, pkTable + "-" + primaryKey + "-" + pos);
			orderedKeys.add(foreignKey);
		}
		
		
		
		
		HashMap<String, HashSet<String>> groups = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> tableForeignColumnsAttr = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> GroupNo2Col = new HashMap<String, HashSet<String>>();
		HashSet<String> groupNumbers = new HashSet<String>();
		Integer groupN = 0;
		for (String column : orderedKeys) {
			String foreign =  foreignKeyMap.get(column);
    		String[] foreignTableColumn = foreign.split("-");
    		String foreignTable = foreignTableColumn[0];
    		@SuppressWarnings("unused")
			String foreignColumn = foreignTableColumn[1]; 
    		String groupNumber = foreignTableColumn[2];
    		if(Integer.parseInt(groupNumber) ==1)
    			++groupN;
    		if(groupN ==0){
    			@SuppressWarnings("unused")
				int debug =1;
    		}
    		groupNumbers.add(groupN.toString());
    		
    		if(GroupNo2Col.get(groupN.toString()) == null){
    			HashSet<String> set = new HashSet<String>();
    			set.add(column);
    			GroupNo2Col.put(groupN.toString(), set);
    		}
    		else
    			GroupNo2Col.get(groupN.toString()).add(column);
    		
    		
    		
    		
    		if(tableForeignColumnsAttr.containsKey(foreignTable)){
    			tableForeignColumnsAttr.get(foreignTable).add(column);
    		}
    		else{
    			HashSet<String> columns = new HashSet<String>();
    			columns.add(column);
    			tableForeignColumnsAttr.put(foreignTable, columns);
    		}	
		}
		for (String table : tableForeignColumnsAttr.keySet()) {
			
			HashSet<String> columns = tableForeignColumnsAttr.get(table);
			if(columns.size() <= 1)
				continue;
			for (String num : groupNumbers) {
				HashSet<String> inColumn = new HashSet<String>();
				for (String col : columns) {
					if(GroupNo2Col.get(num).contains(col))
						inColumn.add(col);
				}
				if(inColumn.size() > 1){
					if(groups.get(table)!= null){
						@SuppressWarnings("unused")
						int debug =1;
					}
					groups.put(table, columns);
				}
			}
			
			
			
		}
		return groups;
	}

	/**
		Permutes the data in the specified table...Only the values in column in the
		keyset of column2DistinctRecordMap are permuted other columns are kept as it is
		the distinct records in the table are 
		provided
	 * @param foreignData 
	 * @throws SQLException 
	*/
	private void permuteDataInTable(String table, HashMap<String, ArrayList<Object>> column2DistinctRecordMap, ArrayList<String> primaryKeys, 
						HashSet<HashSet<String>> columnGroups, MockResultSet foreignData, HashMap<String, String>  foreignKeyMap) throws SQLException{
		
		//if(!isInvokedThroughCommandLine)
			//progressBar.setString("Starting Permutation for table: " + table);

		ArrayList<String> qi = quasiIdentifiers.get(table);
		/*if(quasiIdentifiers.get(table).size() == 0)
			return;*/
		ArrayList<ArrayList<String>> uniqueKeys = new ArrayList<ArrayList<String>>();
			if(uniqueKeyMap != null) 
				uniqueKeys = uniqueKeyMap.get(table);
		if(uniqueKeys == null)
			uniqueKeys = new ArrayList<ArrayList<String>>();
		
		primaryKeys = resolveKeyConflicts(primaryKeys, columnGroups, uniqueKeys);	
		
		ArrayList<String> columnsToPermute = quasiIdentifiers.get(table);
		if(columnsToPermute==null)
			columnsToPermute = new ArrayList<String>();
		Statement stmt = dbConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
		//if(!isInvokedThroughCommandLine)
			//progressBar.setString("Reading table from DB: " + table);
		ResultSet resultSet = stmt.executeQuery("SELECT * FROM " + table);
		long start = System.currentTimeMillis();
		System.out.println("Reading Table in Memory: " + table);
		//if(!isInvokedThroughCommandLine)
			//progressBar.setString("Reading table in Memory: " + table);
		MockResultSet rs = new MockResultSet(resultSet, table, columnsToPermute);
		//if(!isInvokedThroughCommandLine)
			//progressBar.setString("Reading table in memory" + table);
		System.out.println("Cloning: " + table);
		//if(!isInvokedThroughCommandLine)
			//progressBar.setString("Cloning: " + table);
		MockResultSet original = rs.cloneRS();
		System.out.println("Finished Cloning: " + table);
		long end = System.currentTimeMillis();
		System.out.println("Time to read records and clone : " + (start-end)/60000 + " min");
		
		int i=0, percentage = 0;
		double N = rs.getNumRecords();
		totalRecords = N+ totalRecords;
		Set<String> columns = column2DistinctRecordMap.keySet();
		CombinationGenerator combination = null;
		ArrayList<CombinationGenerator> uniqueKeyCombs = new ArrayList<CombinationGenerator>();
		for (ArrayList<String> key : uniqueKeys) {
			uniqueKeyCombs.add(new CombinationGenerator(column2DistinctRecordMap, key, permutationProbability));
		}
		if(primaryKeys.size() > 1)
			combination = new CombinationGenerator(column2DistinctRecordMap, primaryKeys, permutationProbability);
		int numUnchangedRecords =0;
		
		ArrayList<Integer> indices = new ArrayList<Integer>();
		
		if(columnGroups.size() == 1){
			/*Used for permuting data in groups of foreign keys*/
			if(foreignData == null) columnGroups.clear();
			else{
				indices = new ArrayList<Integer>();
				foreignData.reset(); 
				int numForeignRecds = (int) foreignData.getNumRecords();
				for(int ind =0; ind< numForeignRecds; ind++){
					indices.add(ind);
				}
			}
		}
		else if(columnGroups.size() > 1){
			@SuppressWarnings("unused")
			int debug =1;
		}
		
		
		/*removing columns constrained by foreign keys from primary keys : otherwise constraints may fail
		 * 
		 * May still result in error but its a quick fix for now....
		 * */
		
		boolean pkRelated = removeColumnGroupsFromPrimaryKeys(columnGroups, primaryKeys);
		if(pkRelated){
			
			primaryKeys = new ArrayList<String>();
		}
		
		/*break if constraint may violate from adding data*/
		int breakAt = (int)rs.getNumRecords();
		while(rs.next()){
			if(i>=breakAt){
				rs.removeRecordsAfterThis();
				break;
			}
			i++;
			int p = (int) ((i*100)/N);
			if(p >  percentage){
				percentage = p;
				//System.out.println("Permuting " + table + " " + percentage + "% Finished out of " + N);
			}
			
			boolean isRecordDifferent = false;
			int numDiff =0;
			double randomNumber = random.nextDouble();
			for (String column : columns) {
				if(!columnsToPermute.contains(column)) 
					continue;
				if(primaryKeys.size() > 1 && primaryKeys.contains(column))
					continue;
				if(uniqueKeyContainsColumn(column, uniqueKeys))
					continue;
				if(!quasiIdentifiers.get(table).contains(column))
					continue;
				if(columnGroupsContainsColumn(column, columnGroups))
					continue;
					

				
				boolean groupFlag= false;
				for (HashSet<String> group : columnGroups) {
					if(group.contains(column))
						groupFlag = true;
				}
				if(groupFlag)
					continue;
				
				Object oldValue = rs.getObject(column);
				Object newValue;
				
				
				if(randomNumber <= permutationProbability || 
						(primaryKeys.contains(column) && !column2DistinctRecordMap.get(column).contains(oldValue))){
					//change the record
					int num = column2DistinctRecordMap.get(column).size();
					if(num == N)
						newValue = getRandomDistinctValue(column2DistinctRecordMap.get(column), true);
					else{
						newValue = getRandomDistinctValue(column2DistinctRecordMap.get(column), primaryKeys.contains(column));
					}
					//newValue = getRandomDistinctValue(column2DistinctRecordMap.get(column), true);
					
					if(newValue == null && !isRecordDifferent)
						isRecordDifferent = oldValue != null;
					if(newValue == null && oldValue!= null)
						numDiff++;
					else if(newValue != null && !newValue.equals(oldValue)){
						numDiff++;
						isRecordDifferent = true;
					}
					rs.setObjectAt(column, newValue);
					
				}
				else{ //do not change
					newValue = oldValue;
					if(primaryKeys.contains(column)){
						column2DistinctRecordMap.get(column).remove(newValue);
					}
					rs.setObjectAt(column, newValue);
				}
			}
			if(primaryKeys.size() >1){
				boolean permute = false;
				for (String string : primaryKeys) {
					if(columnsToPermute.contains(string))
						permute = true;
				}
				if(permute){
					/*if the primary key consists of more than one column...we generate unique combinations and choose from it*/
					ArrayList<Object> combo =  combination.getNext(false);
					System.out.println();
					for (String column : primaryKeys) {
						int index = primaryKeys.indexOf(column);
						Object obj = combo.get(index);
						Object oldValue = rs.getObject(column);
						if(!obj.equals(oldValue)){
							numDiff++;
							isRecordDifferent = true;
						}
						rs.setObjectAt(column, obj);
					}
				}
			}
			
			for (HashSet<String> group : columnGroups) {
				int n=0;
				int index =-1;
				boolean permute = false;
				for (String string : group) {
					if(columnsToPermute.contains(string))
						permute = true;
				}
				if(!permute)
					break;
				
				for (String column : group) {
					if(n++ == 0 ){
						if(indices.size()==0){
							@SuppressWarnings("unused")
							int debug =1;
						}
						int rand = random.nextInt(indices.size());
						if(pkRelated && !(rs.getNumRecords() > foreignData.getNumRecords())){
							index = indices.remove(rand);
						}
						else
							index = rand;
						if(pkRelated && rs.getNumRecords() > foreignData.getNumRecords() && i==1){
							breakAt = (int)foreignData.getNumRecords();
							try {
								throw new Exception("Change smething");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					String foreignName = getForeignName(column, table, foreignKeyMap);
					Object newVal = foreignData.getColumnAtIndex(index, foreignName);
					Object oldValue = rs.getObject(column);
					if(!newVal.equals(oldValue)){
						isRecordDifferent = true;
						numDiff++;
					}
					rs.setObjectAt(column, newVal);
				}
			}
			
			
			if(uniqueKeys != null){
				for (ArrayList<String> uniqueKey: uniqueKeys) {
					boolean permute = false;
					for (String string : uniqueKey) {
						if(columnsToPermute.contains(string))
							permute = true;
					}
					if(!permute)
						break;
					CombinationGenerator comboGen  =null;
						ArrayList<Object> combo = null;
						if(uniqueKey.size() >1){
							comboGen = uniqueKeyCombs.get(uniqueKeys.indexOf(uniqueKey));
							combo = comboGen.getNext(false);
						}
						for (String column : uniqueKey) {
							if(uniqueKey.size() >1){
								int index = uniqueKey.indexOf(column);
								Object newValue = combo.get(index);
								Object oldValue = rs.getObject(column);
								if(!newValue.equals(oldValue))
									isRecordDifferent = true;
								rs.setObjectAt(column, newValue);
							}
							else{
								Object oldValue = rs.getObject(column);
								Object newValue = getRandomDistinctValue(column2DistinctRecordMap.get(column), true);
								if(!newValue.equals(oldValue)){
									numDiff++;
									isRecordDifferent = true;
								}
								rs.setObjectAt(column, newValue);
							}
						}
				}
				rs.addNumDifferingColumnsFromOriginal(numDiff);
				if(!isRecordDifferent)
					numUnchangedRecords++;
			}
		}
		
		
		rs.reset();
		System.out.println("Calculating Score for " + table);
		//if(!isInvokedThroughCommandLine)
			//progressBar.setString("Calculating Score for " + table);
		double score = getAnonimizationScoreEfficiently(original, rs, columnsToPermute);
		unchangedScore = 1-numUnchangedRecords/rs.getNumRecords();
		cumulativeUniquenessScore = unchangedScore*N + cumulativeUniquenessScore;
		cumulativeAnonymizationScore = score*N + cumulativeAnonymizationScore;
		anonymizationScores.put(table, score);
		System.out.println("New Anonymization score for " + table + " = " + score + " unchangedScore = " + unchangedScore);
		logger.info("Unchanged score for " + table + " = " + unchangedScore);
		if(!scoreOnly)
			permutedData.put(table, rs);
	}

	private ArrayList<String> resolveKeyConflicts(
			ArrayList<String> primaryKeys,
			HashSet<HashSet<String>> columnGroups,
			ArrayList<ArrayList<String>> uniqueKeys) {
		ArrayList<ArrayList<String>> keysToRemove = new ArrayList<ArrayList<String>>();	
		/*Resolution oc conflicts between unique and primary keys*/	
		if(uniqueKeys != null)
		for (ArrayList<String> key : uniqueKeys) {
			for (String primary : primaryKeys) {
				if(key.contains(primary)){
					if(primaryKeys.containsAll(key)){
						primaryKeys = new ArrayList<String>();
					}
					else if(key.containsAll(primaryKeys)){
						keysToRemove.add(key);
					}
					else{
						//complex situation
						keysToRemove.add(key);
						try {
							throw new Exception("Complex Situation");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					break;
				}
			}
		}
		for (ArrayList<String> key : keysToRemove) {
			uniqueKeys.remove(key);
		}
		keysToRemove = new ArrayList<ArrayList<String>>();	
		
		if(uniqueKeys != null)
		for (ArrayList<String> key : uniqueKeys) {
			for (HashSet<String> foreignGroup : columnGroups) {
				for (String fColumn : foreignGroup) {
					if(key.contains(fColumn)){
						if(foreignGroup.containsAll(key)){
							keysToRemove.add(key);
						}
						else{
							//complex situation
							keysToRemove.add(key);
							try {
								throw new Exception("Complex Situation");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	
						}
						break;
					}
				}
			}
		}
		for (ArrayList<String> key : keysToRemove) {
			uniqueKeys.remove(key);
		}
		keysToRemove = new ArrayList<ArrayList<String>>();
		return primaryKeys;
	}
	
	static HashSet<Object> vals = new HashSet<Object>();
	private boolean removeColumnGroupsFromPrimaryKeys(
			HashSet<HashSet<String>> columnGroups, ArrayList<String> primaryKeys) {
		for (HashSet<String> fkGroup : columnGroups) {
			for (String column : fkGroup) {
				if(primaryKeys.contains(column)){
					primaryKeys = new ArrayList<String>();
					return true;
				}
			}
		}
		return false;
	}
	
	

	
	private boolean uniqueKeyContainsColumn(String column, ArrayList<ArrayList<String>> uniqueKeys) {
		// TODO Auto-generated method stub
		if(uniqueKeys == null)
			return false;
		for (ArrayList<String> key : uniqueKeys) {
			if(key.contains(column))
				return true;
		}
		return false;
	}
	//columnGroupsContainsColumn
	private boolean columnGroupsContainsColumn(String column, HashSet<HashSet<String>> groups) {
		// TODO Auto-generated method stub
		if(groups == null)
			return false;
		for (HashSet<String> key : groups) {
			if(key.contains(column))
				return true;
		}
		return false;
	}
	
	
	public void fillPermutedDataInAllTables(HashMap<String, MockResultSet> records, ArrayList<String> sortedTables) throws SQLException{
		for (String table : sortedTables) {
			MockResultSet mockRS = records.get(table);
			if(mockRS == null)
				copyTableToPermutedDB(table);
			else{
				if(!writeRecordsToFile)
					mockRS.fillData(permutedDBConnection.createStatement(), outputSQLFile);
				else
					try {
						mockRS.writeDataToFile(writer);
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		
	}


	/**
		Returns the sql type of a specified column in a specified table
	*/
	public static int getTypeofColumn(String table, String column) throws SQLException{
 	   DatabaseMetaData dbMetaData = dbConnection.getMetaData();
 	   ResultSet res = dbMetaData.getColumns(schema, null, table/*.toUpperCase()*/, null);
       //System.out.println("List of tables: ");
       int type = -1;
       while (res.next()){
            if(res.getString("COLUMN_NAME").toLowerCase().equals(column)){
            	type = res.getInt("DATA_TYPE");
            }
       }
	   return type;
	}

	/*Gets a random value from the recordList */
	private Object getRandomDistinctValue(ArrayList<Object> recordList, boolean isPrimary){
		// TODO Auto-generated method stub
		if(recordList.size() ==0){
			@SuppressWarnings("unused")
			int debug =0;
		}	
		
		int randomNumber = random.nextInt(recordList.size());
		Object obj = recordList.get(randomNumber);
		if(isPrimary)
			recordList.remove(randomNumber);
		return obj;
	}

	/*Gets all the records in the recordSet */
	private ArrayList<Object> getRecordsInFirstColumn(ResultSet rs) throws SQLException {
		ArrayList<Object> recordList =  new ArrayList<Object>();
		while(rs.next()){
			recordList.add(rs.getObject(1));
		}
		return recordList;
	}
	
	
	private ArrayList<ArrayList<Object>> getRecords(ResultSet rs) throws SQLException {
		ArrayList<ArrayList<Object>> recordList =  new ArrayList<ArrayList<Object>>();
		
		while(rs.next()){
			ArrayList<Object> currentRecord = new ArrayList<Object>();
			for(int i=1; i<= rs.getMetaData().getColumnCount(); i++){
				currentRecord.add(rs.getObject(i));
			}
			recordList.add(currentRecord);
		}
		return recordList;
	}
	
	/*Anonimization Score ...complexity O(n2)..takes 9 hours to find a score of a table
	 * with 250,000 records 
	 * In process of implementing an efficient solution*/
	@Deprecated
	public double getAnonimizationScore(MockResultSet orignial, MockResultSet permuted) throws SQLException{
		
		double totalScore = 0.0;
		double n = permuted.getNumRecords();
		long start = System.currentTimeMillis();
		int cc = orignial.getColumnNames().size();
		while(orignial.next()){
			double recordScore = 0.0;
			while (permuted.next()) {
				for (int i = 1; i <= cc; i++) {
					String s1 = orignial.getObject(i).toString();
					String s2 = permuted.getObject(i).toString();
					if(!s1.equals(s2))
						recordScore = recordScore + 1; 
				}
			}
			System.out.println("time to calculate score : " + (System.currentTimeMillis() - start)/1000 + "sec");
			permuted.reset();
			recordScore = recordScore/cc;
			totalScore = recordScore/n + totalScore;
		}
	
		long end = System.currentTimeMillis();
		System.out.println("time to calculate score : " + (end - start)/1000 + "sec");
		return totalScore/n;
	}
	
	int numDiffLess =0;
	/*Efficient implementation for calculation of privacy scores
	 * sorts each column in the table and searches, using a modified binary search, 
	 * for the number of matches in each column and computes scores accordingly */
	public double getAnonimizationScoreEfficiently(MockResultSet orignial, MockResultSet permuted, ArrayList<String> permutedColumns){
		double totalScore = 0.0;
		
		double N = permuted.getNumRecords();
		long start = System.currentTimeMillis();
		//int cc = orignial.columnNames.size();
		int permutedColumnCount = permutedColumns.size();
		ElementComperator comp = new ElementComperator();
		//for (ArrayList<Object> records : orignial.records)
		
		//ArrayList<ArrayList<Object>> sortedRecds = new ArrayList<ArrayList<Object>>();
		ArrayList<ArrayList<Object>> sortedRecds = new ArrayList<ArrayList<Object>>();
		
		for(String column : permutedColumns){
			ArrayList<Object> recordsForColumn = orignial.getRecordsInColumnWithIndices(orignial.getIndexofColumn(column));
			Collections.sort(recordsForColumn, comp);
			sortedRecds.add(recordsForColumn);
		}
		
		int numUniqueRecds =0;
		
		int percentage =0;
		//if(!isInvokedThroughCommandLine){
		//	progressBar.setIndeterminate(false);
		//	progressBar.setMaximum(100);
		//}
		while (permuted.next()) {
			ArrayList<Object> rec = permuted.selectColumnsOfCurrentRecord(permutedColumns);
			
			//int n = permuted.retrieveNumberofRecords(rec, permutedColumns);
			
			
			int newpercentage = 0;
			newpercentage = (int) (permuted.currentPosition*100/(permuted.getNumRecords()));
			if(newpercentage > percentage){
				//if(!isInvokedThroughCommandLine)
					//progressBar.setValue(newpercentage);
				percentage = newpercentage;
				System.out.println("Percentage :  " + percentage);
			}
			
			/**/
			int n = permuted.retrieveNumberofMatchingRecords(permuted.getCurrentRecord());
			int unchangedRecords =0;
			if(n >= 1){
				numUniqueRecds++;
			}
			if(uniqueScoreOnly)
				continue;
			
			double recordScoreOld = 0.0;//Old record score that sees the distance between a permuted rcd and all the records in the database
			double recordScore = 0.0; //measuring the difficulty of am attacker in attacking a permuted record
			
			//indices of the matching records, i.e., records that have the same value in a particular column
			ArrayList<ArrayList<Integer>> matchingIndicesArray = new ArrayList<ArrayList<Integer>>(permutedColumnCount); 
			for (int i = 1; i <= permutedColumnCount; i++) {
				Object key = rec.get(i-1);
				ArrayList<Object> records = sortedRecds.get(i-1);
				ArrayList<Integer> matchingIndices = new ArrayList<Integer>();
				int num = comp.getNumberElementsMatchingKey(records, key, matchingIndices);
				matchingIndicesArray.add(matchingIndices);
				if(num<-1){
					try {
						throw new Exception("SomethingWrong");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(num<0){
					num =0;
				}
				recordScoreOld = recordScoreOld + 1 - num/N;
			}
			int numDiff = permuted.getNumDifferingColumnsFromOriginal().get(permuted.currentPosition);
			//int numRecordsWithLowerScore = getNumRecordsWithLowerScore(matchingIndicesArray, numDiff);
			int threshold = 100;
			int numRecordsWithLowerScore = getNumRecordsWithLowerScoreUsingBucketSort(matchingIndicesArray, numDiff, permuted.getNumRecords(), threshold);
			recordScore = numRecordsWithLowerScore/100;
			
			if(numRecordsWithLowerScore <= 5){
				numDiffLess++;
			}
 			recordScoreOld = recordScoreOld/permutedColumnCount;
			totalScore = totalScore + recordScore;
		}
		//if(!isInvokedThroughCommandLine)
			//progressBar.setIndeterminate(true);
		long end = System.currentTimeMillis();
		System.out.println("time to calculate score : " + (end - start)/1000 + "sec");
		
		totalUniqueRecords = totalUniqueRecords + numUniqueRecds;
		System.out.println("New Score = " + totalScore/N + " numDiffLess = " + numDiffLess);
		logger.info("prob = " + permutationProbability +  " table = " + permuted.tableName +  "Total Recs = " + N + " uniqueRecs = " + numUniqueRecds + "New Score " + totalScore/N);
		return totalScore/N;
	}
	
	
	/*Gets the number of records having a lower score than current record under processing
	 * matchingIndicesArray contains the list of matched array indices in the original database for each permuted column
	 * 
	 * */
	private int getNumRecordsWithLowerScore(
			ArrayList<ArrayList<Integer>> matchingIndicesArray, int nDiffMatching) {
		// TODO Auto-generated method stub
		int numRecordsWithLowerScore = 0;
		
		for (ArrayList<Integer> arrayList : matchingIndicesArray) {
			Collections.sort(arrayList, new ElementComperator());
		}
		for (ArrayList<Integer> arrayList : matchingIndicesArray) {
			for (Integer integer : arrayList) {
				int matches = 1;
				for (ArrayList<Integer> aList2 : matchingIndicesArray) {
					if(aList2.equals(arrayList)) break;
					int search = Collections.binarySearch(aList2, integer, new ElementComperator());
					if(search >= 0)
						matches++;
				}
				int diffColmns = matchingIndicesArray.size() - matches;
				if(diffColmns <= nDiffMatching){
					numRecordsWithLowerScore++;
					if(numRecordsWithLowerScore == 10)
						return numRecordsWithLowerScore;
				}
			}
		}
		
		return numRecordsWithLowerScore;
	}
	
	
	private int getNumRecordsWithLowerScoreUsingBucketSort(
			ArrayList<ArrayList<Integer>> matchingIndicesArray, int nDiffMatching, double bucketSize, int threshold) {
		// TODO Auto-generated method stub
		
		int[] bucket = new int[(int) bucketSize];//contains number of matching records 
		HashSet<Integer> recdsWithLowerScore = new HashSet<Integer>();
		
		/*need to find records that have equal or more matching columns than originalMatches*/
		int originalMatches = matchingIndicesArray.size() - nDiffMatching;
		
		for (ArrayList<Integer> arrayList : matchingIndicesArray) {
			for (Integer integer : arrayList) {
				++bucket[integer];
				if(bucket[integer] >= originalMatches)
					recdsWithLowerScore.add(integer);
				if(recdsWithLowerScore.size() >= threshold)
					return threshold;
			}
		}
		return recdsWithLowerScore.size();
	}
	
	

	
	
	public int getTotalQIs(){
		int n=0;
		for (String table  : quasiIdentifiers.keySet()) {
			n= n+ quasiIdentifiers.get(table).size();
		}
		return n;
	}

	public void displayAnonymizationResults(double totalRecords, double anonymizationScore, double timeTaken, double numberOfUniqueRecords, double uniquenessScore){
		//TaDaFinalView.displayAnonymizationResults(totalRecords, anonymizationScore, timeTaken, numberOfUniqueRecords, uniquenessScore);
	}

	public static double totalRecordsRes, anonymizationScoresRes, timeTakenRes, numberOfUniqueRecordsRes;
	
	public void setAnonymizationResults(double totalRecords, double anonymizationScore, double timeTaken, double numberOfUniqueRecords){
		this.totalRecordsRes = totalRecords;
		this.anonymizationScoresRes = anonymizationScore;
		this.timeTakenRes = timeTaken;
		this.numberOfUniqueRecordsRes = numberOfUniqueRecords;
	}

	public static double getTotalRecordsRes()
	{
		return totalRecordsRes;
	}
	
	public static double getAnonymizationScores()
	{
		return anonymizationScoresRes;
	}
	
	public static double getTimeTaken()
	{
		return timeTakenRes;
	}

	public static double getNumberOfUniqueRecords()
	{
		return numberOfUniqueRecordsRes;
	}
	
	@Override
	public synchronized  void run() {
		// TODO Auto-generated method stub
		try {
			permuteDataInAllTables();
			this.notify();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	 public static int showConfirmationDialog(){
		  Object[] possibleValues = { "First", "Second", "Third" };
		  //jdbc:derby://localhost:1527/riskit;create=true;user=se549;password=se549
//		  String selectedValue = 
//			  JOptionPane.showInputDialog(null, "Enter Command " +
//			  		" to connect to a database. \n (For example jdbc:derby://localhost:1527/UnixUser;create=true;user=me;password=mine) : ", 
//			  		"jdbc:derby://localhost:1527/UnixUser;create=true;user=me;password=mine");
		  int result = 
			  JOptionPane.showConfirmDialog(null, "Do you want to fill permuted data in database?", "Fill Records?", JOptionPane.YES_NO_OPTION);
		  return result;	  
		  
	  }

	public void setUniqueRecords(double totalUniqueRecords) {
		this.totalUniqueRecords = totalUniqueRecords;
	}

	public double getUniqueRecords() {
		return totalUniqueRecords;
	}

	public void setTotalRecords(double totalRecords) {
		this.totalRecords = totalRecords;
	}

	public double getTotalRecords() {
		return totalRecords;
	}
	
	public double getCumulativeUniquenessScore() {
		return cumulativeUniquenessScore;
	}

	
	
}
