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
        if (query.matches("(?i)^\\s*CREATE\\s+DATABASE\\s+\\w+\\s*;\\s*$")) {
            String query1 = query;
            String dataBaseName = query1.replaceAll("(?i)^\\s*CREATE\\s+DATABASE\\s+", "").replaceAll("\\s*;\\s*$", "");
            if (containsDataBase(dataBaseName)) {
//                throw new SQLException("The DataBase With The Name \"" + dataBaseName + "\" Already Exists");?
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
        }
        else if (query.matches("(?i)^\\s*CREATE\\s+DATABASE\\s+\\w+\\s+DROP\\s+IF\\s+EXIST\\s*;\\s*$")) {
            // for example "create database IF NOT EXIST batabase1;"
            String query1 = query;
            String dataBaseName = query1.replaceAll("(?i)^\\s*CREATE\\s+DATABASE\\s+", "").replaceAll("(?i)\\s+DROP\\s+IF\\s+EXIST\\s*;\\s*$", "");
            query1 = "create database " + dataBaseName + " drop if exist;";
            if (containsDataBase(dataBaseName)) {
                // already exists, then drop it and create a new one
                DB selectedDataBase = getDataBase(dataBaseName);
                if (selectedDataBase != null){
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
        /*else if (query.matches("(?i)^\\s*USE\\s+DATABASE\\s+\\w+\\s*;\\s*$")) {
            // for example "use database batabase1;"
            String query1 = query;
            String dataBaseName = query1.replaceAll("(?i)^\\s*USE\\s+DATABASE\\s+", "").replaceAll("\\s*;\\s*$", "");
            if (containsDataBase(dataBaseName)){
                // already exists, so use it.
                currentDataBase = getDataBase(dataBaseName);
                return dataBaseName + " selected successfully";
            } else {
                // doesn't exist
                throw new SQLException("The DataBase With The Name \"" + dataBaseName + "\" Doesn't Exist");
            }
        }
        else if (query.matches("(?i)^\\s*USE\\s+DATABASE\\s+IF\\s+EXIST\\s+\\w+\\s*;\\s*$")){
            // for example "use database batabase1;"
            String query1 = query;
            String dataBaseName = query1.replaceAll("(?i)^\\s*USE\\s+DATABASE\\s+IF\\s+EXIST\\s+", "").replaceAll("\\s*;\\s*$", "");
            if (containsDataBase(dataBaseName)) {
                // the database exists
                currentDataBase = getDataBase(dataBaseName);
                return dataBaseName + " selected successfully";
            } else {
                // the database doesn't exist
                return dataBaseName + " can't be found";
            }
        }*/
        // for select
        else if (query.matches("(?i)^\\s*(SELECT)\\s+")){
            String createTableRegex = "(?i)^\\s*CREATE\\s+TABLE\\s+\\w+\\s*[(]\\s*\\w+\\s+(int|varchar)";

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

    private DB getDataBase(String dataBaseName){
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
}

