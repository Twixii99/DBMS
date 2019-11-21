package eg.edu.alexu.csd.oop.db;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class Table {
    private String name;
    private String[] headers;
    private String[] types;
    private LinkedList<Object[]> table;
    private int lastRecordId;
    private boolean headersVisible;

    public Table(String name, String[] headers, String[] types) {
        this.name = name;
        // adding innerID column to the table
        String[] tempHeaders = new String[headers.length + 1];
        tempHeaders[0] = "innerID";
        System.arraycopy(headers,0,tempHeaders,1,headers.length);
        String[] tempTypes = new String[types.length + 1];
        tempTypes[0] = "Integer";
        System.arraycopy(types,0,tempTypes,1,types.length);
        this.headers = tempHeaders;
        this.types = tempTypes;
        this.lastRecordId = 0;
        this.table = new LinkedList<>();
        this.headersVisible = true;
    }

    // return true if added successfully, added innerID column in the first, given to the record when it is added (recorded)
    public boolean add(Object[] record) {
        if (checkRecordFormat(record)) {
            Object[] tempRecord = new Object[record.length + 1];
            tempRecord[0] = lastRecordId++;
            System.arraycopy(record,0,tempRecord,1,record.length);
            table.add(tempRecord);
            return true;
        }
        return false;
    }

    // returns false if the record format doesn't match what is in the table
    private boolean checkRecordFormat(Object[] record) {
        if (record.length == headers.length - 1) {
            for (int i = 0; i < record.length; i++) {
                if ((types[i + 1].equalsIgnoreCase("Integer"))) {
                    if (!(record[i] instanceof Integer)) {
                        return false;
                    }
                } else if ((types[i + 1].equalsIgnoreCase("String"))) {
                    if (!(record[i] instanceof String)) {
                        return false;
                    }
                }
            }
            // format checked
            return true;
        }
        return false;
    }

    // removes if the record matching the inputs in exist
    public void removeRecord(String header, Object value) {
        LinkedList<Integer> indexes = indexOf(header, value);
        if (!indexes.isEmpty()){
            Iterator<Object[]> iterator = table.listIterator();
            while (iterator.hasNext() && !indexes.isEmpty()){
                Object[] record = iterator.next();
                if (indexes.contains((Integer) record[0])){
                    indexes.remove((Integer) record[0]);
                    iterator.remove();
                }
            }
        }
    }

    // returns the innerIDs if the mentioned record
    private LinkedList<Integer> indexOf(String header, Object value) {
        // find the index of the header in (the index of the column).
        LinkedList<Integer> indexes = new LinkedList<>();
        int j = 0;
        boolean foundHeader = false;
        for (String s : headers) {
            if (!s.equalsIgnoreCase(header)) {
                j++;
                continue;
            }
            foundHeader = true;
            break;
        }
        if (foundHeader) {
            for (Object[] record : table) {
                if (record[j].equals(value)) {
                    indexes.add((Integer) record[0]);
                }
            }
        }
        return indexes;
    }

    // as you know that we only update value by value
    public void updateRecord(String header, Object oldValue, Object newValue) {
        // get the index of header in headers
        int j = 0;
        for (j = 0 ; j < headers.length ; j++){
            if (headers[j].equalsIgnoreCase(header)){
                break;
            }
        }
        // update the record(s) matching the inputs
        LinkedList<Integer> indexes = indexOf(header, oldValue);
        if ((oldValue.getClass().equals(newValue.getClass())) && (!indexes.isEmpty())) {
            ListIterator<Object[]> iterator = table.listIterator();
            while (iterator.hasNext() && !indexes.isEmpty()){
                Object[] record = iterator.next();
                if (indexes.contains((Integer) record[0])){
                    indexes.remove((Integer) record[0]);
                    Object[] newRecord = new Object[record.length];
                    System.arraycopy(record,0,newRecord,0,record.length);
                    newRecord[j] = newValue;
                    iterator.set(newRecord);
                }
            }
        }
    }

    public String schema(){
         StringBuilder stringBuilder = new StringBuilder("CREATE TABLE ");
         stringBuilder.append(this.name).append(" VALUES(");
         for (int i = 1 ; i < headers.length ; i++){ // start from 1 so that we don't include the innerID with us
             stringBuilder.append(this.headers[i]).append(' ');
             if (types[i].equalsIgnoreCase("Integer")){
                 stringBuilder.append("int").append(' ');
             } else if (types[i].equalsIgnoreCase("String")){
                 stringBuilder.append("varchar").append(' ');
             }
             if ((i < headers.length - 1)){
                 stringBuilder.append(',');
             }
         }
        return (stringBuilder.append(')').append(';')).toString();
    }

    public Object[][] toArray(){
        Object[][] arr = new Object[this.table.size()][this.headers.length - 1];
        Iterator<Object[]> iterator = this.table.listIterator();
        int i = 0;
        while (iterator.hasNext()){
            Object[] record = iterator.next();
            // start from one so that we don't include the innerID with us
            if (this.headers.length - 1 >= 0) System.arraycopy(record, 1, arr[i], 0, this.headers.length - 1);
            i++;
        }
        return arr;
    }

    public String getName() {
        return name;
    }

    public String[] getHeaders() {
        String[] tempHeaders = new String[headers.length - 1];
        System.arraycopy(headers, 1, tempHeaders, 0,tempHeaders.length);
        return tempHeaders;
    }

    public Class[] getTypes() {
        Class[] typesClasses = new Class[types.length - 1];
        for (int i = 1 ; i < types.length ; i++ ){
            if (types[i].equalsIgnoreCase("String")){
                typesClasses[i - 1] = String.class;
            } else if (types[i].equalsIgnoreCase("Integer")){
                typesClasses[i - 1] = Integer.class;
            } else if (types[i].equalsIgnoreCase("Boolean")){
                typesClasses[i - 1] = Boolean.class;
            }
        }
        return typesClasses;
    }

    // order By Which The Record Is Added, returns null if not found ( was removed by now)
    public Object[] getRecord(int innerID){
        Iterator<Object[]> iterator = table.listIterator(0);
        while (iterator.hasNext()){
            Object[] record = iterator.next();
            if (((Integer) record[0]) == innerID){
                Object[] tempRecord = new Object[record.length - 1];
                System.arraycopy(record,1,tempRecord,0,tempRecord.length);
                return tempRecord;
            }
        }
        return null;
    }

    // get the order by which you added this/there record(s), and returns empty linkedlist if it didn't find any suitable record
    public LinkedList<Integer> getInnerID(String header, Object value){
        return indexOf(header, value);
    }

    public void setHeadersVisible(boolean headersVisible) {
        this.headersVisible = headersVisible;
    }

    // addes a linked list of record, till it sees an invalid one then it cancels out the operation
    public void addAll(LinkedList<Object[]> subTable){
        for (Object[] record : subTable) {
            if (!add(record)) {
                return;
            }
        }
    }
    
    public LinkedList<Object[]> getTable() {
        return table;
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Table : ");
        stringBuilder.append(this.name).append('\n');
        if (headersVisible){
            for (int i = 1; i < headers.length; i++) {
                stringBuilder.append(headers[i]).append((i < headers.length - 1) ? ('|') : ('\n'));
            }
        }
        for (Object[] record : table) {
            for (int i = 1; i < record.length; i++) { // start from one as to not include the innerID
                stringBuilder.append(record[i]).append((i < record.length - 1) ? ('|') : ('\n'));
            }
        }
        return stringBuilder.toString();
    }
}
