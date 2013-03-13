package annotaten2a;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Script to annotate files for n2a
 * */
public class AnnotateN2A {
	public static String path = "C:\\Documents and Settings\\kunal_taneja\\workspace\\n2a-batch\\n2a-batch-domain\\src\\main\\java\\es\\dia\\pos\\n2a\\batch\\domain";
	public static String outputDirectory = "C:\\annotatedFiles";
	
	public static void main(String[] args) throws IOException {
		getFileList();
	}
	
	static ArrayList<String> files = new ArrayList<String>();
	static HashMap<String, String> file2TableMap = new HashMap<String, String>();
	
	public static void getFileList() throws IOException{
		File directory = new File(path);
		String[] children = directory.list();
		for (String string : children) {
			files.add(string);
		}
		
		Pattern pattern = Pattern.compile("^[\\s]*public[\\s]*[\\w]*[\\s]*get[\\w]*");
		Pattern tablePattern = Pattern.compile("Entity generated from [\\w]* table");
		Pattern entityPattern = Pattern.compile("Id class for the [\\w]* entity");
		
		for (String fileName : children) {
			String tableName = null;
			File javaFile = new File(path +"\\" + fileName);
			BufferedReader reader = new BufferedReader(new FileReader(javaFile));
			File anotatedFile = new File(outputDirectory + "\\" + fileName);
			anotatedFile.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(anotatedFile));
			
			String line = "";
			while ( line != null) {
				line = reader.readLine();
				if(line == null) 
					continue;
				if(tableName == null){
					Matcher tableMatcher = tablePattern.matcher(line);
					if(tableMatcher.find()){
						tableName = extractTableName(line);
						System.out.println("Table: " + tableName);
						writer.write(line + "\n");
						continue;
					}
					Matcher entityMatcher = entityPattern.matcher(line);
					if(entityMatcher.find()){
						if(!file2TableMap.containsKey(fileName)){
							@SuppressWarnings("unused")
							int debug =1;
						}
						tableName = file2TableMap.get(fileName);
						System.out.println("Table for entity: " + tableName);
						writer.write(line + "\n");
						continue;
					}
				}
				
				if(tableName != null){
					Matcher matcher = pattern.matcher(line);
					if(matcher.find()){
						String typeName = extractType(line);
						if(!(typeName.equals("Double") || typeName.equals("Long") || typeName.equals("int")
								|| typeName.equals("String") || typeName.equals("Date") || typeName.equals("Integer"))){
							file2TableMap.put(typeName + ".java", tableName);
						}
						else{
							String dbAttribute = getDBAttributeFromMethodDecl(line);
							System.out.println("Attribute: " + dbAttribute);
							String annotationString = "@TaDaMethod(variablesToTrack = \"temp$0\", correspondingDatabaseAttribute = \"" + tableName + "." + dbAttribute +  "\")";
							writer.write(annotationString + "\n");
						}
					}
				}
				writer.write(line + "\n");
			}
			writer.close();
			reader.close();
			if(tableName == null){
				System.out.println("File " + fileName + " not annotated");
			}
		}
		@SuppressWarnings("unused")
		int finish =1;
	}

	static HashSet<String> types = new HashSet<String>();
	private static String extractType(String line) {
		// TODO Auto-generated method stub
		int index1 = line.indexOf("public");
		int index2 = line.indexOf("get");
		String type = line.substring(index1+6, index2-1).trim();
		
		if(!files.contains(type + ".java")){
			if(type.equals("int")){
				@SuppressWarnings("unused")
				int debug =1;
			}
			types.add(type);
		}
		return type;
	}

	private static String extractTableName(String line) {
		int fromIndex  = line.indexOf("from");
		int tableIndex = line.indexOf("table");
		if(fromIndex == -1 || tableIndex == -1){
			try {
				throw new Exception("Invalid Index");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String tableName = line.substring(fromIndex+4, tableIndex-1).trim();
		
		return tableName;
	}

	private static String getDBAttributeFromMethodDecl(String line) {
		
		ArrayList<String> dbAttributeNameParts = new ArrayList<String>(); 
		String[] parts =   line.split("get");
		
		if(parts.length != 2){
			try {
				throw new Exception("somethings wrong");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String names = parts[1];
		int index = names.indexOf("(");
		names = names.substring(0, index);
		String[] nameParts = names.split("[A-Z]");
		int lastIndex =-1;
		for (String part : nameParts) {
			if(part.length() == 0)
				continue;
			int start = names.indexOf(part, lastIndex +1);
			int end = start + part.length();
			lastIndex = end;
			if(start != 0 )
				start = start-1;
			dbAttributeNameParts.add( names.substring(start, end));
		}
		String attributeName = "";
		for (String part : dbAttributeNameParts) {
			if(attributeName.length() > 0)
				attributeName = attributeName + "_" + part.toUpperCase();
			else
				attributeName = part.toUpperCase();
		}
		
		
		
		return attributeName;
	}

}
