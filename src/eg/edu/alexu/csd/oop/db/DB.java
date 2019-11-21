package eg.edu.alexu.csd.oop.db;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;

public class DB implements Database {
    private String name;
    private LinkedList<Table> tables = new LinkedList<>();

    public DB(String name) {
        this.name = name;
    }


    @Override
    public String createDatabase(String databaseName, boolean dropIfExists) {
        return null;
    }

    @Override
    public boolean executeStructureQuery(String query) throws SQLException {
        return false;
    }

    @Override
    public Object[][] executeQuery(String query) throws SQLException {
        LinkedList<Object> references;
        String condition = query;
        if (query.matches("(?i)^\\s*select\\s.+where\\s+.+\\s*;\\s*$")){
            condition = condition.replaceAll("(?i)^\\s*select\\s.+where\\s+","").replaceAll("(?i)\\s*;\\s*$","");
            condition.replaceFirst("^"," ");
            query = query.replaceAll("(?i)where\\s+.+\\s*","");
            // now we have a free where query
        }
        // in cass the user requested to select all the table with *
        if (query.matches("(?i)^\\s*select\\s+[*]\\s+from\\s+\\w+\\s*;\\s*$")) {
            String query1 = query;
            String tableName = query1.replaceAll("(?i)^\\s*select\\s+[*]\\s+from\\s+", "").replaceAll("(?i)\\s*;\\s*$", "");
            // now here i must select the whole table, i have table name though, i just can call System.out.println(table);, and the whole ordeal is finished
            for (Table table : tables) {
                if (table.getName().equalsIgnoreCase(tableName)) {
                    System.out.println(tableName);
                    return table.toArray();
                }
            }
            System.out.println("can't find the requested table");
            return null;
        }
        // in general cass
        else if (query.matches("(?i)^\\s*select\\s+[a-z0-9,]\\s+from\\s+\\w+\\s*;\\s*$")) {
            String query1 = query;
            String tableName = query1.replaceAll("(?i)^\\s*select\\s+[*]\\s+from\\s+", "").replaceAll("(?i)\\s*;\\s*$", "");
            query1 = query;
            LinkedList<String> columns = (LinkedList<String>) Arrays.asList(query1.replaceAll("(?i)^\\s*select\\s+", "").replaceAll("(?i)\\s+from\\s+\\w+\\s*;\\s*$", "").split(","));
            Table selectedTable = null;
            for (Table table : tables) {
                if (table.getName().equalsIgnoreCase(tableName)) {
                    selectedTable = table;
                    break;
                }
            }
            if (selectedTable != null && Arrays.asList(selectedTable.getHeaders()).containsAll(columns)) { // all the required columns exist in the original table
                // now i have the table name and the required columns
            }
            System.out.println("can't find the requested table");
            return null;
        }
        return new Object[0][];
    }

    @Override
    public int executeUpdateQuery(String query) throws SQLException {
        return 0;
    }

    public String getName() {
        return this.name;
    }

}
