package dbms;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Hashtable;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jdbc.Connection;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CRUD {

	private Connection connection;

	// TODO handle insertion into auto column
	/**
	 * @param table
	 *            The table to process
	 * @param XPath
	 *            Xpath state the select nodes
	 * @param columnsNum
	 *            number of columns to select
	 * @return The selected entries and will return row with no entries if none
	 *         was selected
	 */

	public CRUD(Connection connection) {
		this.connection = connection;
	}

	public String[][] select(Table table, String[] columns, String XPath) {
		Logger logger = connection.getLogger();
		// parse the Xpath and get the selected nodes and then add them to the
		// array
		// use hash to know the column that was selected
		// iterate the node list --> get child --> hash.put(name)
		// Then of any one don't have a value for the hash entries set ""
		try {

			Document doc = table.readDOMTable();
			doc.normalizeDocument();
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr;
			// Executing Xpath
			expr = xpath.compile(XPath);
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;

			// result array
			// row
			String[][] selection = new String[nodes.getLength()][columns.length];
			// Initializing selection array
			for (String[] a : selection)
				Arrays.fill(a, "");

			Hashtable<String, Integer> hash = new Hashtable<String, Integer>();

			for (int i = 0; i < columns.length; i++) {
				String col = columns[i];
				if (col.charAt(0) == '\"'
						&& col.charAt(col.length() - 1) == '\"'
						|| col.charAt(0) == '\''
						&& col.charAt(col.length() - 1) == '\'') {
					// filling the selection array with the column name
					for (int j = 0; j < selection.length; j++)
						selection[j][i] = col.substring(1, col.length() - 1);
				} else
					hash.put(columns[i], i);
			}

			for (int i = 0; i < nodes.getLength(); i++) {
				// Getting All The rows that was Matched
				Node node = nodes.item(i);
				if (node != null) {
					NodeList rowEntries = node.getChildNodes(); // Columns
																// Entries
					for (int k = 0; k < rowEntries.getLength(); k++) {
						// Adding the new Entry in the selection Array
						Node entry = rowEntries.item(k);
						String s = entry.getNodeName();
						if (s.compareTo("#text") != 0) // avoiding internal text
						{
							Integer index = hash.get(s);
							if (index != null)
								selection[i][index] = entry.getTextContent();
						}
					}
				}
			}
			logger.info("Selecting Data from table " + table.name);
			return selection;
		} catch (Exception e) {
			logger.error("Error while Selecting Data from table "
					+ ((table.name != null) ? table.name : ""));
			throw new RuntimeException("Fatal Error: " + e.getMessage());
		}
	}

	/**
	 * @param table
	 *            The table to process
	 * @param cols
	 *            columns names of the rows inserted
	 * @param vals
	 *            values of rows to be inserted
	 * @return inserted rows , return -1 if there are duplicated columns
	 * @throws Exception
	 */
	public int insert(Table table, String[] cols, String[][] vals) {
		Logger logger = connection.getLogger();
		/* updated .. removed validity checkers */
		try {
			Document doc = table.readDOMTable();
			doc.normalizeDocument();
			TableMeta meta = table.getMetaData();

			int colsLength = cols.length;

			int allColumns = meta.numberOfColumns();

			String autoCol = meta.autoColumn();
			int autoColIndex = (autoCol == null) ? (-1) : (meta
					.colIndex(autoCol));

			/*
			 * new method from TableMeta will be added by Amr ------ Note :
			 * should be updated when addColumn
			 */

			Element tableElement = doc.getDocumentElement();
			/* updating Auto 'removing it ,taking its value' */
			Node oldAuto = tableElement.removeChild(doc.getElementsByTagName(
					"auto").item(0));
			int nextId = Integer.parseInt(oldAuto.getTextContent());

			/* Adding new Rows --------------------------- */
			Element[] rows = new Element[vals.length];/* Elements of rows inserted */
			int rowsLength = rows.length;

			/* Append the rows Element [] to The doc */
			for (int i = 0; i < rowsLength; i++) {
				rows[i] = doc.createElement("row");
				tableElement.appendChild(rows[i]);
			}

			for (int i = 0; i < allColumns; i++) {
				int j = 0;
				for (; j < colsLength; j++) {
					/* there is column should be inserted with values */
					if (meta.colIndex(cols[j]) == i) {
						for (int k = 0; k < rowsLength; k++) {
							Element value = doc.createElement(cols[j]);
							value.appendChild(doc
									.createTextNode((vals[k][j] == null) ? ""
											: vals[k][j]));
							rows[k].appendChild(value);

							if (i == autoColIndex) {
								try {
									nextId = Math.max(nextId,
											Integer.parseInt(vals[k][j]) + 1);
								} catch (Exception e) {
								}
							}
						}
						break;
					}
				}

				/* there is no insert value for col i */
				if (j == colsLength) {
					if (i == autoColIndex) {
						for (int k = 0; k < rowsLength; k++) {
							Element value = doc.createElement(autoCol);
							value.appendChild(doc.createTextNode((nextId++)
									+ ""));
							rows[k].appendChild(value);
						}
					} else {
						for (int k = 0; k < rowsLength; k++) {
							Element value = doc.createElement(meta
									.colAtIndex(i));
							value.appendChild(doc.createTextNode(""));
							rows[k].appendChild(value);
						}
					}
				}
			}

			/* inserting auto tag */
			Element autoElement = doc.createElement("auto");
			autoElement.appendChild(doc.createTextNode(nextId + ""));
			tableElement.appendChild(autoElement);

			table.writeDOMTable(doc);
			logger.info("insert new " + rowsLength
					+ ((rowsLength > 1) ? " rows" : " row") + "in table "
					+ ((table != null) ? table.name : ""));
			return rowsLength;
		} catch (Exception e) {
			throw new RuntimeException("Fatal Error: " + e.getMessage());
		}
	}

	/**
	 * 
	 * @param table
	 * @param cols
	 * @param vals
	 * @param XPath
	 * @return number the of columns updated
	 * @throws throws exceptions if the column want to update wasn't found
	 */
	public int update(Table table, String[] cols, String[] vals, String XPath) {
		Logger logger = connection.getLogger();
		// NOTE : throw exceptions if the column want to update wasn't found
		// CHECK FROM META
		int updatedCols = 0;
		try {
			Document doc = table.readDOMTable();
			doc.normalizeDocument();
			XPathExpression xpath = XPathFactory.newInstance().newXPath()
					.compile(XPath);

			NodeList selectedNodes = (NodeList) xpath.evaluate(doc,
					XPathConstants.NODESET);

			for (int i = 0; i < selectedNodes.getLength(); i++) {
				// Columns of the row
				NodeList children = selectedNodes.item(i).getChildNodes();
				for (int j = 0, x = 0; j < children.getLength()
						&& x < cols.length; j++) {
					// Column
					updatedCols++;
					Node n = children.item(j);
					if (n.getNodeType() == Node.ELEMENT_NODE) {
						Element child = (Element) n;
						String colName = child.getNodeName();
						for (int y = 0; y < cols.length; y++) {
							if (cols[y].equals(colName)) {
								x++;
								child.setTextContent(vals[y]);
								break;
							}
						}
					}
				}
			}

			table.writeDOMTable(doc);
			int n = selectedNodes.getLength();
			logger.info("update " + n + ((n > 1) ? " columns " : " column ")
					+ "in table " + ((table != null) ? table.name : ""));
			return selectedNodes.getLength();
		} catch (Exception e) {
			return -1;
			// throw new RuntimeException("Fatal Error: " + e.getMessage());

		}
	}

	/**
	 * 
	 * @param table
	 * @param XPath
	 * @return number of the rows deleted
	 * @throws Exception
	 */

	public int detele(Table table, String XPath) {
		// Select the rows using the xpath and the delete it from the doc
		// then rewrite the doc in the file
		// return the number of the records removed
		// *Assumed the xpath won't contain row doesn't exist
		Logger logger = connection.getLogger();
		int deletedNodes;
		try {
			Document doc = table.readDOMTable();
			doc.normalizeDocument();
			XPathExpression xpath = XPathFactory.newInstance().newXPath()
					.compile(XPath);

			NodeList selectedNodes = (NodeList) xpath.evaluate(doc,
					XPathConstants.NODESET);

			deletedNodes = selectedNodes.getLength();
			if (deletedNodes == 0)
				return 0;

			for (int i = 0; i < deletedNodes; i++) {
				selectedNodes.item(i).getParentNode()
						.removeChild(selectedNodes.item(i));

			}

			table.writeDOMTable(doc);
			logger.info("Deleted " + deletedNodes
					+ ((deletedNodes > 1) ? " rows " : " row ") + "in table "
					+ ((table != null) ? table.name : ""));
		} catch (Exception e) {
			throw new RuntimeException("Fatal error: " + e.getMessage());
		}

		return deletedNodes;
	}
}
