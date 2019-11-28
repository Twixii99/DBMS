package eg.edu.alexu.csd.oop.db;

import java.util.Arrays;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) throws Exception {
        new Parser();

        Table t = new Table("T1",new String[]{"name","age"},new String[]{"String","int"});
        t.add(new Object[]{"ahmed",12});
        t.add(new Object[]{"ad",13});
        t.add(new Object[]{"amd",15});

        t.getTable().remove(0);
        System.out.println(t.getTable().size());

     }

}
