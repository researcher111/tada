package tadagui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import database.anonymization.DatabaseAnonymizer;

import tadadriver.CoverageDatabaseAttributeMapping;
import tadadriver.TaDaMain;

/* This class runs the main functions of the GUI from the command line */

public class TaDaCmdline
{
	public static ArrayList<String> arQI = new ArrayList<String>();
	public static Connection dbConnection;
	
	public static void main(String args[]) throws Exception
	{
		long startTime = System.nanoTime();
		
        String javalibloc = "/usr/lib/jvm/java-6-openjdk-amd64/jre/lib";
        String javacommonloc = "/usr/lib/jvm/java-6-openjdk-common/jre/lib";

    	String runcmd = "--interactive-mode --d \"/home/cmc/projects/priest/workspace/sootOutput/\" " +
			"--cp \"" +
			javacommonloc+"/jce.jar:" +
			javacommonloc+"/charsets.jar:" +
			javacommonloc+"/ext/dnsns.jar:" +
			javacommonloc+"/jsse.jar:" +
			javacommonloc+"/ext/sunpkcs11.jar:" +
			javacommonloc+"/resources.jar:" +
			"/home/cmc/projects/priest/workspace/Sample/bin:" +
			javacommonloc+"/ext/localedata.jar:" +
			javalibloc+"/rt.jar:" +
			javacommonloc+"/ext/sunmscapi.jar:" + // not found on nanook, doesn't seem to matter
			javacommonloc+"/ext/sunjce_provider.jar:" +
			"/home/cmc/projects/priest/workspace/Sample/src:" +
			"/home/cmc/projects/priest/workspace/sootOutput:" +
			"/home/cmc/projects/priest/subjectapps/DurboDax/src:" +
			"/home/cmc/projects/priest/subjectapps/UnixUsage/src:" +
			"//home/cmc/projects/subjectapps/RiskInsurance/src\" " +
			"-p jb preserve-source-annotations " +
			"-output-format J " +
			"--keep-line-number " +
			"--xml-attributes " +
			"--src-prec java " +

		// SET FOR DurboDax
			"-process-dir \"/home/cmc/projects/priest/subjectapps/DurboDax/src\" " +
			"durbodax.Main";

    	// SET FOR RiskInsurance
			//"-process-dir \"/home/cmc/projects/priest/subjectapps/RiskInsurance/src\" " +
			//"com.riskIt.app.MainClass";

    	// prepare the arguments for soot
    	ArrayList<String> arguments = new ArrayList<String>();
    	String[] sootargs = runcmd.split("\"");
    	for (String string : sootargs) {
			if (string.split("[^\\\\][\\s]").length == 1){
				string = string.replace("\\ ", " ");
				arguments.add(string.trim());
			}
			else
				if(string.trim().startsWith("-")){
					for(String s : string.split("[\\s]"))
						if(s.trim().length() >0 )
							arguments.add(s.trim());
				}
				else
					arguments.add(string);
		}
    	String[] str = new String[arguments.size()];
    	int i=0;
    	for (String string : arguments) {
			str[i++] = string;
		}
		
		// run soot
		TaDaMain tada = new TaDaMain();
	    tada.mainArguments = str;
	    Thread thread = new Thread(tada, "soot");
	    thread.run();
	    thread.join();

	    // print coverage information
	    TreeMap<Integer, HashSet<String>> sortedMap = CoverageDatabaseAttributeMapping.getSortedMap();
	    int total = 0;
	    
	    for (Integer key : sortedMap.keySet())
	    	total = total + key;
	    
	    for (Integer key : sortedMap.keySet())
	    {
	    	HashSet<String> attributes = sortedMap.get(key);
	    	for (String attr : attributes)
	    	{
	    		int percent = key*100/total;
	    		System.out.println(attr + " " + key + " " + percent);
	    		// TODO Use this attribute information to map phenotype
	    	}
	    }

	    // now use the phenotype (as an argument) to select the columns to enter into the QI
	    // e.q. 0110 = main.id, main.race
	    //ArrayList<String> arQI = new ArrayList<String>();
	    String phenotype = "1100";
	    if(args.length > 1 && args[1].length() == 4)
	    	phenotype = args[1];
	    
	    String currentChar;
	    int charNum = 0;

	    String qis[] = {"main.sex",
	    				"main.id",
	    				"main.race",
	    				"main.nchild"};
	    
	    
	    while(charNum < phenotype.length())
	    {
	      currentChar = phenotype.substring(charNum, charNum+1);

	      if(currentChar.equals("1"))
	    	  arQI.add(qis[charNum]);
	    	  
	      charNum++;
	    }

	    //arQI.add("main.sex");
	    //arQI.add("main.id");
	    //arQI.add("main.race");
	    //arQI.add("main.nchild");

	    // compute coverage loss
        HashSet<String> qiSet = new HashSet<String>(arQI);
        int loc = (Integer) CoverageDatabaseAttributeMapping.computeEstimatedCoverageLoss(qiSet).get(0);
        double percent = (Double) CoverageDatabaseAttributeMapping.computeEstimatedCoverageLoss(qiSet).get(1);
        int estCoverageLoss = (int)percent;
        System.out.println("estimated coverage loss: " + estCoverageLoss + "% (" + loc + " loc)");

        // connect to the database
        String connectionCommand = "jdbc:mysql://localhost:3306/priest_durbodax";
        dbConnection = DriverManager.getConnection(connectionCommand, "root", "----");
        
        // anonymize and compute privacy metric
        double probability = 0.9;
        if(args.length > 0)
        	probability = Double.parseDouble(args[0]);
    	int size = arQI.size();
    	DatabaseAnonymizer dbAnonymizer = new DatabaseAnonymizer(probability);

    	for(int j=0; j<size; j++)
    	{
    		String tableColumn = (String) arQI.get(j);
    		int sep = tableColumn.indexOf('.');
    		String table = tableColumn.substring(0, sep);
    		String column = tableColumn.substring(sep+1, tableColumn.length());
    		dbAnonymizer.addQI(table.toLowerCase(), column.toLowerCase());
    	}
		
		//dbAnonymizer.permuteDataInAllTables();
    	Thread anonymizationThread = new Thread(dbAnonymizer, "anonymization");
    	anonymizationThread.start();
    	anonymizationThread.join();
    	
    	double totalRecordsRes = DatabaseAnonymizer.getTotalRecordsRes();
    	double anonymizationScoresRes = DatabaseAnonymizer.getAnonymizationScores();
    	double timeTakenRes = DatabaseAnonymizer.getTimeTaken();
    	double numberOfUniqueRecordsRes = DatabaseAnonymizer.getNumberOfUniqueRecords();

    	// compute actual test coverage
    	// TODO
    	
    	long estimatedTime = System.nanoTime() - startTime;
    	
    	System.out.println("total records: " + totalRecordsRes);
    	System.out.println("anon score: " + anonymizationScoresRes);
    	System.out.println("time taken: " + timeTakenRes);
    	System.out.println("unique records: " + numberOfUniqueRecordsRes);

    	// print final results
    	System.out.println("\nBOTTOM LINE:");
    	System.out.println("privacy level: " + anonymizationScoresRes);
    	System.out.println("estimated coverage loss: " + estCoverageLoss + "%");
    	System.out.println("execution time: " + (estimatedTime/ 1000000000.0) + " seconds");
	}
}
