package jdbc;


import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import junit.framework.Assert;


public class DriverTest {
	Driver driver = new Driver();	

	public void testAcceptsURL() {
		String urlTrue = "jdbc:normandy2:some/Path/to/Database";
		String urlFalse = "jdbc:normandy:invalid:path";
		try {
			Assert.assertEquals(true, driver.acceptsURL(urlTrue));
			Assert.assertEquals(false, driver.acceptsURL(urlFalse));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void testConnect() {
		String url = "jdbc:normandy2:../Dumpster/DemoDB";
		Properties info = new Properties();
		info.put("user", "mkatri");
		info.put("password", "fisherman");
		try {
			Assert.assertEquals(Connection.class, driver.connect(url, info).getClass());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean test = false;
		try{
			url = "jdbc:normandy2:some/Path/to/Database";
			driver.connect(url, info);			
		}catch(SQLException e){
			test = true;
		}
		
		Assert.assertTrue(test);
	}


	
	public void testGetPropertyInfo() {
		Properties info = new Properties();
		String url = "jdbc:normandy2:some/Path/to/Database";
		DriverPropertyInfo[] expected = new DriverPropertyInfo[2];
		expected[0] = new DriverPropertyInfo("user", "");
		expected[1] = new DriverPropertyInfo("password", "");
		
		try {
			DriverPropertyInfo[] test = driver.getPropertyInfo(url, info);
			Assert.assertEquals(expected.length, test.length);
			for(int i = 0; i < expected.length; i++){
				Assert.assertEquals(test[i].name, expected[i].name);
				Assert.assertEquals(test[i].value, expected[i].value);
			}
				
			//assertArrayEquals(expected, driver.getPropertyInfo(url, info));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
