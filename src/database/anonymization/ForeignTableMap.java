package database.anonymization;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ForeignTableMap {
	private static HashMap<String, ArrayList<String>> table2ForeignTables = new HashMap<String, ArrayList<String>>();
	private static HashMap<String, ArrayList<String>> tableAsForeignTables = new HashMap<String, ArrayList<String>>();
	
	
	public static void updateMap(String table, ArrayList<String> foreignTables){
		if(table2ForeignTables.get(table) == null)
			table2ForeignTables.put(table, foreignTables);
		else{
			for (String ft : foreignTables) {
				ArrayList<String> fts = new ArrayList<String>();
				if(!fts.contains(ft))
					fts.add(ft);
			}
			
		}
		for (String foreignTable : foreignTables) {
			if(tableAsForeignTables.get(foreignTable) == null){
				ArrayList<String> tables = new ArrayList<String>();
				tables.add(table);
				tableAsForeignTables.put(foreignTable, tables);
			}
			else{
				ArrayList<String> tables = tableAsForeignTables.get(foreignTable);
				if(!tables.contains(table))
					tables.add(table);
			}
		}
	}
	
	public static ArrayList<String> getForeignTablesFor(String table){
		return table2ForeignTables.get(table);
	}
	
	public static ArrayList<String> getTablesHavingForeignTableAs(String table){
		return tableAsForeignTables.get(table);
	}
}
