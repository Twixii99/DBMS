package eg.edu.alexu.csd.oop.db;

public class Main {
    public static void main(String[] args) {
        Table table = new Table("contacts",new String[]{"name", "phone", "email"}, new String[]{"String", "Integer", "String"});
        table.add(new Object[]{"abc", 12456789, "abc@gmail.com"});
        table.add(new Object[]{"def", 987654321, "def@gmail.com"});
        System.out.println(table);
        table.updateRecord("phone",987654321,481926);
        table.updateRecord("name","abc" ,"ABC");
        table.add(new Object[]{"fido", 12304, "dog@email.com"});
        System.out.println(table);

        table.removeRecord("email","def@gmail.com");
        table.removeRecord("name","ABC");
        System.out.println(table);

        System.out.println(table.schema());
    }
}
