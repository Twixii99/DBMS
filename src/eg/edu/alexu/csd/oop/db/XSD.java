package eg.edu.alexu.csd.oop.db;

import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XSD {
    private static LinkedList<String> Labels=new LinkedList();
    private static LinkedList<Class> TYPES=new LinkedList();
    public static void MakeXsd(String databaseName, String table, String[] Labels, Class[] Types)  {
    XML.MakeDirectory(databaseName,table);
      File dbfile=new File("Database"+ System.getProperty("file.separator") + databaseName +  System.getProperty("file.separator") + table +  System.getProperty("file.separator")+ table + ".xsd");
      try {
          dbfile.createNewFile();
      } catch (IOException e) {
          e.printStackTrace();
      }
      FileWriter writer;
      try {
          writer = new FileWriter(dbfile);
          StringBuilder str=new StringBuilder("");


          String s=getBeginOfXsd(databaseName,table);
          str.append(s);
          writer.write(s);


          s=getDataXsd(Labels,Types);
          writer.write(s);


          s=getEndXsd();
          writer.write(s);

          writer.flush();
          writer.close();


      } catch (IOException e) {
          e.printStackTrace();
      } catch (Exception e) {
          e.printStackTrace();
      }

  }
   public  static void getXSD(String DB_NAME ,  String Table) throws IOException {

  Labels=new LinkedList<>();
  TYPES=new LinkedList<>();
       String str = new String(Files.readAllBytes(Paths.get(new File("Database"+ System.getProperty("file.separator") + DB_NAME +  System.getProperty("file.separator") + Table +  System.getProperty("file.separator") + Table + ".xsd").getAbsolutePath())));

       Pattern pattern=Pattern.compile("(.*)(<xs:complexType name = '"+Table+"'>)(.*)");

       Matcher matcher=pattern.matcher(str);

       if (matcher.find())
       {
           int x=matcher.start();

           String s=new StringBuilder(str).delete(0,x).toString();

           pattern=Pattern.compile("(<xs:element)(.*)(/>)");

           matcher=pattern.matcher(s);

           while (matcher.find())
           {
               String LastString =matcher.group(2);

               Labels.add(GetName(LastString));
               TYPES.add(GetType(LastString));

           }
       }else throw new EOFException("ERROR FORMAT XSD FILE");
    }



    public  static String[] GetNames(){
      String s[]=new String[Labels.size()];
      int i=0;
      for(String str:Labels){
          s[i++]=str;
      }
      return s;
   }

    public static Class[] GetTypes(){
        Class s[]=new Class[TYPES.size()];
        int i=0;
        for(Class cls:TYPES){
            s[i++]=cls;
        }
        return s;
    }

    private static Class GetType(String lastString) throws EOFException {
        Pattern pattern=Pattern.compile("(\")(.*?)(\")");
        Matcher matcher =pattern.matcher(lastString);
        int count=0;
        while (matcher.find())
        {
            if(count==0){count++;continue;}

            return GetClass(new StringBuilder(matcher.group(2)).delete(0,3).toString());
        }
        throw new EOFException("ERROR FORMAT XSD FILE");
  }

    private static Class GetClass(String group) throws EOFException {
        switch (group)
        {
            case "string":return String.class;
            case "int":return Integer.class;
            case "boolean":return Boolean.class;
        }
        System.out.println(group);
        throw new EOFException("ERROR FORMAT XSD FILE");
    }


    private static String GetName(String lastString) throws EOFException
    {
        Pattern pattern=Pattern.compile("(\")(.*?)(\")");
        Matcher matcher =pattern.matcher(lastString);
        if(matcher.find())
        {
            return (matcher.group(2));
        }
        throw new EOFException("ERROR FORMAT XSD FILE");
    }

    private static String getEndXsd()
    {
      return  "      </xs:sequence>\n </xs:complexType> \n</xs:schema>" ;
    }

    private static String getDataXsd(String[] labels, Class[] types) throws Exception
    {
      StringBuilder stringBuilder =new StringBuilder("");
      for(int i=0;i<labels.length;i++)
      {
          String s=getRecord(labels[i],types[i]);
          stringBuilder.append(s);
      }
       return stringBuilder.toString();
    }

    private  static String getRecord(String label, Class type) throws Exception
    {
      return new StringBuilder().append("        <xs:element name =\"").append(label).append( "\" type =\"").append(getclass(type)).append("\"/>\n").toString();
    }

    private static  String getclass(Class type) throws Exception
    {
      if(type==String.class)return "xs:string";
      if(type==Integer.class||type==int.class)return "xs:int";
      if(type==Boolean.class||type==boolean.class)return "xs:boolean";
      throw new Exception("not supported type");
    }
    private  static  String getBeginOfXsd(String DB_name , String table)
    {
    StringBuilder Str =new StringBuilder("<?xml version = \"1.0\"?>\n   ");
    Str.append("<xs:schema xmlns:xs = \"http://www.w3.org/2001/XMLSchema\">\n");
    Str.append( "   <xs:element name = '"+ DB_name+ "'>\n");
    Str.append("         <xs:complexType>\n              <xs:sequence>\n");
    Str.append("                  <xs:element name = '"+table+"' type = '"+table+"' minOccurs = '0'  maxOccurs = 'unbounded' />\n ");
    Str.append("             </xs:sequence>\n" + "         </xs:complexType>\n" + "   </xs:element>\n\n");
    Str.append("<xs:complexType name = '"+table+"'>\n"+"      <xs:sequence>\n");
    return Str.toString();

  }
}
