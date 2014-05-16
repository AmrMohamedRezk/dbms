package jdbc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import dbms.CRUD;
import dbms.Table;
import dbms.TableMeta;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Connection implements java.sql.Connection {
	Hashtable<String, TableMeta> metaData = new Hashtable<String, TableMeta>();
	Hashtable<String, Table> table = new Hashtable<String, Table>();

	Logger logger;
	CRUD operations;
	File DBDir;

	public Table[] getTables() {
		Table[] tables = new Table[table.size()];
		table.values().toArray(tables);
		return tables;
	}

	public TableMeta[] getTablesMeta() {
		TableMeta[] tables = new TableMeta[metaData.size()];
		metaData.values().toArray(tables);
		return tables;
	}

	/**
	 * Constructs a connection
	 * 
	 * @param folder
	 *            Database path
	 * @throws MalformedURLException
	 * @throws Exception
	 */
	private Connection(String folder) throws MalformedURLException {
		// Database Directory
		DBDir = new File(folder);
		File tableDir = new File(folder + "/tables");
		File metaDir = new File(folder + "/tables_meta");
		if (!DBDir.exists() | !tableDir.exists() | !metaDir.exists()) {
			logger.error("Invalid Databse");
			throw new MalformedURLException("Invalid Database");
		}

		File logFile = new File(folder + "/track.log");
		System.setProperty("log.file", logFile.getAbsolutePath());
		File logProp = new File(folder + "/normandy2log4j.properties");
		PropertyConfigurator.configure(logProp.getAbsolutePath());

		logger = Logger.getLogger(Connection.class);
		logger.info("Connection Request");

		File tableFiles[] = tableDir.listFiles();
		File metaFiles[] = metaDir.listFiles();
		HashSet<String> xmlFile = new HashSet<String>();
		String[] metaFile = new String[tableFiles.length];
		int meta = 0;
		for (int i = 0; i < tableFiles.length; i++) {
			// searching for xml and meta files
			String name = tableFiles[i].getName();

			if (name.endsWith(".xml")) {
				// file name without extension
				String f = name.substring(0, name.indexOf(".xml"));
				if (xmlFile.contains(f)) {
					logger.error("Duplicate Table Name");
					throw new MalformedURLException(
							"Duplicate Table Name(tables with the same name were found)");
				}
				xmlFile.add(f);
			}
		}

		for (int i = 0; i < metaFiles.length; i++) {
			String name = metaFiles[i].getName();
			if (name.endsWith(".meta.xml")) {
				// file name without extension
				String f = name.substring(0, name.indexOf(".meta.xml"));
				metaFile[meta++] = f;
				/*
				 * if (xmlFile.contains(f)) throw new MalformedURLException(
				 * "Table with duplicate Meta files was found");
				 */
			}
		}

		// missing files or no files
		if (meta != xmlFile.size() || meta == 0) {
			logger.error("Missing some Database Files");
			throw new MalformedURLException("Missing files");
		}
		for (int i = 0; i < meta; i++) {
			if (!xmlFile.contains(metaFile[i])) {
				logger.error("Table Without metadata file!");
				throw new MalformedURLException("Missing files");
			} else {
				// This table is valid -----> DO LOGIC
				Table t = new Table(DBDir.getPath(), metaFile[i]);
				metaData.put(metaFile[i], t.getMetaData());
				table.put(metaFile[i], t);
			}
		}

		operations = new CRUD(this);

		logger.info("Successfully connected to Database");
		/*
		 * Cases: ------------- 1- no 2 tables with the same name 2- no table
		 * without meta ----- or more than one meta 3- no meta without table
		 */
	}

	public boolean addTable(Table table) {
		TableMeta meta;
		try {
			meta = table.getMetaData();
			metaData.put(meta.tableName(), meta);
			this.table.put(meta.tableName(), table);
			return true;
		} catch (Exception e) {

		}

		return false;
	}

	/**
	 * Creates connection
	 * 
	 * @param path
	 *            Database Path
	 * @param username
	 *            login name
	 * @param password
	 *            login password
	 * @return the connection that has been created
	 * @throws Exception
	 */
	public static Connection createConnection(String path, String username,
			String password) throws SQLException, IOException {

		// TODO log this
		// TODO pass username to connection

		if (hasAccess(path, username, password) == 0)
			return new Connection(path); // Accepted username and password
		else {
			// didn't match any username or password
			// logger.error("Incorrect username and password");
			throw new SQLException(
					"Incorrect username and password combination");
		}
	}

	protected static int hasAccess(String url, String user, String password)
			throws IOException {
		File dir = new File(url);
		if (!dir.exists())
			throw new IOException("Database doesn't exist");
		// search for the users file and verify this user name and password
		/*
		 * User file is called "users" format is User1:password1 User2:password2
		 * User3:password3
		 */
		File[] files = dir.listFiles();
		File users = null;
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().compareTo("users") == 0) {
				users = files[i];
				break;
			}
		}

		if (users == null)
			throw new IOException("Database structure missing users list");

		BufferedReader br = new BufferedReader(new FileReader(users));
		while (br.ready()) {
			String[] line = br.readLine().split(":");
			if (line[0].equals(user))
				if (line[1].equals(password))
					return 0;
				else
					return 1; // Accepted username and password
		}

		return 2;
		// didn't match any username or password
	}

	/**
	 * 
	 * @param tableName
	 *            name of the table wants to get it's meta data
	 * @return table Meta Data
	 */
	public TableMeta getTableMeta(String tableName) {
		if (metaData.containsKey(tableName))
			return metaData.get(tableName);
		else {
			logger.warn(tableName + " meta was not Found");
			return null;
		}
	}

	public Table getTable(String tableName) {
		if (table.containsKey(tableName))
			return table.get(tableName);
		else {
			logger.warn(tableName + " was not Found");
			return null;
		}
	}

	public boolean tableExists(String tableName) {
		return table.containsKey(tableName);
	}

	/**
	 * closes connection
	 */
	public void close() throws SQLException {
		logger.info("Closing Connection with Databse");
		metaData = null;
	}

	@Override
	public Statement createStatement() throws SQLException {
		// Statement Class should have this constructor yet
		return (metaData == null) ? (null)
				: (new jdbc.Statement(this));
	}

	/**
	 * checks whether there's a table with the specified columns or not
	 * 
	 * @param tables
	 * @param cols
	 * @return
	 * @throws SQLException
	 *             if no table has been matched
	 */
	public boolean checkIdentity(String[] tables, String[] cols)
			throws SQLException {
		// Return true if finished correctly or throw exception in it didn't
		// match
		// in exception msg name the mismatched columns
		TableMeta meta = metaData.get(tables[0]);
		if (meta == null)
			throw new SQLException("Table '" + tables[0] + "' doesn't exist");

		if (cols != null) {
			for (int i = 0; i < cols.length; i++) {
				String c = cols[i];
				if ((c.charAt(0) != '\"' || c.charAt(c.length() - 1) != '\"')
						&& (c.charAt(0) != '\'' || c.charAt(c.length() - 1) != '\''))
					if (!meta.hasColumn(cols[i]))
						throw new SQLException("Unknown column '" + cols[i]
								+ "' in field list");
			}
		}

		return true;
	}

	public ArrayList<String>[] getIdentityMatrix(String[] tables, String[] cols)
			throws SQLException {

		@SuppressWarnings("unchecked")
		ArrayList<String>[] idList = new ArrayList[tables.length + 1];
		idList[0] = new ArrayList<String>();

		int[] foundIn = new int[cols.length];

		for (int i = 0; i < tables.length; i++) {
			TableMeta meta = metaData.get(tables[i]);
			if (meta == null)
				throw new SQLException("Table '" + tables[i]
						+ "' doesn't exist.");
			else {
				idList[i+1] = new ArrayList<String>();

				for (int j = 0; j < cols.length; j++) {
					if(cols[j].equals("*")){
						idList[i+1].addAll(Arrays.asList(meta.getColumnNames()));
						foundIn[j] = i+1;
						continue;
					}
					int point = cols[j].indexOf('.');
					if (point > 0) {
						if(cols[j].substring(0, point).equals(tables[i])){
							String colName = cols[j].substring(point+1);
							if(colName.equals("*")){
								idList[i+1].addAll(Arrays.asList(meta.getColumnNames()));
								foundIn[j] = i+1;
								continue;
							}
							if(!meta.hasColumn(colName))
								throw new SQLException("Unknown column '"+cols[j]+"' in field list");
							foundIn[j] = i+1;
							idList[i+1].add(colName);
						}
					} else if (meta.hasColumn(cols[j])) {
						if(foundIn[j]-1 >= 0 && foundIn[j]-1 != i)
							throw new SQLException("Column '"+cols[j]+"' in field list is ambiguous");
						idList[i+1].add(cols[j]);
						foundIn[j] = i + 1;
					}

				}
			}
		}
		
		for(int j = 0; j < cols.length; j++){
			if(foundIn[j] == 0){
				if (cols[j].charAt(0) == '\"' || cols[j].charAt(0) == '\'')
						idList[0].add(cols[j]);
				else
					throw new SQLException("Unknown column '" + cols[j]
							+ "' in field list");
			}
		}
		
		return idList;

	}

	/**
	 * checks whether the following values can be added to this table
	 * 
	 * @param table
	 *            the name of the table want to check
	 * @param cols
	 *            the name of the columns corresponding to the values
	 * @param values
	 *            wants to check
	 * @return the matching result
	 * @throws SQLException
	 *             if the table didn't match these specifications
	 */
	public boolean checkType(TableMeta tMeta, String[] cols, String[][] values)
			throws SQLException {
		// Return true if finished correctly or throw exception if it didn't
		// match
		if (tMeta == null)
			throw new SQLException("Invalid table name");
		for (int i = 0; i < cols.length; i++) {
			int j = 0;
			try {
				int type = tMeta.colType(cols[i]);
				switch (type) {
				case Types.NUMERIC:
					for (; j < values.length; j++)
						new BigDecimal(values[j][i]);
					break;
				case Types.DECIMAL:
					for (; j < values.length; j++)
						new BigDecimal(values[j][i]);
					break;
				case Types.BIT:
					for (; j < values.length; j++)
						new Boolean(values[j][i]);
					break;
				case Types.BOOLEAN:
					for (; j < values.length; j++)
						new Boolean(values[j][i]);
					break;
				case Types.TINYINT:
					for (; j < values.length; j++)
						new Byte(values[j][i]);
					break;
				case Types.SMALLINT:
					for (; j < values.length; j++)
						new Short(values[j][i]);
					break;
				case Types.INTEGER:
					for (; j < values.length; j++)
						new Integer(values[j][i]);
					break;
				case Types.BIGINT:
					for (; j < values.length; j++)
						new Long(values[j][i]);
					break;
				case Types.REAL:
					for (; j < values.length; j++)
						new Float(values[j][i]);
					break;
				case Types.FLOAT:
					for (; j < values.length; j++)
						new Float(values[j][i]);
					break;
				case Types.DOUBLE:
					for (; j < values.length; j++)
						new Float(values[j][i]);
					break;
				case Types.DATE:
					SimpleDateFormat formatter = new SimpleDateFormat(
							"yy-MM-dd");
					for (; j < values.length; j++)
						formatter.parse(values[j][i]);
					break;
				case Types.TIME:
					for (; j < values.length; j++)
						new Long(values[j][i]);
					break;
				}
			} catch (Exception e) {
				throw new SQLException(
						"Field type constraint violation for column '"
								+ cols[i] + "' at insert row " + i);
			}
		}

		return true;
	}

	// Unimplemented Methods
	// ----------------------------------------------------------------
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public void clearWarnings() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public void commit() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public Blob createBlob() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public Clob createClob() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public NClob createNClob() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public String getCatalog() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public int getHoldability() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return new jdbc.DatabaseMetaData(this);
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public boolean isClosed() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public void rollback() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
	}

	@Override
	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		throw new SQLException("Unimplemented");
	}

	public Logger getLogger() {
		return logger;
	}

	public CRUD manipulate() {
		return operations;
	}

	public String getDBPath() {
		return DBDir.getAbsolutePath();
	}

	public void removeTable(String tableName) {
		table.remove(tableName);
		metaData.remove(tableName);
	}

	@Override
	public void abort(Executor arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getSchema() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNetworkTimeout(Executor arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSchema(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}
}