package anonymization.scripts;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.sun.java.browser.net.ProxyServiceProvider;

import database.anonymization.DatabaseAnonymizer;

public class AnonimizationDriver {
	
	
	public static void main(String[] args) {
		
		//String dbConnectionString = "jdbc:derby://localhost:1527/riskit;create=true;user=se549;password=se549";
		
		//String dbConnectionString1 = "jdbc:derby://localhost:1527/UnixUser;create=true;user=me;password=mine";
		//String dbConnectionString2 = "jdbc:derby://localhost:1527/ipums;create=true;";
		String dbConnectionString = "jdbc:derby://localhost:1527/riskit;create=true;user=se549;password=se549";
		
		connection = getConnection(dbConnectionString);
		boolean insertRecordsInDB = false;
		//0 = p=1
		//1 = p=0.8
		//....
		double[] runForProbabilities = {1.0, 0.8, 0.6, 0.4, 0.2, 0.1, 0.01 };
		String schema = "se549";
		boolean commandLine = true;
		
		for(int i=0; i< runForProbabilities.length-1; i++){
			String permutedDatabaseConnectionString = 
				"jdbc:derby://localhost:1527/riskitPermuted" + i + ";create=true;user=se549;password=se549";
			String outputSQLFIle = "C:/riskitPermuted" + i + ".sql";
			DatabaseAnonymizer anonymizer = new DatabaseAnonymizer(connection, runForProbabilities[i], 
					permutedDatabaseConnectionString, outputSQLFIle, true, true, true);
			try {
				anonymizer.permuteDataInAllTables();
				double u = anonymizer.getUniqueRecords();
				double N = anonymizer.getTotalRecords();
				double score = anonymizer.getCumulativeUniquenessScore();
				System.out.println(runForProbabilities[i] + " " + u + " " + N + " " + score);
				writeToFile(runForProbabilities[i], u, N, score);
				System.out.println("Finished writing for p = " + runForProbabilities[i]);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	private static void writeToFile(double p, double u,
			double n, double score) {
		// TODO Auto-generated method stub
		try {
			FileWriter fw = new FileWriter("c:\\riskitResults.txt", true);
			BufferedWriter writer = new BufferedWriter(fw);
			writer.write("Probability = " + p + "\n");
			writer.write("Unique Records = " + u + "\n");
			writer.write("Total Records = " + n + "\n");
			writer.write("Score = " + score + "\n");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Connection connection = null;
	public static java.sql.Connection getConnection(String connectionCommand){
		System.out.println("Connecting");
		if(connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try { 
			connection = DriverManager.getConnection(connectionCommand);
	        Statement stmt = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("Connected");
        return connection;
	}
	
}
