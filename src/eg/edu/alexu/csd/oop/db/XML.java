package eg.edu.alexu.csd.oop.db;

import org.w3c.dom.*;

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
import java.util.List;

public class XML {
    public boolean convertIntoXml(LinkedList<LinkedList> data, String DataBaseName, String TableName, String[]Labels) {

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
                Attr attr = doc.createAttribute(Labels[numberOfColumn]);
                attr.setValue(String.valueOf(object));
                row.setAttributeNode(attr);
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
        File file = new File("DataBase");
        file.mkdirs();
        file = new File("Database\\" + DataBaseName);
        file.mkdirs();
        file = new File("Database\\" + DataBaseName + "\\" + TableName);
        file.mkdirs();

        StreamResult result = new StreamResult(new File("Database\\" + DataBaseName + "\\" + TableName + "\\" + TableName + ".xml"));
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public LinkedList<LinkedList> convertFromXml(String dataBase, String table, Class[] types,String[] Labels) {
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
                for (int j = 0; j < Labels.length; j++) {
                    String str = eElement.getAttribute(Labels[j]);
                    if (str.equals("null")) {
                        row.add(null);
                    } else if (types[j] == String.class) {
                        row.add(str);
                    } else if (types[j] == int.class) {
                        row.add(Integer.parseInt(str));
                    } else if (types[j] == boolean.class) {
                        row.add(Boolean.parseBoolean(str));
                    } else if (types[j] == float.class) {
                        row.add(Float.parseFloat(str));
                    } else if (types[j] == Double.class) {
                        row.add(Double.parseDouble(str));
                    } else {
                        throw new Exception("not supported type");
                    }

                    System.out.println(str);
                }
                data.add(row);

            }

        } catch (Exception e) {
            System.out.println(e);
        }
        return data;
    }
}