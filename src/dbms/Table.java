package dbms;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.transform.Result;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class Table {
	// String to hold the name of the file
	String name;
	File xmlFile;
	String metaFile;
	String dtdFile;

	/*
	 * Constructor that take a string which is the name of the file then assign
	 * it to the variable name
	 */
	public Table(String dbDir, String tableName) {
		name = tableName;
		xmlFile = new File(dbDir + "/tables/" + name + ".xml");
		// create the name of the meta data file
		// TODO this is to be modified
		metaFile = dbDir + "/tables_meta/" + name + ".meta.xml";
		dtdFile = dbDir + "/tables/" + name + ".dtd";
	}

	public static Table createFromMeta(String dbDir, TableMeta table) {
		try {
			String tName = table.getName();
			File tFile = new File(dbDir + "/tables/" + tName + ".xml");
			tFile.createNewFile();

			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = fact.newDocumentBuilder();
			Document doc = parser.newDocument();
			//doc.setd

			Node root = doc.createElement(tName);
			root.setNodeValue(tName);
			doc.appendChild(root);
			Node auto = doc.createElement("auto");
			auto.setTextContent("1");
			root.appendChild(auto);
			Table t = new Table(dbDir, tName);
			t.writeDOMTable(doc);
			createMetaFile(dbDir, table);
			createDTDFile(dbDir, table);
			return t;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static void createDTDFile(String dbDir, TableMeta tab)
			throws IOException {
		StringBuilder dtdBuff = new StringBuilder();
		StringBuilder buff = new StringBuilder();

		dtdBuff.append("<!ELEMENT " + tab.getName() + " (row*, auto)>\n");
		dtdBuff.append("<!ELEMENT row (");
		String[] cols = tab.getColumnNames();
		for (int i = 0; i < cols.length; i++) {
			dtdBuff.append(cols[i]);
			buff.append("<!ELEMENT " + cols[i] + " (#PCDATA)>");
			if (i != cols.length - 1) {
				dtdBuff.append(", ");
				buff.append("\n");
			}

		}
		dtdBuff.append(")>\n");
		dtdBuff.append("<!ELEMENT auto (#PCDATA)>\n");
		dtdBuff.append(buff);
	
		FileWriter fstream = new FileWriter(dbDir + "/tables/" + tab.getName() + ".dtd");
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(dtdBuff.toString());
		out.close();
	}

	public static void createMetaFile(String dbDir, TableMeta tab)
			throws Exception {

		String tName = tab.getName();
		File mFile = new File(dbDir + "/tables_meta/" + tName + ".meta.xml");
		mFile.createNewFile();

		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = fact.newDocumentBuilder();
		Document doc = parser.newDocument();

		Node root = doc.createElement("metadata");
		root.setNodeValue("metadata");
		doc.appendChild(root);

		Node one = doc.createElement("table");
		one.setTextContent(tName);
		root.appendChild(one);

		Node colsNode = doc.createElement("cols");
		String[][] array = tab.getInfoArray();
		for (int i = 0; i < array.length; i++) {
			if (array[i][0] == null)
				break;
			Node child = addColumn(array[i][0],
					Boolean.parseBoolean(array[i][2]), array[i][1], doc);
			colsNode.appendChild(child);
		}
		root.appendChild(colsNode);

		Node keys = doc.createElement("keys");
		// Node key = doc.createElement("key");
		String[] keyArray = tab.getKeyArray();
		for (int i = 0; i < keyArray.length; i++) {
			if (keyArray[i] == null)
				break;
			Node child = addKey(tab.getcolumnName(keyArray[i]),
					tab.getKeyName((keyArray[i])), doc);
			keys.appendChild(child);

		}
		root.appendChild(keys);

		if (tab.autoColumn() != null) {
			Element auto = doc.createElement("auto");
			Attr attribute = doc.createAttribute("col");
			attribute.setTextContent(tab.autoColumn());
			auto.setAttributeNode(attribute);
			root.appendChild(auto);
		}

		Source source = new DOMSource(doc);
		Result result = new StreamResult(mFile);
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.transform(source, result);

	}

	private static Node addKey(String name, String colName, Document doc) {
		Node myNode = doc.createElement("key");
		Node firstChildNode = doc.createElement("name");
		firstChildNode.setTextContent(name);
		Node secondChildNode = doc.createElement("cols");
		Node textNode = doc.createElement("col");
		textNode.setTextContent(colName);
		myNode.appendChild(firstChildNode);
		secondChildNode.appendChild(textNode);
		myNode.appendChild(secondChildNode);
		return myNode;
	}

	private static Element addColumn(String colName, boolean nullable,
			String type, Document doc) {
		Attr attribute1 = doc.createAttribute("name");
		attribute1.setTextContent(colName);
		Attr attribute2 = doc.createAttribute("null");
		attribute2.setTextContent(Boolean.toString(nullable));
		Attr attribute3 = doc.createAttribute("type");
		attribute3.setTextContent(type);
		Element newElement = doc.createElement("col");
		newElement.setAttributeNode(attribute1);
		newElement.setAttributeNode(attribute2);
		newElement.setAttributeNode(attribute3);
		return newElement;
	}

	/*
	 * read the XML file using DOM and save the read data into a document then
	 * return it to the caller
	 */
	public Document readDOMTable() throws SAXException, IOException {
		// Create a dbFactory to instantiate the dbuilder which will parse the
		// XML file
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setValidating(true);
		// Create the dBuilder using the dbFactory
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			dBuilder.setErrorHandler(new ErrorHandler() {
				public void warning(SAXParseException e) throws SAXException {

				}

				public void error(SAXParseException e) throws SAXException {
					throw new SAXException("Unable to read table '" + name
							+ "', data is corrupt.");
				}

				public void fatalError(SAXParseException e) throws SAXException {
					throw new SAXException("Unable to read table '" + name
							+ "', data is corrupt.");
				}
			});
			// Creating the document using the parse method in the dBuilder
			Document doc = dBuilder.parse(xmlFile);
			return doc;
		} catch (ParserConfigurationException e1) {
			throw new RuntimeException(e1.getMessage());
		}

	}

	// This method writes a DOM document to a file
	public boolean writeDOMTable(Document table) throws Exception {
		boolean flag = false;
		if (!flag) {
			Source source = new DOMSource(table);
			Result result = new StreamResult(xmlFile);
			Transformer xformer = TransformerFactory.newInstance()
					.newTransformer();
			xformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, name+".dtd");
			xformer.transform(source, result);			
			flag = true;
		}
		return flag;
	}

	// This method is to get the information from the metaData file
	public TableMeta getMetaData() {
		//
		TableMeta metaObject = new TableMeta(name, this);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		// Create the dBuilder using the dbFactory
		DocumentBuilder dBuilder;
		Document doc;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(new File(metaFile));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Fatal Error: " + e.getMessage());
		}
		// Creating the document using the parse method in the dBuilder

		// create a node list from the children of the meta data
		NodeList nList = (doc.getElementsByTagName("metadata")).item(0)
				.getChildNodes();
		// create a two dimensional array to hold the info of the file
		String[][] metaArray;
		ArrayList<String[]> ta = new ArrayList<String[]>();
		// loop through the list and extract the information from the file
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				if (eElement.getNodeName() == "cols") {
					NodeList n = eElement.getChildNodes();
					for (int i = 0; i < n.getLength(); i++) {
						if (n.item(i).hasAttributes()) {
							String[] nmeta = new String[3];
							NamedNodeMap nnMap = n.item(i).getAttributes();
							// Get the attribute tagged with name
							Node mynNode = nnMap.getNamedItem("name");
							nmeta[0] = mynNode.getTextContent();
							// get the attribute tagged with null
							Node mynNode2 = nnMap.getNamedItem("null");
							nmeta[2] = mynNode2.getTextContent();
							// get the attribute tagged with type
							Node mynNode3 = nnMap.getNamedItem("type");
							nmeta[1] = mynNode3.getTextContent();
							ta.add(nmeta);
							metaObject.arrayIndex++;
						}
					}
				}
				/*
				 * if (eElement.getNodeName() == "keys") {
				 * metaObject.setAutoColumn(getTagValue("name", eElement));
				 * metaObject.addKeyArray(getTagValue("name", eElement),
				 * getTagValue("col", eElement)); }
				 */
				if (eElement.getNodeName() == "auto") {
					metaObject.setAutoColumn(eElement.getAttribute("col"));
				}
			}
		}
		//
		metaArray = new String[ta.size()][];
		ta.toArray(metaArray);
		metaObject.setInfoArray(metaArray);
		return metaObject;
	}

	public void addKey(String name, String colName) throws Exception {
		Document doc = readDOMTable();
		Node element = doc.getElementsByTagName("keys").item(0);
		Node myNode = addKey(name, colName, doc);
		element.appendChild(myNode);
		writeDOMTable(doc);
	}

	public void addColumn(String colName, boolean nullable, String type)
			throws Exception {
		Document doc = readDOMTable();
		Node element = doc.getElementsByTagName("cols").item(0);
		Element newElement = addColumn(colName, nullable, type, doc);
		element.appendChild(newElement);
		writeDOMTable(doc);

	}

	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();
		Node nValue = (Node) nlList.item(0);
		return nValue.getNodeValue();

	}

	public void delete() {
		xmlFile.delete();
		(new File(metaFile)).delete();
		(new File(dtdFile)).delete();
	}
}
