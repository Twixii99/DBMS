package eg.edu.alexu.csd.oop.db;

import java.io.File;
import java.util.LinkedList;


/**
 * to call this class use it
 *  Table []t =ReadTables.ReadTables("");
 *  it return array of tables
 */
public class ReadTables {

    public static Table[] ReadTables(String DB) throws Exception {
        String tablesName[]=new File(new File("DataBase"+System.getProperty("file.separator")+DB).getAbsolutePath()).list();
        Table[] tables=new Table[tablesName.length];
        for(int i=0;i<tables.length;i++){
            tables[i]=MakeTable(tablesName[i],DB);
        }
        return tables;
    }

    private static Table MakeTable(String tableName, String db) throws Exception {
        XSD.getXSD(db,tableName);
         String[] headers=XSD.GetNames();
         Class[] types=XSD.GetTypes();
         String[] StringTypes=GetString(types);
         LinkedList<Object[]> table=XML.convertFromXml(db,tableName,types);
        Table table1=new Table(tableName,headers,StringTypes) ;
        table1.addAll(table);
        return table1;
    }

    private static String[] GetString(Class[] types) throws Exception {
        int i=0;
        String str[]=new String[types.length];
        for(Class c :types){
           if(c==String.class){
               str[i++]="String";
           }else if(c==Integer.class||c==int.class){
               str[i++]="Integer";
            }else if(c==Boolean.class||c==boolean.class){
               str[i++]="boolean";
           }else {
               throw new Exception("type "+c+" is not supported");
           }
        }
        return str;
    }

}
