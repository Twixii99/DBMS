package eg.edu.alexu.csd.oop.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
        }
        LinkedList<Integer> columnsIndexes = new LinkedList<>();
        for (Object reference : references){
            for (String column : columns){
                if (headers.contains(column)){
                    columnsIndexes.add(headers.indexOf(column));
                }
            }
        }
        Object[][] array = new Object[references.size()][columnsIndexes.size()];
        for (int i = 0; i < references.size(); i++) {
            int j = 0;
            for (Integer index : columnsIndexes){
                array[i][j++] = ((Object[]) references.get(i))[index];
            }
        }
        return array;
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