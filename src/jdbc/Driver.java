package jdbc;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public class Driver implements java.sql.Driver {
	static {
		try {
			DriverManager.registerDriver((Driver) Class.forName(
					"normandy2.jdbc.Driver").newInstance());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param url
	 *            - the URL of the database
	 * @return Retrieves whether the driver thinks that it can open a connection
	 *         to the given URL
	 */
	@Override
	public boolean acceptsURL(String url) throws SQLException {
		String[] s = url.split(":");
		return (s.length == 3 && s[0].compareTo("jdbc") == 0
				&& s[1].compareTo("normandy2") == 0 && (new File(s[2]) != null));
	}

	/**
	 * @param url
	 *            - the URL of the database
	 * @param info
	 *            - a list of arbitrary string tag/value pairs as connection
	 *            arguments. Normally at least a "user" and "password" property
	 *            should be included.
	 * @return a Connection object that represents a connection to the URL
	 * @throws SQLException
	 *             - if a database access error occurs
	 */
	public Connection connect(String url, Properties info) throws SQLException {
		if (!acceptsURL(url))
			return null;
		/* waiting for more information about connetion */
		DriverPropertyInfo[] miss = getPropertyInfo(url, info);
		if (miss.length != 0)
			throw new SQLException(
					"Missing Connection Arguments, username and password.");
		try {
			return jdbc.Connection.createConnection(
					url.split(":")[2], info.getProperty("user"),
					info.getProperty("password"));
		} catch (SQLException e) {
			/* erorr accessing database */
			throw e;
		} catch (IOException e) {
			/* if there is error in the path */
			throw new SQLException(
					"Database doesn't exist or its structure is corrupt.");
		}
	}

	/**
	 * get Property info , gets what is needed and not found in properties
	 * 
	 * @param url
	 *            - the URL of the database to which to connect info - a
	 *            proposed list of tag/value pairs that will be sent on connect
	 *            open
	 * @return an array of DriverPropertyInfo objects describing possible
	 *         properties. This array may be an empty array 'length = 0' if no
	 *         properties are required.
	 * @throws SQLException
	 *             - if a database access error occurs
	 * 
	 */
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		String name = info.getProperty("user");
		String password = info.getProperty("password");

		/* the */
		if (name != null && name.length() != 0 && password != null
				&& password.length() != 0)
			return new DriverPropertyInfo[0];
		else if (password != null && password.length() != 0)
			return new DriverPropertyInfo[] { new DriverPropertyInfo(
					"user", "") };
		else if (name != null && name.length() != 0)
			return new DriverPropertyInfo[] { new DriverPropertyInfo(
					"user", "") };
		else
			return new DriverPropertyInfo[] { new DriverPropertyInfo(
					"user", ""), new DriverPropertyInfo("password", "") };
	}

	@Override
	public boolean jdbcCompliant() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

}
