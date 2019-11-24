package eg.edu.alexu.csd.oop.db;

import eg.edu.alexu.csd.oop.test.TestRunner;
import org.junit.Assert;

import java.io.File;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {

      //  Database db = (Database) TestRunner.getImplementationInstanceForInterface(Database.class);
       // DB db = new DB(); //DB is the class which implements Database

        File f = createDatabase_static((Database)eg.edu.alexu.csd.oop.test.TestRunner.getImplementationInstanceForInterface(Database.class)
                , "SaMpLe",true);
        System.out.println(f.getPath());
    }

    private static File createDatabase_static(Database db, String name, boolean drop){
        String path = db.createDatabase("sample" + System.getProperty("file.separator") + name, drop);  // create database
        Assert.assertNotNull("Failed to create database", path);
        File dbDir = new File(path);
        Assert.assertTrue("Database directory is not found or not a directory", dbDir.exists() && dbDir.isDirectory());
        return dbDir;
    }
/*
    public void testCreateAndOpenAndDropDatabase() {
        File dummy = null;
        Database db = (Database) TestRunner.getImplementationInstanceForInterface(Database.class);
        {
            File dbDir = createDatabase_static(db, "SaMpLe", true);
            String files[] = dbDir.list();
            Assert.assertTrue("Database directory is not empty!", files == null || files.length == 0);
            dummy = new File(dbDir, "dummy");
            try {
                boolean created = dummy.createNewFile();
                Assert.assertTrue("Failed t create file into DB", created && dummy.exists());
            } catch (IOException e) {
                TestRunner.fail("Failed t create file into DB", e);
            }
        }
        {
            File dbDir = createDatabase_static(db, "sAmPlE", false);
            String files[] = dbDir.list();
            Assert.assertTrue("Database directory is empty after opening! Database name is case insensitive!", files.length > 0);
            Assert.assertTrue("Failed t create find a previously created file into DB", dummy.exists());
        }
        {
            File dbDir = createDatabase_static(db, "SAMPLE", true);
            String files[] = dbDir.list();
            Assert.assertTrue("Database directory is not empty after drop! Database name is case insensitive!", files == null || files.length == 0);
        }
    }*/
}
