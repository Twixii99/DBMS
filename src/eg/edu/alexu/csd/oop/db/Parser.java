package eg.edu.alexu.csd.oop.db;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

public class Parser {
    private LinkedList<DB> dataBases = new LinkedList<>();
    private DB currentDataBase;
    private Scanner scanner = new Scanner(System.in);

    public Parser() throws SQLException {
        run();
    }

    public void run() throws SQLException {
        String command;
        do {
            command = scanner.nextLine();
            System.out.println(parse(command));
        } while (!command.equalsIgnoreCase(".quit"));
        System.out.println(parse("create database database1;"));
        System.out.println(parse("create database database2  drop if EXIST ;"));
        System.out.println(parse("use database database1;"));
        System.out.println(parse("use databaSe if exist database2;"));
    }

    // takes the query as a parameter, and returns the message back to run(); to show it to the console
    private String parse(String query) throws SQLException {
        // for databases creation, for example "create database batabase1;"
        if (query.matches("(?i)^\\s*CREATE.+$")) {
            // we are dealing with create
            return parseCreate(query);
        } else if (query.matches("(?i)^\\s*Drop.+$")) {
            // we are dealing with drop
            return parseDrop(query);
        } else if (query.matches("(?i)^\\s*INSERT\\s+INTO\\s+.+$")) {
            // we are dealing with insert statement
            // example: "insert into table_name
        }
        return "wrong input!!";
    }

    private boolean containsDataBase(String dataBaseName) {
        Iterator<DB> iterator = dataBases.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().getName().equalsIgnoreCase(dataBaseName)) {
                // already exists
                return true;
            }
        }
        return false;
    }

    private DB getDataBase(String dataBaseName) {
        Iterator<DB> iterator = dataBases.listIterator();
        while (iterator.hasNext()) {
            DB dataBase = iterator.next();
            if (dataBase.getName().equalsIgnoreCase(dataBaseName)) {
                // already exists
                return dataBase;
            }
        }
        return null;
    }

    private String parseCreate(String query) throws SQLException {
        String query1 = query;
        query1 = query1.replaceAll("(?i)^\\s*CREATE\\s+", "");
        if (query1.matches("(?i)^\\s*DATABASE\\s+.+")) {
            // we are dealing with data base
            query1 = query1.replaceAll("(?i)^\\s*DATABASE\\s+", "");
            if (query1.matches("\\s*\\w+\\s*;\\s*$")) {
                // for databases creation, for example "create database databasename1;"
                String dataBaseName = query1.replaceAll("\\s*;\\s*$", "");
                if (containsDataBase(dataBaseName)) {
                    currentDataBase = getDataBase(dataBaseName);
                    return dataBaseName + " loaded successfully";

                } else {
                    // create brand new database
                    DB newDataBase = new DB(dataBaseName);
                    dataBases.add(newDataBase);
                    currentDataBase = newDataBase;
                    currentDataBase.executeStructureQuery("create database " + dataBaseName + ";");
                    return dataBaseName + " created successfully";
                }
            } else if (query1.matches("(?i)^\\s*\\w+\\s+DROP\\s+IF\\s+EXIST\\s*;\\s*$")) {
                // for example "create database IF NOT EXIST batabase1;"
                String dataBaseName = query1.replaceAll("(?i)\\s+DROP\\s+IF\\s+EXIST\\s*;\\s*$", "");
                query1 = "create database " + dataBaseName + " drop if exist;";
                if (containsDataBase(dataBaseName)) {
                    // already exists, then drop it and create a new one
                    DB selectedDataBase = getDataBase(dataBaseName);
                    if (selectedDataBase != null) {
                        dataBases.remove(selectedDataBase);
                        selectedDataBase.executeStructureQuery(query1);
                    }
                    currentDataBase = new DB(dataBaseName);
                    currentDataBase.executeStructureQuery(query1);
                    return "the database " + dataBaseName + " already exists, but created a new one with the same name";
                } else {
                    // doesn't exist
                    currentDataBase = new DB(dataBaseName);
                    dataBases.add(currentDataBase);
                    currentDataBase.executeStructureQuery(query1);
                    return dataBaseName + " created successfully";
                }
            }

        }
        else if (query1.matches("(?i)^\\s*TABLE\\s+.+")) { // done
            // we are dealing with table
            // example: "create table table_name values ( names varchar, phone int, email varchar);"
            String tableName = query1;
            tableName = tableName.replaceAll("(?i)^\\s*TABLE\\s+", "").replaceAll("(?i)\\s+VALUES.+\\s*;\\s*$", "");
            String[] dataStyle = query1.replaceAll("(?i)^\\s*TABLE\\s+" + tableName + "\\s+VALUES\\s*[(]\\s*","").replaceAll("\\s*[)]\\s*;\\s*$","").split(",");
            // dataStyle will look like "[name varchar, phone int, email varchar]"
            if (validateTableStyle(dataStyle)){
                currentDataBase.executeUpdateQuery(query);
                return tableName + " created successfully";
            }
        }
        return "wrong input";
    }

    private String parseDrop(String query) throws SQLException {
        String query1 = query;
        query1 = query1.replaceAll("(?i)^\\s*Drop\\s+", "");
        if (query1.matches("(?i)^\\s*DATABASE.+$")) {
            // we are dealing with database dropping
            // example: "DROP DATABASE database_name;"
            String dataBaseName = query1.replaceAll("(?i)^\\s*DATABASE\\s+", "").replaceAll("(?i)\\s*;\\s*$", "");
            if (containsDataBase(dataBaseName)) {
                // the database exist then
                DB selectedDataBase = getDataBase(dataBaseName);
                if (selectedDataBase != null) {
                    selectedDataBase.executeStructureQuery("drop database " + dataBaseName + ";");
                    this.dataBases.remove(selectedDataBase);
                    return dataBaseName + " dropped successfully";
                }
            } else {
                return "data base " + dataBaseName + " doesn't exist";
            }
        } else if (query1.matches("(?i)^\\s*TABLE\\s+\\w+\\s*;\\s*$")) {
            // we are dealing with table dropping
            // example: "DROP TABLE table_name;"
            String tableName = query1.replaceAll("(?i)^\\s*TABLE\\s+", "").replaceAll("(?i)\\s*;\\s*$", "");
            currentDataBase.executeUpdateQuery("drop table " + tableName + ";");
            return tableName + " dropped successfully";
        }
        return "wrong input";
    }

    private String parseInsert(String query) throws SQLException {
        return null;
    }

    private boolean validateTableStyle(String[] tableStyle){
        boolean valid = true;
        for (String s : tableStyle){
            if (!s.matches("(?i)^\\s*\\w+\\s+(varchar|int)\\s*")){
                valid = false;
                break;
            }
        }
        return valid;
    }
}

