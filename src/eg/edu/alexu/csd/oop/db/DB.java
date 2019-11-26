package eg.edu.alexu.csd.oop.db;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

public class DB implements Database {
    private String path;
    private LinkedList<Table> tables = new LinkedList<>();
    private Mark mark = new Mark();
    private List<Object> data;
    private String[] actualHeaders;
    private Class[] actualTypes;
    private String directoryOfDatabases = "DataBasesDirectory";
    DB(){
    }

    @Override
    public String createDatabase(String databaseName, boolean dropIfExists) {
        File direct = new File(directoryOfDatabases);
        if(!direct.exists()) direct.mkdirs();
        databaseName = directoryOfDatabases + System.getProperty("file.separator")+databaseName;
        File file = new File(databaseName);
        boolean exist = file.exists() ;
        if( exist && !dropIfExists ){ //if exist and not drop >> use it
            this.path = databaseName;
            try {
                tables = ReadTables.ReadTables(databaseName);
            }
            catch (Exception e) {
                return e.getMessage();
            }
            return databaseName;
        }
        else if(exist){ //if exist and drop >> drop the old then create new one and use it
            dropDirectory(databaseName);
            createDirectory(databaseName);
            tables = new LinkedList<>();
            this.path = databaseName;
            return databaseName;
        }
        if(createDirectory(databaseName)){ //if not exists create it
            this.path = databaseName;
            tables = new LinkedList<>();
            return databaseName;
        }
        else return "not created";
    }

    private void dropDirectory(String path){ //this function can delete a directory and all its contents
        File oldData = new File(path);
        String[]entries = oldData.list();
        if(entries != null) {
            for (String s : entries) {
                dropDirectory(oldData.getPath() + System.getProperty("file.separator") + s);
            }
        }
        oldData.delete();
    }
    private boolean createDirectory(String path){ //this function creates a directory witt this path
        File directory = new File(path);
        return directory.mkdirs();
    }

    @Override
    public boolean executeStructureQuery(String query) throws SQLException {
        if(query.matches("(?i)^create .+")) { //starts with create
            LinkedList<Object> resultOfQuery = Parser.parseCreate(query);
            if (resultOfQuery == null){
                System.out.println("Wrong create command");
                return false;
            }
            if ((Boolean) resultOfQuery.get(0)) { //create DataBase
                String dataName = (String) resultOfQuery.get(1);
                Boolean drop = (Boolean) resultOfQuery.get(2);
                createDatabase(dataName, drop);
            } else { //create a table
                if (path == null) {
                    System.out.println("no database initialized");
                    return false;
                }
                String tableName = (String) resultOfQuery.get(1);
                File tablePath = new File(path+System.getProperty("file.separator")+tableName);
                if (tablePath.exists()) {
                    System.out.println("table is already existed");
                    return false;
                }
                String[] namesOfColumns = (String[]) resultOfQuery.get(3);
                String[] types = (String[]) resultOfQuery.get(4);
                Table t = new Table(tableName, namesOfColumns, types);
                addTable(t);
                XML.convertIntoXml(path, t);
            }
        }
        else { //starts with drop
            LinkedList<Object> resultOfQuery = Parser.parseDrop(query);
            if (resultOfQuery == null){
                System.out.println("wrong query");
                return false;
            }
            if ((Boolean) resultOfQuery.get(0)) { //drop DataBase
                String dataName = (String) resultOfQuery.get(1);
                dropDirectory(directoryOfDatabases+System.getProperty("file.separator")+dataName);
                this.tables=new LinkedList<>();
                this.path = null;

            } else { //drop a table
                String tableName = (String) resultOfQuery.get(1);
                Table t = getTable(tableName);
                if (t == null ) {
                    System.out.println("table name not exist!");
                    return false;
                }
                removeTable(t);
                dropDirectory(path+System.getProperty("file.separator")+tableName);
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
                references = (LinkedList<Object>) mark.getData(condition,getTable(tableName));
            } catch (Exception e){
                System.out.println(e.getMessage());
                return null;
            }
        } else {
            references.addAll(getTable(tableName).getTable());
        }
        Object[][] arr  = new Object[references.size()][columns.size()];
        LinkedList<Integer> columnsIndexes = new LinkedList<>();
        for (String column : columns){
            columnsIndexes.add(headers.indexOf(column));
        } // now i have the indexes of the columns to be selected
        int k = 0;
        for (int j = 0 ; j < headers.size() ; j++){
            if (columnsIndexes.contains(j)){ // j is a column to be selected
                int i = 0;
                for (Object record : references){ // i is an index of a record selected
                    arr[i++][k] = ((Object[]) record)[j];
                }
                k++;
            }
        }
        return arr;
    }

    @Override
    public int executeUpdateQuery(String query) throws Exception {
        if(query.matches("(?i)^\\s*INSERT\\s+INTO\\s.+$"))
            return this.excuteInsert(query);
        if(query.matches("(?i)^\\s*DELETE\\s+FROM\\s.+$"))
            return this.excuteDelete(query);
        if(query.matches("(?i)^\\s*UPDATE\\s+.+$"))
            return this.excuteUpdate(query);
        return -1;
    }

    /**
     * @param
     *      query of type string the query
     * @return
     *        Integer define the modified row in the table
     * this method takes the query and then get the main data to be inserted
     * checks if the table already exits
     * checks if the number of scheme objects equals the insertions objects
     * checks data types of the the insertions
     * rearrange the data to make it right to according to the scheme
     */
    private int excuteInsert(String query) {

        data = Parser.parseInsert(query);
        if(data == null)
            return -1;
        Table operateOnTable = this.existance(data.get(0).toString());
        if(operateOnTable != null) {
            String[] headers = (String[])data.get(1);
            String[] values = (String[])data.get(2);
            if(headers.length != values.length) return 0;
            actualHeaders = operateOnTable.getHeaders();
            actualTypes = operateOnTable.getTypes();
            if(this.containsTheseHeaders(headers, values)) {
                String[] strings = this.rearrange(headers, values);
                operateOnTable.add(convertToObjects(strings));
                return 1;
            }
            else
                return -1;
        }
        return -1;
    }

    private Object[] convertToObjects(String[] str) {
        Object[] obj = new Object[str.length];
        int i = 0;
        for(String temp : str) {
            if(temp.matches("^[0-9]+$"))
                obj[i++] = Integer.parseInt(temp);
            else if(temp.matches("(?i).*true|flase.*"))
                obj[i++] = Boolean.parseBoolean(temp);
            else
                obj[i++] = temp;
        }
        return obj;
    }

    private int excuteDelete(String query) throws Exception {
        data = Parser.parseDelete(query);
        if(data == null)
            return 0;
        Table deleteFromTable = this.existance(data.get(0).toString());
        if(deleteFromTable != null) {
            if((String)data.get(1) == "") {
                deleteFromTable.emptyTheTable();
            } else {
                LinkedList<Object> deletedRows = mark.getData(data.get(1).toString(), deleteFromTable);
                Iterator<Object> deletedRecords = deletedRows.iterator();
                while(deletedRecords.hasNext()) {
                    Object[] thisRecord = (Object[])deletedRecords.next();
                    this.actualHeaders = deleteFromTable.getHeaders();
                    for(int i = 0; i < actualHeaders.length; ++i) {
                        if(thisRecord[i] != null) {
                            deleteFromTable.removeRecord(actualHeaders[i], thisRecord[i]);
                            return deletedRows.size();
                        }
                    }
                }
            }
        }
        return -1;
    }

    private int excuteUpdate(String query) throws Exception {
        LinkedList<Object> markedList = new LinkedList<>();
        data = Parser.parseUpdate(query);
        if(data == null)
            return 0;
        Table updatedTable = this.existance(data.get(0).toString());
        if(updatedTable != null) {
            String[] cols = (String[])data.get(2);
            String[] newValues = (String[])data.get(3);
            actualHeaders = updatedTable.getHeaders();
            actualTypes = updatedTable.getTypes();
            if(cols.length != newValues.length) return -1;
            if(this.containsTheseHeaders(cols, newValues)) {
                if(data.get(1).toString() != null) {
                    markedList = mark.getData(data.get(1).toString(), updatedTable);
                    Object[] oldy;
                    String[] oldValues = new String[cols.length];
                    for(int i = 0; i < cols.length; ++i) {
                        for(int j = 0; cols[j] != actualHeaders[j]; ++j);
                        oldy = (Object[])markedList.get(i);
                        oldValues[i] = (String)oldy[i];
                    }
                    for(int i = 0; i < cols.length; ++i) {
                        updatedTable.updateRecord(cols[i], oldValues[i], newValues[i]);
                    }
                }
                else
                    updatedTable.updateHoleTable(this.rearrange(cols, newValues));
            }
        }
        return markedList.size();
    }

    private Table existance(String tableName) {
            return this.getTable(tableName);
    }

    private Boolean containsTheseHeaders(String[] headers, String[] values) {
        LinkedList<String> actualHeadersAsList = new LinkedList<String>(Arrays.asList(actualHeaders));
        int i;
        for(i = 0; i < headers.length; ++i) {
            if(actualHeadersAsList.contains(headers[i])) {
                int index = actualHeadersAsList.indexOf(headers[i]);
                if(actualTypes[index].toString().equals("class java.lang.Integer")) {
                    if(!values[i].matches("(?i)[^a-z]$")){
                        continue;
                    }
                    else {
                        return false;
                    }
                }
                else if(actualTypes[index].toString().equals("class java.lang.Boolean")) {
                    if(!values[i].matches("(?i).*true.*|.*false.*"))
                        continue;
                    else return false;
                }
            }
        }
        return i == headers.length;
    }

    private String[] rearrange(String[] headers, String[] values) {
        LinkedList<String> headersList = new LinkedList<String>(Arrays.asList(headers));
        String[] dummy = new String[actualHeaders.length];
        for(int i = 0; i < headersList.size(); ++i) {
            int index = Arrays.asList(actualHeaders).indexOf((headersList.get(i)));
            dummy[index] = values[i];
        }
        return dummy;
    }

    private Table getTable(String tableName){
        for (Table table : tables) {
            if (table.getName().equalsIgnoreCase(tableName)) {
                return table;
            }
        }
        return null;
    }

    String schema(){
        StringBuilder stringBuilder = new StringBuilder("");
        Iterator<Table> tableIterator = tables.listIterator();
        while (tableIterator.hasNext()){
            stringBuilder.append(tableIterator.next());
            if (tableIterator.hasNext()){
                stringBuilder.append('\n');
            }
        }
        return stringBuilder.toString();
    }

    private void addTable(Table table){
        this.tables.add(table);
    }
    private void removeTable (Table table){
        this.tables.remove(table);
    }

}