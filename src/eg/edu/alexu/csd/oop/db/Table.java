package eg.edu.alexu.csd.oop.db;

import java.util.Iterator;
import java.util.LinkedList;

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

    // returns false if it doesn't find the record
    public boolean removeRecord(String header, Object value) {
        int[] index = indexOf(header, value);
        if (index[0] != -1) { // index of record != -1
            table.remove(index[0]); // removed so the one after it is taking its place
            return true;
        }
        return false;
    }

    // returns the index if the mentioned record "[index of record, index of header]"
    private int[] indexOf(String header, Object value) {
        // find the index of the header in (the index of the column).
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
            for (int i = 0; i < table.size(); i++) {
                if (table.get(i)[j].equals(value)) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{-1, -1};
    }

    // as you know that we only update value by value
    public boolean updateRecord(String header, Object oldValue, Object newValue) {
        int[] index = indexOf(header, oldValue);
        if ((oldValue.getClass().equals(newValue.getClass())) && (index[0] != -1)) {
//            if ((types[index[1]].equalsIgnoreCase("Integer") && (oldValue instanceof Integer)) ||
//                    (types[index[1]].equalsIgnoreCase("String") && (oldValue instanceof String))) {
//                // type of value checked
//            }
            Object[] tempRecord = table.get(index[0]).clone();
            tempRecord[index[1]] = newValue;
            table.set(index[0], tempRecord);
            return true;
        }
        return false;
    }

//     if headersToBeSelected = null then we select the whole table
    public Object[][] select(String[] headersToBeSelected, String conditionHeader, Object targetValue, char relation) {
        LinkedList<Object[]> selectedTable = new LinkedList<>();
        int[] indexesOfHeaders = new int[headersToBeSelected.length]; // indexes of the headers to be displayed
        boolean foundHeader = false;
        int conditionHeaderIndex = 0;
        for (int i = 0; i < headersToBeSelected.length; i++) {
            for (int j = 0; j < headers.length; i++) {
                if (!foundHeader && (headers[j].equalsIgnoreCase(conditionHeader))) {
                    foundHeader = true;
                    conditionHeaderIndex = j;
                }
                if (headersToBeSelected[i].equalsIgnoreCase(headers[j])) {
                    indexesOfHeaders[i] = i;
                    continue;
                }
            }
        }

        Iterator<Object[]> iterator = table.listIterator();
        while (iterator.hasNext()) {
            Object[] tempRecord = iterator.next();

            // the condition to add to selected table
            if (tempRecord[conditionHeaderIndex].equals(targetValue)) {
                selectedTable.add(tempRecord);
            }
        }


        Object[][] result = new Object[selectedTable.size()][headersToBeSelected.length];
        Iterator<Object[]> iterator1 = selectedTable.listIterator();
        int i = 0;
        while (iterator.hasNext()){
            Object[] tempRecord = iterator1.next();
            int k = 0;
            for (int j : indexesOfHeaders){
                result[i][k++] = tempRecord[j];
            }
            i++;
        }
        return result;
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
            for (int j = 1 ; j < this.headers.length ; j++ ){ // start from one so that we don't include the innerID with us
                arr[i][j - 1] = record[j];
            }
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

    public String[] getTypes() {
        String[] tempTypes = new String[types.length - 1];
        System.arraycopy(types, 1, tempTypes, 0,tempTypes.length);
        return tempTypes;
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

    // get the order by which you added this record, and it returns -1 if record isn't found, or removed by now
    public int getInnerID(String header, Object value){
        int[] indexes = indexOf(header, value);
        if (indexes[0] != -1){ // meaning, found the record
            return (Integer) table.get(indexes[0])[0];
        }
        return -1;
    }

    public void setHeadersVisible(boolean headersVisible) {
        this.headersVisible = headersVisible;
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
