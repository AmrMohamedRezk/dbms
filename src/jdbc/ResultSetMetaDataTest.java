package jdbc;


import org.apache.log4j.net.SMTPAppender;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import jdbc.Connection;
import jdbc.ResultSet;
import jdbc.Statement;
import junit.framework.Assert;



public class ResultSetMetaDataTest {
	ResultSetMetaData metaData;
	Connection con;
	Statement stmt;
	String select = "select id,first_name,last_name,birth_date,notable_award from authors";
	ResultSet result;

	public void setUp() throws Exception {
		String url = "jdbc:normandy2:../Dumpster/DemoDB";
		try {
			Class.forName("normandy2.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: ");
			System.err.println(e.getMessage());
		}

		con = (Connection) DriverManager.getConnection(url, "mkatri", "fisherman");
		stmt = (Statement) con.createStatement();
		result = stmt.executeQuery(select);
		metaData=(ResultSetMetaData) result.getMetaData();
	}
	public void testGetColumnCount() {
		int colCount = 5;
		try {
			Assert.assertEquals(colCount, metaData.getColumnCount());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testGetColumnLabel() {
		int index = 1;
		String label= "id";
		try {
			Assert.assertEquals(label, metaData.getColumnLabel(index));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testGetColumnName() {
		int index = 1;
		String label= "id";
		try {
			Assert.assertEquals(label, metaData.getColumnLabel(index));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testGetColumnType() {
		int type = 12;
		int index = 2;
		try {
			Assert.assertEquals(type, metaData.getColumnType(index));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void testGetTableName()throws Exception {
		Assert.assertEquals("authors", metaData.getTableName(0));	
	}

	
	public void testIsAutoIncrement() {
			boolean value = true;
			int index = 1;
			try {
				Assert.assertEquals(value, metaData.isAutoIncrement(index));
				Assert.assertEquals(false, metaData.isAutoIncrement(2));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}

	
	public void testIsNullable()throws Exception {
		Assert.assertEquals(1, metaData.isNullable(5));
		Assert.assertEquals(0, metaData.isNullable(2));
	}

	
	public void testIsReadOnly()throws Exception {
		Assert.assertEquals(false, metaData.isReadOnly(2));
	}

	public void testIsSearchable() throws Exception {
		Assert.assertEquals(true, metaData.isSearchable(2));
	}

	
	public void testIsWritable() throws Exception {
		Assert.assertEquals(true, metaData.isSearchable(2));
	}

}
