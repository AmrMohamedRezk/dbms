package jdbc;

import java.sql.SQLException;
import dbms.TableMeta;

public class ResultSetMetaData implements java.sql.ResultSetMetaData {
	private TableMeta[] tMeta;
	private String[] columns;
	
	
	/**
	 * This constructor initializes the ResultSetMetaData.
	 * @param tMeta_			is an array of the tables represented in the ResultSet
	 * @param columns_		is an array containing the names of the columns represented in the ResultSet ordered by their appearance.
	 */
	public ResultSetMetaData(TableMeta[] tMeta_, String[] columns_){
		tMeta = tMeta_;
		columns = columns_;
	}

	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return false;
	}	
	
	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return null;
	}

	@Override
	public String getCatalogName(int arg0) throws SQLException {
		return null;
	}

	@Override
	public String getColumnClassName(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return tMeta[0].colType(arg0-1);
	}

	@Override
	public int getColumnCount() throws SQLException {
		return columns.length;
	}

	@Override
	public int getColumnDisplaySize(int arg0) throws SQLException {
		return 0;
	}

	@Override
	public String getColumnLabel(int arg0) throws SQLException {
		// TODO Redo when we use aliases
		return columns[arg0-1];
	}

	@Override
	public String getColumnName(int arg0) throws SQLException {
		// TODO Redo when we use aliases
		return columns[arg0-1];
	}

	@Override
	public int getColumnType(int arg0) throws SQLException {
		return tMeta[0].colType(columns[arg0-1]);
	}

	@Override
	public String getColumnTypeName(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return tMeta[0].colType(arg0);
	}

	@Override
	public int getPrecision(int arg0) throws SQLException {
		return 0;
	}

	@Override
	public int getScale(int arg0) throws SQLException {
		return 0;
	}

	@Override
	public String getSchemaName(int arg0) throws SQLException {
		return null;
	}

	
	@Override
	public String getTableName(int arg0) throws SQLException {
		//TODO Modify when we implement multiple tables.
		return tMeta[0].tableName();
	}

	@Override
	public boolean isAutoIncrement(int arg0) throws SQLException {
		//TODO Modify when we implement multiple tables.
		return tMeta[0].isAuto(columns[arg0-1]);
	}

	@Override
	public boolean isCaseSensitive(int arg0) throws SQLException {
		return false;
	}

	@Override
	public boolean isCurrency(int arg0) throws SQLException {
		return false;
	}

	@Override
	public boolean isDefinitelyWritable(int arg0) throws SQLException {
		return false;
	}

	@Override
	public int isNullable(int arg0) throws SQLException {
		// TODO Modify when we implement multiple tables
		return tMeta[0].isNullable(columns[arg0-1])?columnNullable:columnNoNulls;
	}

	@Override
	public boolean isReadOnly(int arg0) throws SQLException {
		String s= getColumnName(arg0);
		char c=s.charAt(0);
		boolean flag=false;
		if(c==34||c==39)
			flag=true;
		return flag;
	}

	@Override
	public boolean isSearchable(int arg0) throws SQLException {
		// TODO Do when we handle select "constant"
		boolean flag =isReadOnly(arg0);
		return !flag;
	}

	@Override
	public boolean isSigned(int arg0) throws SQLException {
		return !(isReadOnly(arg0));
	}

	
	@Override
	public boolean isWritable(int arg0) throws SQLException {
		boolean flag =isReadOnly(arg0);
		return !flag;
	}

}
