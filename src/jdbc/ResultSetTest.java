package jdbc;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

import junit.framework.Assert;

import dbms.TableMeta;


public class ResultSetTest {

	Connection con;
	Statement stmt;
	String select = "select id,first_name,last_name,birth_date from authors";
	ResultSet result;

	public void setUp() throws Exception {
		String url = "jdbc:normandy2:../Dumpster/DemoDB";
		try {
			Class.forName("normandy2.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: ");
			System.err.println(e.getMessage());
		}

		con = DriverManager.getConnection(url, "mkatri", "fisherman");
		stmt = (Statement) con.createStatement();
		result = stmt.executeQuery(select);
	}

	public void testAbsolute() throws Exception {
		result.absolute(-1);
		Assert.assertEquals(2, result.getCursor());
		result.absolute(2);
		Assert.assertEquals(2, result.getCursor());
		result.absolute(-100);
		Assert.assertEquals(0, result.getCursor());
	}

	
	public void testAfterLast() throws Exception {
		result.afterLast();
		Assert.assertEquals(3, result.getCursor());
	}

	
	public void testBeforeFirst() throws Exception {
		result.setCursor(200);
		result.beforeFirst();
		Assert.assertEquals(0, result.getCursor());
	}

	public void testFindColumn() throws Exception {
		int i = result.findColumn("first_name");
		Assert.assertEquals(2, i);
		Assert.assertEquals(3, result.findColumn("last_name"));
	}
//insert into table (col1, col2) values (val1, val2)
	public void testFirst() throws Exception {
		result.setCursor(500);
		result.first();
		Assert.assertEquals(1, result.getCursor());

	}

	public void testGetBooleanInt() throws Exception {
		result.first();
		Assert.assertEquals(false, result.getBoolean(1));

	}

	
	public void testGetBooleanString() throws Exception {
		result.first();
		Assert.assertEquals(false, result.getBoolean("first_name"));
	}

	public void testGetDateInt() throws Exception {
		result.first();
		java.sql.Date date = result.getDate(4);
		String s = "1911-12-11";
		DateFormat formatter = new SimpleDateFormat("yy-MM-dd");
		java.util.Date dat = formatter.parse(s);
		long longDtae = dat.getTime();
		java.sql.Date dats = new java.sql.Date(longDtae);
		Assert.assertEquals(dats, date);
	}

	public void testGetDateString() throws Exception {
		result.first();
		java.sql.Date date = result.getDate("birth_date");
		String s = "1911-12-11";
		DateFormat formatter = new SimpleDateFormat("yy-MM-dd");
		java.util.Date dat = formatter.parse(s);
		long longDtae = dat.getTime();
		java.sql.Date dats = new java.sql.Date(longDtae);

		Assert.assertEquals(dats, date);
	}

	public void testGetDoubleInt() throws Exception {
		result.first();
		double number = 1;
		Assert.assertEquals(number, result.getDouble(1), 0.001);
	}

	
	public void testGetDoubleString() throws Exception {
		result.first();
		double number = 1;
		Assert.assertEquals(number, result.getDouble("id"), 0.001);
	}

	
	public void testGetFloatInt() throws Exception {
		result.first();
		float number = 1;
		Assert.assertEquals(number, result.getFloat(1), 0.001);
	}

	
	public void testGetFloatString() throws Exception {
		result.first();
		float number = 1;
		Assert.assertEquals(number, result.getDouble("id"), 0.001);
	}

	
	public void testGetIntInt() throws Exception {
		result.first();
		Assert.assertEquals(1, result.getInt(1));
	}

	
	public void testGetIntString() throws Exception {
		result.first();
		Assert.assertEquals(1, result.getInt("id"));
	}

	
	public void testGetLongInt() throws Exception {
		String select = "select id from tableLong";
		String insert = "insert into tableLong (id) values ( '100' )";
		//create table (id BIGINT [null])
		 //stmt.execute("create table tableLong (id BIGINT [null])");
		stmt.execute(insert);
		// stmt.execute(update);
		ResultSet rs = stmt.executeQuery(select);
		rs.first();
		long l = rs.getLong(1);
		long l2 = 100;
		Assert.assertEquals(l2, l);
	}

	
	public void testGetLongString() throws Exception {
		String select = "select id from tableLong";
		String insert = "insert into tableLong (id) values ( '100' )";

		// stmt.execute("create table testLong (id BIGINT)");
		stmt.execute(insert);
		// stmt.execute(update);
		ResultSet rs = stmt.executeQuery(select);
		rs.first();
		long l = rs.getLong("id");
		long l2 = 100;
		Assert.assertEquals(l2, l);
	}

	
	public void testGetObjectInt() throws Exception {
		result.first();
		Object o = result.getObject(4);
		String s = "1911-12-11";
		DateFormat formatter = new SimpleDateFormat("yy-MM-dd");
		java.util.Date dat = formatter.parse(s);
		long longDtae = dat.getTime();
		java.sql.Date date = new java.sql.Date(longDtae);
		Assert.assertEquals(date.toString(), o.toString());
		Object o2 = result.getObject(1);
		Integer expected = new Integer(1);
		Assert.assertEquals(expected, o2);
	}

	
	public void testGetStatement() throws Exception {
		Assert.assertEquals(stmt, result.getStatement());
	}

	
	public void testGetStringInt() throws Exception {
		String s = "Naguib";
		result.first();
		String s2 = result.getString(2);
		Assert.assertEquals(s, s2);
	}

	
	public void testGetStringString() throws Exception {
		String s = "Naguib";
		result.first();
		String s2 = result.getString("first_name");
		Assert.assertEquals(s, s2);
	}

	
	public void testIsAfterLast() throws Exception {
		result.first();
		Assert.assertEquals(false, result.isAfterLast());
		result.afterLast();
		Assert.assertEquals(true, result.isAfterLast());
	}

	
	public void testIsBeforeFirst() throws Exception {
		result.first();
		Assert.assertEquals(false, result.isBeforeFirst());
		result.beforeFirst();
		Assert.assertEquals(true, result.isBeforeFirst());
	}

	
	public void testIsClosed() throws Exception {
		Assert.assertEquals(false, result.isClosed());
		result.close();
		Assert.assertEquals(true, result.isClosed());
	}

	
	public void testIsFirst() throws Exception {
		result.setCursor(9);
		Assert.assertEquals(false, result.isFirst());
		result.first();
		Assert.assertEquals(true, result.isFirst());
	}

	
	public void testIsLast() throws Exception {
		result.setCursor(9);
		Assert.assertEquals(false, result.isLast());
		result.last();
		Assert.assertEquals(true, result.isLast());
	}

	
	public void testLast() throws Exception {
		result.last();
		Assert.assertEquals(2, result.getCursor());
	}

	
	public void testNext() throws Exception {
		result.setCursor(1);
		result.next();
		Assert.assertEquals(2, result.getCursor());
	}

	
	public void testPrevious() throws Exception {
		result.setCursor(7);
		result.previous();
		Assert.assertEquals(6, result.getCursor());
	}
		
	public void testClose() throws Exception {
		result.close();
		String tab[][] = result.getTable();
		ResultSetMetaData met = result.getMetaData();
		Assert.assertEquals(null, result.getStatement());
		Assert.assertEquals(null, tab);
		Assert.assertEquals(null, met);

	}
}
