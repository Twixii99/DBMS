package eg.edu.alexu.csd.oop.db;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

public class DB implements Database {
    private String name;
    private LinkedList<Table> tables = new LinkedList<>();
    private String directoryOfDataBases = "DataBase";


    @Override
    public String createDatabase(String databaseName, boolean dropIfExists) {
        File directory = new File(directoryOfDataBases);
        if(! directory.exists()) createDirectory(directory.getPath());
        System.out.println("hi");
        //directory for all existed databases
        for(String x  : Objects.requireNonNull(directory.list())){ //loop on all existed db
            boolean equals = x.equals(databaseName);
            if( equals && !dropIfExists ){ //if exist and not drop >> use it
                this.name = x;
                try {
                    tables = ReadTables.ReadTables(databaseName);
                }
                catch (Exception e) {
                    return e.getMessage();
                }
                return "existAndUse";
            }
            if( equals ){ //if exist and drop >> drop the old then create new one and use it
                dropDirectory(directoryOfDataBases+System.getProperty("file.separator")+databaseName);
                createDirectory(directoryOfDataBases+System.getProperty("file.separator")+databaseName);
                this.name = databaseName;
                return "droppedAndCreateNew";
            }
        }

        if(createDirectory(directoryOfDataBases+System.getProperty("file.separator")+databaseName)){ //if not exists create it
            this.name = databaseName;
            return "notExistAndCreated";
        }
        else return "not created";
    }

    private void dropDirectory(String path){ //this function can delete a directory and all its contents
        File oldData = new File(path);
        String[]entries = oldData.list();
        for (String s : Objects.requireNonNull(entries)) {
            File currentFile = new File(oldData.getPath(), s);
            if (currentFile.isDirectory()) dropDirectory(currentFile.getPath());
            currentFile.delete();
        }
        oldData.delete();
    }
    private boolean createDirectory(String path){ //this function creates a directory witt this path
        File directory = new File(path);
        return directory.mkdirs();
    }

    @Override
    public boolean executeStructureQuery(String query) throws SQLException {
        if(query.matches("^create .+")) { //starts with create
            LinkedList<Object> resultOfQuery = Parser.parseCreate(query);
            if (resultOfQuery == null) throw new SQLException("Wrong create command");
            if ((Boolean) resultOfQuery.get(0)) { //create DataBase
                String dataName = (String) resultOfQuery.get(1);
                Boolean drop = (Boolean) resultOfQuery.get(2);
                createDatabase(dataName, drop);
            } else { //create a table
                String tableName = (String) resultOfQuery.get(1);
                String[] namesOfColumns = (String[]) resultOfQuery.get(3);
                String[] types = (String[]) resultOfQuery.get(4);
                Table t = new Table(tableName, namesOfColumns, types);
                addTable(t);
                XML.convertIntoXml(name, t);
            }
        }
        else { //starts with drop
            LinkedList<Object> resultOfQuery = Parser.parseDrop(query);
            if (resultOfQuery == null) throw new SQLException("Wrong Drop command");

            if ((Boolean) resultOfQuery.get(0)) { //drop DataBase
                String dataName = (String) resultOfQuery.get(1);
                dropDirectory(directoryOfDataBases+System.getProperty("file.separator")+dataName);
            } else { //drop a table
                String tableName = (String) resultOfQuery.get(1);
                Table t = getTable(tableName);
                if (t == null ) throw new SQLException("table name not exist!");
                removeTable(t);
                dropDirectory(directoryOfDataBases+System.getProperty("file.separator")+
                        name+System.getProperty("file.separator")+tableName);
            }
        }
        return true;
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
        if (columns.getFirst().equals("*")){
            columns = headers;
        }
        // when ahmed finished it
        LinkedList<Object> references = new LinkedList<>();
        if (!condition.equals("")){
            Mark mark = new Mark();
            try{
                references = (LinkedList<Object>) mark.GetData(condition,getTable(tableName));
            } catch (Exception e){
                System.out.println(e.getMessage());
                return null;
            }
        } else {
            for (Object record : getTable(tableName).getTable()){
                references.add(record);
            }
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

    private Table getTable(String tableName){
        for (Table table : tables) {
            if (table.getName().equals(tableName)) {
                return table;
            }
        }
        return null;
    }

    private void addTable (Table table){
        this.tables.add(table);
    }
    private void removeTable (Table table){
        this.tables.remove(table);
    }

    public void setName(String name) {
        this.name = name;
    }
}