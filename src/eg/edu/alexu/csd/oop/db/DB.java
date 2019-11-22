package eg.edu.alexu.csd.oop.db;

import java.sql.SQLException;
import java.util.*;

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
        System.out.println("executeStructureQuery called");
        return false;
    }

    @Override
    public Object[][] executeQuery(String query) throws SQLException {
        LinkedList<Object> result = Parser.parseSelect(query);
        if (result == null){
            throw new SQLException("wrong input format");
        }
        LinkedList<String> columns = new LinkedList<>(Arrays.asList((String[]) result.get(0)));
        String tableName = (String) result.get(1);
        String condition = (String) result.get(2);
        LinkedList<String> headers = new LinkedList<>(Arrays.asList(getTable(tableName).getHeaders()));
        // when ahmed finished it
        Mark mark = new Mark();

        LinkedList<Object> references = new LinkedList<>();
        try{
            references = (LinkedList<Object>) mark.GetData(condition,getTable(tableName));
        } catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        Object[][] arr  = new Object[references.size()][columns.size()];
        LinkedList<Integer> columnsIndexes = new LinkedList<>();
        for (String column : columns){
            columnsIndexes.add(headers.indexOf(column));
        } // now i have the indexes of the columns to be selected
        for (int j = 0 ; j < headers.size() ; j++){
            if (columnsIndexes.contains(j)){ // j is a column to be selected
                int i = 0;
                for (Object record : references){ // i is an index of a record selected
                    arr[i++][j] = ((Object[]) record)[j];
                }
            }
        }
        return arr;
    }

    @Override
    public int executeUpdateQuery(String query) throws SQLException {
        System.out.println("executeUpdateQuery");
        return 0;
    }

    public String getName() {
        return this.name;
    }

    public boolean containsTable(String tableName){
        Iterator<Table> tableIterator = tables.listIterator();
        while (tableIterator.hasNext()){
            if(tableIterator.next().getName().equals(tableName)){
                return true;
            }
        }
        return false;
    }

    public Table getTable(String tableName){
        Iterator<Table> tableIterator = tables.listIterator();
        while (tableIterator.hasNext()){
            Table table = tableIterator.next();
            if(table.getName().equals(tableName)){
                return table;
            }
        }
        return null;
    }

    public void addTable (Table table){
        this.tables.add(table);
    }

}