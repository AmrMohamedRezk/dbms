package jdbc;


import org.apache.log4j.net.SMTPAppender;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import junit.framework.Assert;



public class StatementTest {
	Connection con;
	Statement stmt;
	String select="select first_name, nationality,birth_date from authors";
	String update="update authors set last_name = 'Rezk' where first_name = 'Amr'";
	String insert="insert into authors (first_name) values ( 'Amr' )";
	String delete ="delete from authors where first_name = 'Amr'";
	public void setUp() throws Exception {
		String url = "jdbc:normandy2:../Dumpster/DemoDB";
		try {
			Class.forName("normandy2.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: ");
			System.err.println(e.getMessage());
		}
		try {
			con = (Connection) DriverManager.getConnection(url, "mkatri", "fisherman");
			stmt = (Statement) con.createStatement();
			ResultSet rs;
			//rs = stmt.executeQuery("select first_name, nationality,birth_date from authors ");
			//stmt.execute("update authors set last_name = fawzy where first_name = mo7ssen");
				} catch (SQLException ex) {
			ex.printStackTrace();
		}
		

	}

	public void testAddBatch() {
		try {
			stmt.addBatch("select first_name, nationality,birth_date from authors ");
			stmt.addBatch("insert into authors (first_name) values ( Amr )");
			stmt.addBatch("update authors set last_name = Rezk where first_name = 'Amr'");
			ArrayList<String> list = new ArrayList<String>();
			list.add("select first_name, nationality,birth_date from authors ");
			list.add("insert into authors (first_name) values ( Amr )");
			list.add("update authors set last_name = Rezk where first_name = 'Amr'");
			for(int i=0;i<list.size();i++)
				Assert.assertEquals(list.get(i), stmt.list.get(i));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void testClearBatch() {
		try {
			stmt.clearBatch();
			Assert.assertEquals(0, stmt.list.size());

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void testExecuteString() {
		try {
			boolean flag1=stmt.execute(select);
			boolean flag2=stmt.execute(update);
			boolean flag3=stmt.execute(insert);
			boolean flag4=stmt.execute(delete);
			Assert.assertEquals(true, flag1);
			Assert.assertEquals(false, flag2);
			Assert.assertEquals(false, flag3);	
			Assert.assertEquals(false, flag4);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testExecuteBatch() throws Exception
	{
			stmt.addBatch(insert);
			stmt.addBatch(update);
			stmt.addBatch("insert into authors (first_name) values ( 'Mohamed' )");
			stmt.addBatch("update authors set last_name = 'fawzy',nationality='Egyptian' where first_name = 'Mohamed'");
			int tester [] = {1,1,1,1};
			int result[] = stmt.executeBatch();
			int j = 0;
			for(int i :tester)
				Assert.assertEquals(i, result[j++]);
			cleanUpAfterExecuteBatch();
	}

	public  void cleanUpAfterExecuteBatch()throws Exception
	{
		String delete ="delete from authors where first_name = 'Amr'";
		stmt.execute(delete);
		stmt.execute("delete from authors where first_name = 'Mohamed'");
	}


	
	public void testExecuteQuery() throws Exception {
		String select ="select first_name from authors";
		ResultSet rs =stmt.executeQuery(select);
		rs.first();
		Assert.assertEquals(rs.getString(1),"Naguib");
		rs.last();
		Assert.assertEquals(rs.getString(1),"William");
	}

	public void testExecuteUpdateString() throws Exception {
		int n1=stmt.executeUpdate(insert);
		String select ="select first_name from authors";
		ResultSet rs =stmt.executeQuery(select);
		Assert.assertEquals(1, n1);
		rs.last();
		Assert.assertEquals(rs.getString(1), "Amr");

		int n = stmt.executeUpdate(update);
		String select2 ="select first_name,last_name from authors";
		 rs =stmt.executeQuery(select2);
		 Assert.assertEquals(1, n);
		rs.last();
		Assert.assertEquals(rs.getString(1), "Amr");
		Assert.assertEquals(rs.getString(2), "Rezk");
		String delete ="delete from authors where first_name = 'Amr'";
		int n2 = stmt.executeUpdate(delete);
		rs =stmt.executeQuery(select);
		Assert.assertEquals(1, n2);
		rs.last();
		Assert.assertEquals(rs.getString(1), "William");
		
	}


	
	public void testGetConnection() throws Exception{
		Assert.assertEquals(con, stmt.getConnection());
	}

	
	public void testClose() {
		try {
			stmt.close();	
			Assert.assertEquals(null, stmt.connection);
			Assert.assertEquals(null, stmt.list);
			} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
/*
	@Test
	public void testGetQueryTimeout() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetQueryTimeout() {
		fail("Not yet implemented");
	}
*/
}
