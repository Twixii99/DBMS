package eg.edu.alexu.csd.oop.db;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.LinkedList;

public class XML {
    public static boolean convertIntoXml(LinkedList<LinkedList> data, String DataBaseName, String TableName, String[]Labels) {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return false;
        }
        Document doc = dBuilder.newDocument();
        Element rootElement = doc.createElement(DataBaseName);
        doc.appendChild(rootElement);

        for (LinkedList linkedList : data) {
            Element row = doc.createElement(TableName);
            rootElement.appendChild(row);
            int numberOfColumn = 0;
            for (Object object : linkedList) {
                Element element = doc.createElement(Labels[numberOfColumn]);
                element.appendChild(doc.createTextNode(String.valueOf(object)));
                row.appendChild(element);
                numberOfColumn++;
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            return false;
        }
        DOMSource source = new DOMSource(doc);

        MakeDirectory(DataBaseName,TableName);
        StreamResult result = new StreamResult(new File("Database\\" + DataBaseName + "\\" + TableName + "\\" + TableName + ".xml"));
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static LinkedList<LinkedList> convertFromXml(String dataBase, String table, Class[] types) {
        LinkedList<LinkedList> data = new LinkedList<>();
        try {
            File file = new File("Database\\" + dataBase + "\\" + table + "\\" + table + ".xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName(table);
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                LinkedList<Object> row = new LinkedList<>();
                Element eElement = (Element) nNode;
                NodeList nodeList =eElement.getChildNodes();
                for (int j = 0; j < eElement.getChildNodes().getLength(); j++) {
                    String str = nodeList.item(j).getTextContent();
                   if (types[j] == String.class) {
                        row.add(str);
                    } else if (types[j] == int.class||types[j] == Integer.class) {
                        row.add(Integer.parseInt(str));
                    } else if (types[j] == boolean.class||types[j] == Boolean.class) {
                        row.add(Boolean.parseBoolean(str));
                    } else {
                        throw new Exception("not supported type");
                    }

                }
                data.add(row);
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return data;
    }

    public static void MakeDirectory( String DataBaseName , String  TableName){
        File file;
        if(System.getProperty("DataBase")==null) {
            file = new File("DataBase");
            file.mkdirs();
        }
        if(System.getProperty("Database\\" + DataBaseName)==null) {
            file = new File("Database\\" + DataBaseName);
            file.mkdirs();

        }
        if(System.getProperty("Database\\" + DataBaseName + "\\" + TableName)==null) {
            file = new File("Database\\" + DataBaseName + "\\" + TableName);
            file.mkdirs();
        }
    }
}