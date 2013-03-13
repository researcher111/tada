/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tadagui;

/**
 *
 * @author Lavanya
 */
import java.awt.event.ActionListener;
import java.sql.*;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.util.*;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Lavanya
 */
public class AbstractComboBoxModel  {
     static Connection con;
     static Statement stmt;

     static class ShowInputDialog{
    	  public static String showInputDialog(){
    		  Object[] possibleValues = { "First", "Second", "Third" };
    		  //jdbc:derby://localhost:1527/riskit;create=true;user=se549;password=se549
//    		  String selectedValue = 
//    			  JOptionPane.showInputDialog(null, "Enter Command " +
//    			  		" to connect to a database. \n (For example jdbc:derby://localhost:1527/UnixUser;create=true;user=me;password=mine) : ", 
//    			  		"jdbc:derby://localhost:1527/UnixUser;create=true;user=me;password=mine");
//    		  String selectedValue = 
//    			  JOptionPane.showInputDialog(null, "Enter Command " +
//    			  		" to connect to a database. \n (For example jdbc:derby://localhost:1527/UnixUser;create=true;user=me;password=mine) : ", 
//    			  		"jdbc:derby://localhost:1527/riskit;create=true;user=se549;password=se549");
//    		  String selectedValue = 
//    			  JOptionPane.showInputDialog(null, "Enter Command " +
//    			  		" to connect to a database. \n (For example jdbc:derby://localhost:1527/UnixUser;create=true;user=me;password=mine) : ", 
//    			  		"jdbc:derby://localhost:1527/ipums;create=true;");
//    		  String selectedValue = 
//    			  JOptionPane.showInputDialog(null, "Enter Command " +
//    			  		" to connect to a database. \n (For example jdbc:derby://localhost:1527/UnixUser;create=true;user=me;password=mine) : ", 
//    			  		"jdbc:mysql://localhost:3306/n2a");
    		  String selectedValue = 
    			  JOptionPane.showInputDialog(null, "Enter Command " +
    			  		" to connect to a database. \n (For example jdbc:derby://localhost:1527/UnixUser;create=true;user=me;password=mine) : ", 
    			  		"jdbc:mysql://localhost:33061/priest_durbodax");
    		  
    		  if(selectedValue != null)
    	          return selectedValue.toString();
    	      return showInputDialog();  
    	  }
    	}
     
    //public static String schema = "n2a";
     public static String schema = "priest_durbodax";
     public static String[] listTables()
    {
       ArrayList ar = new ArrayList();
        try{
        Class.forName("org.apache.derby.jdbc.ClientDriver");
        Class.forName("com.mysql.jdbc.Driver");
        }
        catch(ClassNotFoundException e)
        {
            System.out.println(e);
        }
        try{
        	String connectionCommand = ShowInputDialog.showInputDialog();
        	
         con = DriverManager.getConnection(connectionCommand, "root", "---");
         //for n2a application
        	//con = DriverManager.getConnection(connectionCommand, "root", "");
         stmt = con.createStatement();
         System.out.println("Connected");
         DatabaseMetaData meta = con.getMetaData();
         ResultSet res = meta.getTables(schema , null, null,new String[] {"TABLE"});
         while (res.next()){
              ar.add(res.getString("TABLE_NAME"));
         }

        }catch(SQLException e){
            System.err.println(e);
        }
       String[] tables = new String[ar.size()];
        for (int i = 0; i < ar.size(); i++) {
            tables[i] = (String)ar.get(i);
        }

            return tables;
    }
     public static String[] processTableValue()
      {
           //System.out.print("process table value\n");

           ArrayList arCol = new ArrayList();
           try
           {
               //System.out.println("selectedTable"+ProjectView.selectedTable);
               DatabaseMetaData meta = con.getMetaData();
               //ResultSet res = meta.getColumns(schema, null, "USER_INFO", null);
               ResultSet res = meta.getColumns(schema, null, TaDaFinalView.selectedTable, null);
               while (res.next())
                {
                    arCol.add(res.getString(4));
                }

               // System.out.println("after list1 selection"+arCol);

          }//end of try
           catch(SQLException e)
           {
                 System.err.println(e);
           }//end of catch
           String[] col = new String[arCol.size()];
           for (int i = 0; i < arCol.size(); i++)
            {
             col[i] = (String)arCol.get(i);
            }
           return col;
      }
      static String fktable;
      static String fkcol;
     public static String[] processAttributeValue()
      {
          // System.out.print("process table value\n");
           ArrayList arAttr = new ArrayList();
           try
           {

               DatabaseMetaData meta = con.getMetaData();
               ResultSet res = meta.getImportedKeys(schema, null, TaDaFinalView.selectedTable);

               while (res.next())
                {
                   if(res.getString("FKCOLUMN_NAME").equalsIgnoreCase(TaDaFinalView.selectedCol))
                   {
                       fktable =res.getString(3);
                       fkcol=res.getString(4);
                       arAttr.add(res.getString(3)+"."+res.getString(4));
                    }
                }

          }//end of try
           catch(SQLException e)
           {
                 System.err.println(e);
           }//end of catch
           //System.out.println(arAttr);
           String[] attr = new String[arAttr.size()];
           for (int i = 0; i < arAttr.size(); i++)
            {
             attr[i] = (String)arAttr.get(i);
            }
           // System.out.println("Foreign Keys"+attr);

            return attr;
      }
     public static Vector columnNames = new Vector();
     public static Vector data = new Vector();
     static int columnCount;
     public static void TableFromDatabase()
    {

         try
         {
        	 stmt.setMaxRows(100);
        	 data.clear();
        	 columnNames.clear();
             ResultSet rs = stmt.executeQuery("SELECT * from "+ TaDaFinalView.selectedTable);
        	 
             ResultSetMetaData rm = rs.getMetaData();
             columnCount = rm.getColumnCount();
             //get coloumn names and add it to the coloumname vector
             for (int i = 1; i <= columnCount; i++)
            {
               columnNames.add(rm.getColumnName(i));
            }
             //get the rows

              while (rs.next())
            {
                Vector row = new Vector(columnCount);

                for (int i = 1; i <= columnCount; i++)
                {
                    row.add( rs.getObject(i) );

                }

                data.add(row);


            }

         }
         catch(Exception e)
        {
            e.printStackTrace();
        }

     }


    public static Vector foreignColumnNames = new Vector();
    public static Vector foreignData = new Vector();
    public static void getForeignValues()
      {
        try
         {
             ResultSet rs = stmt.executeQuery("SELECT distinct * from "+fktable+"");

             ResultSetMetaData rm = rs.getMetaData();
             int columnCount = rm.getColumnCount();
             //get coloumn names and add it to the coloumname vector
             for (int i = 1; i <= columnCount; i++)
            {

                 foreignColumnNames.add(rm.getColumnName(i));

                 //columnNames.addElement( rm.getColumnName(i) );
            }
             //get the rows

              while (rs.next())
            {
                Vector row = new Vector(columnCount);

                for (int i = 1; i <= columnCount; i++)
                {
                    row.add( rs.getObject(i) );

                }

                foreignData.add(row);


            }

         }
         catch(Exception e)
        {
            System.out.println( e );
        }

      }

    public static Vector sensitiveColumnNames;
    public static Vector sensitiveData;
   
    public static void getSensitiveValues(String table, String col)
      {
        try
         {
            sensitiveColumnNames = new Vector();
            sensitiveData = new Vector();
            int columnCount = TaDaFinalView.arSense.size();
            String colName = TaDaFinalView.selectedTable.concat(".").concat(TaDaFinalView.selectedCol);
                 sensitiveColumnNames.add(colName);

             //get the rows
             //ResultSet rs = stmt.executeQuery("SELECT distinct "+TaDaFinalView.selectedCol+" from "+TaDaFinalView.selectedTable+"");
             ResultSet rs = stmt.executeQuery("SELECT distinct "+col+" from "+table+" ORDER BY 1 DESC");

              while (rs.next())
                {
                 // System.out.print(rs.getObject(1));
                Vector row = new Vector(columnCount);
                row.add( rs.getObject(1) );
                sensitiveData.add(row);
                }

         }
         catch(Exception e)
        {
            System.out.println( e );
        }

      }
    
  public static Vector qiColumnNames;
    public static Vector qiData;

    public static void getQIValues(String table, String col)
      {
        try
         {
            qiColumnNames = new Vector();
            qiData = new Vector();
            int columnCount = TaDaFinalView.arQI.size();
            String colName = TaDaFinalView.selectedTable.concat(".").concat(TaDaFinalView.selectedCol);
                 qiColumnNames.add(colName);

             //get the rows
             //ResultSet rs = stmt.executeQuery("SELECT distinct "+TaDaFinalView.selectedCol+" from "+TaDaFinalView.selectedTable+"");
             ResultSet rs = stmt.executeQuery("SELECT distinct "+col+" from "+table+" ORDER BY 1 DESC");

              while (rs.next())
                {
                 // System.out.print(rs.getObject(1));
                Vector row = new Vector(columnCount);
                row.add( rs.getObject(1) );
                qiData.add(row);
                }

         }
         catch(Exception e)
        {
            System.out.println( e );
        }

      }
    public static String[] splitFunc(String s)
    {
        String[] temp = s.split("\\.");
        return temp;

    }
	public static Connection getConnection() {
		// TODO Auto-generated method stub
		return con;
	}
   
  }


