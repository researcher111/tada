package database.anonymization;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * Represents the database after permutation of records
 * The database is written back to the actual database after permutation.
 * Hopefully wont result in out of memory exception
 * @author Kunal Taneja
 * */
public class MockDatabase {
	/*A map of table and records in it*/
	private HashMap<String, MockResultSet> tables;
	private static Connection dbConnection;
	
	/**
	 * */
	
	public MockDatabase(){
		tables = new HashMap<String, MockResultSet>(1);
		dbConnection =  DatabaseAnonymizer.dbConnection;
	}
	/*Creates a database with a specified name and copies the mock database to the 
	 * created database
	 * */
	private void writeToDatabase(String dbName) throws SQLException{
		
		Statement stmt;
		String query = "CREATE TABLE ";
		stmt = dbConnection.createStatement();
		for (String table : tables.keySet()) {
			MockResultSet records = tables.get(table);
			records.fillData(stmt, "c:\\db.sql");
		}
	}
	
	public void addTable(String table, MockResultSet rs){
		tables.put(table, rs);
	}

	public HashMap<String, MockResultSet> getTableMap() {
		return tables;
	}
	
	
	/*public static Connection getConnection(String dbName){

        if (permutedDBConnection != null){
            return permutedDBConnection;
        }
        // load driver
        String driver = "org.apache.derby.jdbc.ClientDriver";

        // define the Derby connection URL to use
        String connectionURL = "jdbc:derby://localhost:1527/" + dbName + ";create=true";

        try{
            Class.forName(driver);}
        catch(java.lang.ClassNotFoundException e){
            System.err.println("Load Driver Failed: "+ e.toString());
        }
        try{
            permutedDBConnection = DriverManager.getConnection(connectionURL);
        }
        catch (SQLException e){
            System.err.println("Get Connection Failed: " + e.toString());
        }
        return permutedDBConnection;
    }*/
}
