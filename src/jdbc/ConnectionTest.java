package jdbc;


import java.sql.DriverManager;
import java.sql.SQLException;

import junit.framework.Assert;
import junit.framework.TestCase;


/*
 * package dbms;

import junit.framework.TestCase;

public class tester extends TestCase {

	public tester(String name) {
		super(name);
		assertEquals(expected, actual)
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}

 */

public class ConnectionTest {
	Connection connection;
	Statement statement;
	
		
	public void setUp() throws SQLException{
		String url = "jdbc:normandy2:../Dumpster/DemoDB";
		try {
			Class.forName("normandy2.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: ");
			System.err.println(e.getMessage());
		}

		connection = (Connection) DriverManager.getConnection(url, "mkatri", "fisherman");
	}
	
	public void testClose() throws SQLException {
		try {
			connection.close();
		} catch (SQLException e) {
 			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Assert.assertEquals(null, connection.createStatement());
	}

	public void testCreateStatement() {
		try {
			Assert.assertNotNull(connection.createStatement());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testCheckIdentity2(){
		try {
			connection.getIdentityMatrix(new String[]{"authors"}, new String[]{"\"authors.id\"", "id"});
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
