package eg.edu.alexu.csd.oop.db;

public class Main {
    public static void main(String[] args) {
        Table table = new Table("contacts",new String[]{"name", "phone", "email"}, new String[]{"String", "Integer", "String"});
        table.add(new Object[]{"Ahmed Kamal", 12456789, "rtya89@gmail.com"});
        table.add(new Object[]{"Ahmed Alzayady", 987654321, "Alzayady@gmail.com"});
        System.out.println(table);
        table.updateRecord("phone",987654321,481926);
        table.updateRecord("name","Ahmed Kamal" ,"ahmed kamal");
        table.add(new Object[]{"fido", 12304, "dog@email.com"});
        System.out.println(table);

        table.removeRecord("email","Alzayady@gmail.com");
        System.out.println(table);
    }
}
