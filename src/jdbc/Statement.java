package jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;

import sql.Query;

public class Statement implements java.sql.Statement {
	ArrayList<String> list = new ArrayList<String>();
	int timeOut;
	private final static int SUCCESS_NO_INFO = -1;
	private final static int EXECUTE_FAILED = -2;
	jdbc.Connection connection;

	// TODO check this comment
	/*
	 * I assume that the given query is correct!!
	 * 
	 * 
	 * Things we still have to handle in this class
	 * --------------------------------------------- 1)This class just have the
	 * logical implementation and isn't tested at all till now
	 * 
	 * 2)Method boolean parse have to return a boolean to show if the parsing
	 * have been successful or not because it always return false
	 * 
	 * 3)Method close still have to clarify the main purpose of this method
	 * 
	 * 4)Method execute in this class : the execute method in the query class
	 * return a Result set ! have to figure something out about this.
	 * ------------
	 * --------------------------------------------------------------
	 * ------------- 5)The difference between the three methods (execute-execute
	 * update-execute Query) a)executeQuery() ---> This is used generally for
	 * reading the content of the database. The output will be in the form of
	 * ResultSet. Generally SELECT statement is used. ---> MODIFIED AND WAITING
	 * TO BE TESTED
	 * --------------------------------------------------------------
	 * ----------------------- b)executeUpdate() ---> This is generally used for
	 * altering the databases. Generally DROP TABLE or DATABASE, INSERT into
	 * TABLE, UPDATE TABLE, DELETE from TABLE statements will be used in this.
	 * The output will be in the form of int. This int value denotes the number
	 * of rows affected by the query.---> MODIFIED AND WAITING TO BE TESTED
	 * ------
	 * -----------------------------------------------------------------------
	 * c)execute() ---> If you don't know which method to be used for executing
	 * SQL statements, this method can be used. This will return a boolean. TRUE
	 * indicates the result is a ResultSet and FALSE indicates it has the int
	 * value which denotes number of rows affected by the query.---> MODIFIED
	 * AND WAITING TO BE TESTED
	 * --------------------------------------------------
	 * -----------------------------
	 * 
	 * 6)The method executeBatch() we have to modify the return array for now i
	 * set it to 1 if the operation done is successful and o otherwise--->
	 * MODIFIED AND WAITING TO BE TESTED
	 * 
	 * --------------------------------------------------------------------------
	 * --------- 7)Execute query is used for getting data and return a result
	 * set so it appears to be/M compatible with the current method should be
	 * tested /O /D 8)Execute update same problem as 7 the return type /I /F /I
	 * /E /D
	 * ----------------------------------------------------------------------
	 * ------------------ 9)get connection method unimplemented yet waiting for
	 * the create statement method
	 * 
	 * 10)set and get timeout no problems
	 */


	protected Statement(jdbc.Connection connect) {
		connection = connect;
	}

	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		// 
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		// 
		return null;
	}

	@Override
	public void addBatch(String sql) throws SQLException {
		// 
			list.add(sql);
	}

	@Override
	public void cancel() throws SQLException {
		// 

	}

	@Override
	public void clearBatch() throws SQLException {
		// 
		for (int i = 0; i < list.size(); i++) {
			list.remove(i);
		}

	}

	@Override
	public void clearWarnings() throws SQLException {
		// 

	}

	@Override
	public void close() throws SQLException {
		// 
		list = null;
		connection=null;
	}

	public void closeOnCompletion() throws SQLException {
		// 

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Statement#execute(java.lang.String) Execute is used when we
	 * don't know which method to use and it return a boolean so if it should
	 * return a result set we return true if it should return an int which
	 * represent the number of columns we return false otherwise we throw an
	 * exception
	 */
	@Override
	public boolean execute(String sql) throws SQLException {
		// 

		int i = 0;
		for (; Character.isWhitespace(sql.charAt(i)); i++)
			;
		Query st = Query.buildQuery(connection, this, sql.substring(i));
		int rank = Query.buildStatementRank(sql.substring(i));
		if (rank == 0) {
			st.execute();
			return true;
		} else if (rank > 0) {
			st.execute();
			return false;
		} else
			throw new SQLException();
	}

	@Override
	public boolean execute(String sql, int autoGeneratedKeys)
			throws SQLException {
		// 
		return false;
	}

	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		// 
		return false;
	}

	@Override
	public boolean execute(String sql, String[] columnNames)
			throws SQLException {
		// 
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Statement#executeBatch()
	 */
	@Override
	public int[] executeBatch() throws SQLException {
		// 
		int[] array = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			String sql = list.get(i);
			int j = 0;
			for (; Character.isWhitespace(sql.charAt(j)); j++)
				;
			Query st = Query.buildQuery(connection, this, sql.substring(j));
			int rank = Query.buildStatementRank(sql.substring(j));
			if (rank == 0) {
				st.execute();
				array[i] = SUCCESS_NO_INFO;
			} else if (rank > 0) {
				st.execute();
				array[i] = st.getNumberOfRows();
			} else
				array[i] = EXECUTE_FAILED;
		}
		return array;
	}

	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		// 
		ResultSet rs;
		int i = 0;
		for (; Character.isWhitespace(sql.charAt(i)); i++)
			;
		int st = Query.buildStatementRank(sql.substring(i));
		if (st == 0) {
			Query statement = Query.buildQuery(connection, this, sql.substring(i));
			rs = statement.execute();
		} else
			throw new SQLException();

		return rs;
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		// 
		int num = 0;
		int i = 0;
		for (; Character.isWhitespace(sql.charAt(i)); i++)
			;
		int st = Query.buildStatementRank(sql.substring(i));
		if (st > 0) {
			Query statement = Query.buildQuery(connection, this, sql.substring(i));
			statement.execute();
			num = statement.getNumberOfRows();
		} else
			throw new SQLException();
		return num;
	}

	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
		// 
		return 0;
	}

	@Override
	public int executeUpdate(String sql, int[] columnIndexes)
			throws SQLException {
		// 
		return 0;
	}

	@Override
	public int executeUpdate(String sql, String[] columnNames)
			throws SQLException {
		// 
		return 0;
	}

	@Override
	public Connection getConnection() throws SQLException {
		// 
		return connection;
	}

	@Override
	public int getFetchDirection() throws SQLException {
		// 
		return 0;
	}

	@Override
	public int getFetchSize() throws SQLException {
		// 
		return 0;
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		// 
		return null;
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		// 
		return 0;
	}

	@Override
	public int getMaxRows() throws SQLException {
		// 
		return 0;
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		// 
		return false;
	}

	@Override
	public boolean getMoreResults(int current) throws SQLException {
		// 
		return false;
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		// 
		return timeOut;
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		// 
		return null;
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		// 
		return 0;
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		// 
		return 0;
	}

	@Override
	public int getResultSetType() throws SQLException {
		// 
		return 0;
	}

	@Override
	public int getUpdateCount() throws SQLException {
		// 
		return 0;
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		// 
		return null;
	}

	public boolean isCloseOnCompletion() throws SQLException {
		return false;
	}

	@Override
	public boolean isClosed() throws SQLException {
		// 
		return false;
	}

	@Override
	public boolean isPoolable() throws SQLException {
		// 
		return false;
	}

	@Override
	public void setCursorName(String name) throws SQLException {
		// 

	}

	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		// 

	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		// 

	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		// 

	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		// 

	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		// 

	}

	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		// 

	}

	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
		// 
		timeOut = seconds;
	}
}
