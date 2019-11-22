package eg.edu.alexu.csd.oop.db;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

public class Parser {
    private static LinkedList<DB> dataBases = new LinkedList<>();
    private static DB currentDataBase;
    private Scanner scanner = new Scanner(System.in);

    public Parser() throws SQLException {
        run();
    }

    private void run() throws SQLException {
        String command;
        do {
            command = scanner.nextLine();
            System.out.println(parse(command));
        } while (!command.equalsIgnoreCase(".quit"));
    }

    // takes the query as a parameter, and returns the message back to run(); to show it to the console
    private String parse(String query) throws SQLException {
        // for databases creation, for example "create database batabase1;"
        if (query.matches("(?i)^\\s*(CREATE|Drop).+$")) {
            // we are dealing with create
            if (currentDataBase != null) {
                currentDataBase.executeStructureQuery(query);
                return "";
            } else {
                DB dummyDataBase = new DB("dummy_database");
                dummyDataBase.executeUpdateQuery(query);
                return "";
            }
        } else if (query.matches("(?i)^\\s*INSERT\\s+INTO\\s.+$")) {
            // we are dealing with insert statement
            if (currentDataBase != null) {
                currentDataBase.executeUpdateQuery(query);
                return "";
            } else {
                return "no database in use";
            }
        } else if (query.matches("(?i)^\\s*SELECT\\s+.+\\s+FROM\\s.+$")) {
            // we are dealing with select statement
            if (currentDataBase != null) {
                currentDataBase.executeQuery(query);
                return "";
            } else {
                return "no database in use";
            }
        } else if (query.matches("(?i)^\\s*DELETE\\s+FROM\\s.+$")) {
            // we are dealing with delete statement
            if (currentDataBase != null) {
                currentDataBase.executeUpdateQuery(query);
                return "";
            } else {
                return "no database in use";
            }
        } else if (query.matches("(?i)^\\s*UPDATE\\s+.+$")) {
            // we are dealing with delete statement
            if (currentDataBase != null) {
                currentDataBase.executeUpdateQuery(query);
                return "";
            } else {
                return "no database in use";
            }
        }
        return "wrong input!!";
    }


    /*
     * takes the query as a parameter
     * returns a linked list of objects
     * at index = 0, a Boolean Object it is true if it is to create a database, and false if to create a table
     * at index = 1, a String object to tell you the name of database or the name of the table
     * at index = 2, an Boolean Object, is useless in case of create table, if true then drop the existing database
     *           with the same name if false then don't drop it.
     * at index = 3, an object of an array of strings that holds the names of each column.
     * at index = 4, an object of an array of classes that holds that types of each column.
     * put in your mind that you have to use type cast when dealing with the return value
     * like : "Boolean value = (Object[]) Parser.parseCreate(query).get(2);" (Boolean not boolean)
     * returns null if the query doesn't match
     */
    public static LinkedList<Object> parseCreate(String query) throws SQLException {
        if (query.matches("(?i)^\\s*CREATE.+$")) {
            LinkedList<Object> result = new LinkedList<>();
            String query1 = query;
            query1 = query1.replaceAll("(?i)^\\s*CREATE\\s+", "");
            if (query1.matches("(?i)^\\s*DATABASE\\s+.+")) {
                // we are dealing with data base
                result.add(true);
                query1 = query1.replaceAll("(?i)^\\s*DATABASE\\s+", "");
                if (query1.matches("^\\s*\\w+\\s*;\\s*$")) {
                    // for databases creation, for example "create database databasename1;"
                    String dataBaseName = query1.replaceAll("\\s*$", "");
                    result.add(dataBaseName);
                    result.add(false);
                    if (containsDataBase(dataBaseName)) {
                        currentDataBase = getDataBase(dataBaseName);
                    } else {
                        // create brand new database
                        DB newDataBase = new DB(dataBaseName);
                        dataBases.add(newDataBase);
                        currentDataBase = newDataBase;
                    }
                    return result;
                } else if (query1.matches("(?i)^\\s*\\w+\\s+DROP\\s+IF\\s+EXIST\\s*$")) {
                    // for example "create database IF NOT EXIST batabase1;"
                    String dataBaseName = query1.replaceAll("(?i)\\s+DROP\\s+IF\\s+EXIST\\s*$", "");
                    result.add(dataBaseName);
                    query1 = "create database " + dataBaseName + " drop if exist;";
                    if (containsDataBase(dataBaseName)) {
                        // already exists, then drop it and create a new one
                        DB selectedDataBase = getDataBase(dataBaseName);
                        if (selectedDataBase != null) {
                            dataBases.remove(selectedDataBase);
                        }
                        currentDataBase = new DB(dataBaseName);
                    } else {
                        // doesn't exist
                        currentDataBase = new DB(dataBaseName);
                        dataBases.add(currentDataBase);
                    }
                    result.add(true);
                    return result;
                }
            } else if (query1.matches("(?i)^\\s*TABLE\\s+.+")) { // done
                // we are dealing with table
                // example: "create table table_name values ( names varchar, phone int, email varchar);"
                result.add(false);
                String tableName = query1;
                tableName = tableName.replaceAll("(?i)^\\s*TABLE\\s+", "").replaceAll("(?i)\\s+VALUES.+\\s*$", "");
                result.add(tableName);
                result.add(false);
                String[] dataStyle = query1.replaceAll("(?i)^\\s*TABLE\\s+" + tableName + "\\s+VALUES\\s*[(]\\s*", "").replaceAll("\\s*[)]\\s*$", "").split(",");
                // dataStyle will look like "[name varchar, phone int, email varchar]"
                String[] headers = new String[dataStyle.length];
                Class[] types = new Class[dataStyle.length];
                if (validateTableStyle(dataStyle)) {
                    for (int i = 0; i < dataStyle.length; i++) {
                        String[] temp = dataStyle[i].replaceAll("^\\s+", "").replaceAll("\\s+$", "").split("\\s+");
                        headers[i] = temp[0];
                        if (temp[1].equalsIgnoreCase("varchar")) {
                            types[i] = String.class;
                        } else if (temp[1].equalsIgnoreCase("int")) {
                            types[i] = Integer.class;
                        } else if (temp[1].equalsIgnoreCase("bool")) {
                            types[i] = Boolean.class;
                        }
                    }
                }
                result.add(headers);
                result.add(types);
                return result;
            }
        }
        return null;
    }

    /*
     * takes the query as a parameter
     * returns a linked list of objects
     * at index = 0, a Boolean Object it is true if it is to drop a database, and false if to drop a table
     * at index = 1, a String object to tell you the name of database or the name of the table
     * put in your mind that you have to use type cast when dealing with the return value
     * like : "Boolean value = (Object[]) Parser.parseDrop(query).get(0);" (Boolean not boolean)
     * returns null if the query doesn't match
     */
    public static LinkedList<Object> parseDrop(String query) throws SQLException {
        if (query.matches("(?i)^\\s*DROP\\s+.+\\s*$")) {
            LinkedList<Object> result = new LinkedList<>();
            String query1 = query;
            query1 = query1.replaceAll("(?i)^\\s*Drop\\s+", "");
            if (query1.matches("(?i)^\\s*DATABASE.+$")) {
                // we are dealing with database dropping
                // example: "DROP DATABASE database_name;"
                result.add(true);
                String dataBaseName = query1.replaceAll("(?i)^\\s*DATABASE\\s+", "").replaceAll("(?i)\\s*$", "");
                result.add(dataBaseName);
                if (containsDataBase(dataBaseName)) {
                    // the database exist then
                    DB selectedDataBase = getDataBase(dataBaseName);
                    if (selectedDataBase != null) {
                        dataBases.remove(selectedDataBase);
                    }
                }
            } else if (query1.matches("(?i)^\\s*TABLE\\s+\\w+\\s*$")) {
                // we are dealing with table dropping
                // example: "DROP TABLE table_name;"
                result.add(false);
                String tableName = query1.replaceAll("(?i)^\\s*TABLE\\s+", "").replaceAll("(?i)\\s*$", "");
                result.add(tableName);
            }
            return result;
        }
        return null;
    }

    /*
     * takes the query as its parameter
     * returns a linkedlist of objects
     * at index = 0, there is a string object that contains the name of the table.
     * at index = 1, there is an array of strings object that contains the columns.
     * at index = 2, there is an object of an array of objects that contains the data.
     * put in your mind that you have to use type cast when dealing with the return value
     * like : "String value = (Object[]) Parser.parseInsert(query).get(2);"
     * returns null if the query doesn't match
     */
    public static LinkedList<Object> parseInsert(String query) {
        // example: "insert into table_name (column1 ,column2, column3) values (value1, value2, value3);"
        if (query.matches("(?i)^\\s*INSERT\\s+INTO\\s+\\w+\\s*[(].*[)]\\s*VALUES\\s*[(].+[)]\\s*$")) {
            String query1 = query;
            String tableName = query1.replaceAll("(?i)^\\s*INSERT\\s+INTO\\s+", "").replaceAll("(?i)\\s*[(].*[)]\\s*VALUES\\s*[(].+[)]\\s*$", "");
            query1 = query;
            String[] headers = removeSpaces(query1.replaceAll("(?i)^\\s*INSERT\\s+INTO\\s+\\w+\\s*[(]\\s*", "").replaceAll("(?i)[)]\\s*VALUES\\s*[(].+[)]\\s*$", "").split(","));
            // headers are for example: "column1,column2,column3".
            query1 = query;
            String[] values = removeSpaces(query1.replaceAll("(?i)^\\s*INSERT\\s+INTO\\s+\\w+\\s*[(].*[)]\\s*VALUES\\s*[(]\\s*", "").replaceAll("(?i)\\s*[)]\\s*$", "").split(","));
            // values are for example: "value1,value2,value3".
            LinkedList<Object> result = new LinkedList<>();
            result.add(tableName);
            result.add(headers);
            result.add(values);
            return result;
        }
        return null;
    }

    /*
     * takes the query as its parameter
     * returns a linkedlist of objects
     * at index = 0, there is an array object that tells you which columns you want to select,
     *          unless you get and array whose first element is *(string) then select all the columns.
     * at index = 1, there is a string object that tells you the name of the table.
     * at index = 2, there is a string object that contains the condition after where,
     *          and if the object is an empty string then no condition.
     * put in your mind that you have to use type cast when dealing with the return value
     * like : "String value = (Object[]) Parser.parseInsert(query).get(2);"
     * returns null if the query doesn't match
     */
    public static LinkedList<Object> parseSelect(String query) {
        if (query.matches("(?i)^\\s*SELECT\\s+.+\\s+FROM\\s+.+\\s*$")) {
            LinkedList<Object> result = new LinkedList<>();
            // don't know if it contains where or not
            query = query.replaceAll("(?i)^\\s*SELECT\\s+", "").replaceAll("(?i)\\s*$", "");
            String query1 = query;
            String requiredColumns = query1.replaceAll("(?i)\\s+FROM.+$", "");
            if (requiredColumns.matches("^\\s*[*]\\s*$")) {
                // meaning we "SELECT * FORM table_name;".
                result.add(new String[]{"*"});
            } else {
                // something like "column1, column2".
                result.add(removeSpaces(requiredColumns.split(",")));
            }
            if (query.matches("(?i)^.+WHERE.+$")) {
                // contains where
                String tableName = query1.replaceAll("(?i)^.+FROM\\s+", "").replaceAll("(?i)\\s+WHERE.+$", "");
                result.add(tableName);
                String condition = query1.replaceAll("(?i)^.+FROM\\s+.+WHERE\\s+", "");
                result.add(condition);
            } else {
                // doesn't contain where
                String tableName = query1.replaceAll("(?i)^.+FROM\\s+", "");
                result.add(tableName);
                result.add("");
            }
            return result;
        }
        return null;
    }

    /*
     * takes the query as its parameter
     * returns a linkedlist of objects
     * at index = 0, there is a object of a string of the name of the table.
     * at index = 1, there is a string object that contains the condition after where,
     *          and if the object is an empty string then no condition.
     * put in your mind that you have to use type cast when dealing with the return value
     * like : "String value = (Object[]) Parser.parseInsert(query).get(2);"
     * returns null if the query doesn't match
     */
    public static LinkedList<Object> parseDelete(String query) {
        if (query.matches("(?i)^\\s*DELETE\\s+FROM\\s+(\\w+|\\w+\\s+WHERE\\s+.+)\\s*$")) {
            LinkedList<Object> result = new LinkedList<>();
            String query1 = query;
            query1 = query1.replaceAll("(?i)^\\s*DELETE\\s+FROM\\s+", "").replaceAll("\\s*$", "");
            String tableName;
            String condition;
            if (query.matches("(?i)^\\s*DELETE\\s+FROM\\s+\\w+\\s+WHERE\\s+.+\\s*$")){
                tableName = query1.replaceAll("(?i)\\s+WHERE.+$", "");
                condition = query1.replaceAll("(?i)^\\s*\\w+\\s+WHERE\\s+", "");
            } else {
                tableName = query1.replaceAll("(?i)\\s+WHERE.+$", "");
                condition = "";
            }
            result.add(tableName);
            result.add(condition);
            return result;
        }
        return null;
    }

    /*
     * takes the query as its parameter
     * returns a linkedlist of objects
     * at index = 0, there is a object of a string of the name of the table.
     * at index = 1, there is a string object that contains the condition after where,
     *          and if the object is an empty string then no condition.
     * at index = 2, there is an object of an array that contains the columns that we need to set the data at.
     * at index = 3, there is an object of an array that contains the new values that we are ganna set.
     * put in your mind that you have to use type cast when dealing with the return value
     * like : "String value = (Object[]) Parser.parseInsert(query).get(2);"
     * returns null if the query doesn't match
     */
    public static LinkedList<Object> parseUpdate(String query) {
        if (query.matches("(?i)^\\s*UPDATE\\s+\\w+\\s+SET\\s+.+\\s*$")){
            LinkedList<Object> result = new LinkedList<>();
            String tableName;
            String condition;
            String[] setters;
            String[] columns;
            String[] newValues;
            if (query.matches("(?i)^\\s*UPDATE\\s+\\w+\\s+SET\\s+.+\\s+WHERE\\s+.+\\s*$")) {
                // the query has where
                tableName = query.replaceAll("(?i)^\\s*UPDATE\\s+", "").replaceAll("(?i)\\s+SET\\s+.+\\s+WHERE\\s+.+\\s*$", "");
                condition = query.replaceAll("(?i)^\\s*UPDATE\\s+\\w+\\s+SET\\s+.+\\s+WHERE\\s+", "").replaceAll("\\s*$", "");
                setters = query.replaceAll("(?i)^\\s*UPDATE\\s+\\w+\\s+SET\\s+", "").replaceAll("\\s+WHERE\\s+.+\\s*$", "").split(",");

            } else {
                // doesn't contain where
                tableName = query.replaceAll("(?i)^\\s*UPDATE\\s+", "").replaceAll("(?i)\\s+SET\\s+.+\\s*$", "");
                condition = "";
                setters = query.replaceAll("(?i)^\\s*UPDATE\\s+\\w+\\s+SET\\s+", "").replaceAll("\\s*$", "").split(",");
            }
            columns = new String[setters.length];
            newValues = new String[setters.length];
            for (int i = 0; i < setters.length; i++) {
                String[] arr = setters[i].split("=");
                columns[i] = arr[0];
                newValues[i] = arr[1];
            }
            columns = removeSpaces(columns);
            newValues = removeSpaces(newValues);
            result.add(tableName);
            result.add(condition);
            result.add(columns);
            result.add(newValues);
            return result;
        }
        return null;
    }

    private static boolean validateTableStyle(String[] tableStyle) {
        boolean valid = true;
        for (String s : tableStyle) {
            if (!s.matches("(?i)^\\s*\\w+\\s+(varchar|int)\\s*")) {
                valid = false;
                break;
            }
        }
        return valid;
    }

    private static String[] removeSpaces(String[] array) {
        String[] tempArray = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            tempArray[i] = array[i].replaceAll("\\s+", "");
        }
        return tempArray;
    }

    private static boolean containsDataBase(String dataBaseName) {
        for (DB dataBase : dataBases) {
            if (dataBase.getName().equalsIgnoreCase(dataBaseName)) {
                // already exists
                return true;
            }
        }
        return false;
    }

    private static DB getDataBase(String dataBaseName) {
        for (DB dataBase : dataBases) {
            if (dataBase.getName().equalsIgnoreCase(dataBaseName)) {
                // already exists
                return dataBase;
            }
        }
        return null;
    }

    public static void addDataBase(DB dataBase) {
        dataBases.add(dataBase);
    }

}

