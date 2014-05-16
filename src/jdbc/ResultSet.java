package jdbc;


import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

//TODO double check on sql to java types conversion
public class ResultSet  implements java.sql.ResultSet{
	private jdbc.ResultSetMetaData metaData;
	private String[][] table;
	// the index of row pointing at, initially equal -1
	// cursor is 1 based
	private	int cursor ;
	//Base is One as stated at the doc
	private int firstRowIndex;
	private  int lastRowIndex;
	private jdbc.Statement stat = null;
	
	public String [][]getTable()
	{
		return table;
	}
	public int getCursor()
	{
		return cursor;
	}
	public int  getFirstRowIndex()
	{
		return firstRowIndex;
	}
	public int getLastRowIndex()
	{
		return lastRowIndex;
	}
	public void setCursor(int cur)
	{
		cursor=cur;
	}
//	normandy2.jdbc.Statement statement;
/*	public ResultSet(ResultSetMetaData myMetaData,String[][] data)
	{
		table = data;
		cursor = 0;
		lastRowIndex = data.length;
		firstRowIndex = 1;
		metaData= (normandy2.jdbc.ResultSetMetaData) myMetaData;
		stat= null;
		//statement =sta;
	}*/
	
	public ResultSet(ResultSetMetaData myMetaData,String[][] data,jdbc.Statement stat)
	{
		
		table = data;
		cursor = 0;
		lastRowIndex = data.length;
		firstRowIndex = 1;
		metaData= (jdbc.ResultSetMetaData) myMetaData;
		this.stat=stat ;
		//statement =sta;
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
	public boolean absolute(int row) throws SQLException {
		// 
		int tempCursor = 0 ;
		if ( row >= 0) // start count from the first row
			tempCursor = row;
		else if(row < 0) // start count form the last row
			tempCursor = lastRowIndex + 1 + row;

		if( tempCursor > lastRowIndex) //Attempt to move the cursor after the last row
		{
			cursor = lastRowIndex + 1;
			return false;
		}
		else if( tempCursor <= 0) //Attempt to move the cursor before the last row
		{
			cursor = firstRowIndex - 1;
			return false;
		}
		else
			cursor=tempCursor;
		return true;
	}

	@Override
	public void afterLast() throws SQLException {
		// 
		cursor = lastRowIndex + 1;
	}

	@Override
	public void beforeFirst() throws SQLException {
		// 
		cursor = firstRowIndex - 1;
	}

	@Override
	public void cancelRowUpdates() throws SQLException {
		// 

	}

	@Override
	public void clearWarnings() throws SQLException {
		// 

	}

	@Override
	public void close() throws SQLException {
		// 
		metaData=null;
		table=null;
		stat=null;
	}

	@Override
	public void deleteRow() throws SQLException {
		// 

	}

	@Override
	public int findColumn(String columnLabel) throws SQLException {
		int index = -1;
		// TODO Fix to implement multiple tables
		for(int i = 1 ; i <= metaData.getColumnCount(); i++)
		{
			if(columnLabel.equals(metaData.getColumnName(i)))
			{
				index = i;
				break;
			}
		}
		if( index == -1)
			throw new SQLException();
		return index;
	}

	@Override
	public boolean first() throws SQLException {
		// 
		cursor = firstRowIndex;
		if(table.length > 0)
			return true;
		return false;
	}

	@Override
	public Array getArray(int columnIndex) throws SQLException {
		// TODO decide what to do about it
		String [] array = new String [table[columnIndex].length];
		for(int i=0;i<table[columnIndex].length;i++)
			array[i]=table[i][columnIndex];
		return null;
	}

	@Override
	public Array getArray(String columnLabel) throws SQLException {
		return getArray(findColumn(columnLabel));
	}

	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		// 
		return null;
	}

	@Override
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		// 
		return null;
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex, int scale)
	throws SQLException {
		// 
		return null;
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel, int scale)
	throws SQLException {
		// 
		return null;
	}

	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		// 
		return null;
	}

	@Override
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLException {
		// 
		return null;
	}

	@Override
	public Blob getBlob(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public boolean getBoolean(int columnIndex) throws SQLException {
		return Boolean.parseBoolean(getString(columnIndex));
	}

	@Override
	public boolean getBoolean(String columnLabel) throws SQLException {
		return getBoolean(findColumn(columnLabel));
	}

	@Override
	public byte getByte(int columnIndex) throws SQLException {
		// 
		return 0;
	}

	@Override
	public byte getByte(String columnLabel) throws SQLException {
		// 
		return 0;
	}

	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {
		// 
		byte[] obj = table[ cursor - 1][ columnIndex - 1].getBytes();
		return obj;
	}

	@Override
	public byte[] getBytes(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		// 
		return null;
	}

	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLException {
		// 
		return null;
	}

	@Override
	public Clob getClob(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public int getConcurrency() throws SQLException {
		// 
		return 0;
	}

	@Override
	public String getCursorName() throws SQLException {
		// 
		return null;
	}

	@Override
	public Date getDate(int columnIndex) throws SQLException {
		String s = getString(columnIndex);
		DateFormat formatter ; 
		java.util.Date date =null; 
		formatter = new SimpleDateFormat("yy-MM-dd");
		try {
			date = (java.util.Date)formatter.parse(s);
		} catch (ParseException e) {
			//TODO hotfix;
			if(s.equals(""))
				return null;
			throw new RuntimeException("Parse Error");
		} 
		long longDate=date.getTime();
		Date d = new Date(longDate);
	return d;
	}	

	@Override
	public Date getDate(String columnLabel) throws SQLException {
		return getDate(findColumn(columnLabel));
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		// 
		return null;
	}

	@Override
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		// 
		return null;
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException {
		return Double.parseDouble(getString(columnIndex));
	}

	@Override
	public double getDouble(String columnLabel) throws SQLException {
		return getDouble(findColumn(columnLabel));
	}

	@Override
	public int getFetchDirection() throws SQLException {
		// TODO see if we'll ever decide to go in 2 directions!!
		return 1;
	}

	@Override
	public int getFetchSize() throws SQLException {
		// 
		return 0;
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException {
		return Float.parseFloat(getString(columnIndex));
	}

	@Override
	public float getFloat(String columnLabel) throws SQLException {
		return getFloat(findColumn(columnLabel));
	}

	@Override
	public int getHoldability() throws SQLException {
		// 
		return 0;
	}

	@Override
	public int getInt(int columnIndex) throws SQLException {
		return Integer.parseInt(getString(columnIndex));
	}

	@Override
	public int getInt(String columnLabel) throws SQLException {
		return getInt(findColumn(columnLabel));
	}

	@Override
	public long getLong(int columnIndex) throws SQLException {
		return Long.parseLong(getString(columnIndex));
	}

	@Override
	public long getLong(String columnLabel) throws SQLException {
		return getLong(findColumn(columnLabel));
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return metaData;
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		// 
		return null;
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		// 
		return null;
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		// 
		return null;
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public Object getObject(int columnIndex) throws SQLException {
		if(metaData.getColumnCount() < columnIndex)
				throw new SQLException();
		
		String value=getString(columnIndex);
		try {			
			int type=metaData.getColumnType(columnIndex);
					switch (type) {
				case Types.NUMERIC:
					return new BigDecimal(value);
				case Types.DECIMAL:
					return new BigDecimal(value);
				case Types.BIT:
					return new Boolean(value);
				case Types.BOOLEAN:
					return new Boolean(value);
				case Types.TINYINT:
					return new Byte(value);
				case Types.SMALLINT:
					return new Short(value);
				case Types.INTEGER:
					return new Integer(value);
				case Types.BIGINT:
					return new Long(value);
				case Types.REAL:
					return new Float(value);
				case Types.FLOAT:
					return new Float(value);
				case Types.DOUBLE:
					return new Float(value);
				case Types.DATE:
					return getDate(columnIndex);
				case Types.TIME:
					return new Time(new Long(value));
				default:
					return value;
				}
	} catch (Exception e) {
		//TODO hotfix :)
		if(value.equals(""))
			return null;
			throw new RuntimeException(
					"Data base file corrupted!");
		}
	}

	@Override
	public Object getObject(String columnLabel) throws SQLException {
		if(cursor <= 0)
			throw new SQLException();
		else
		{
			return getObject(findColumn(columnLabel));
		}
	}

	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map)
	throws SQLException {
		// 
		return null;
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map)
	throws SQLException {
		// 
		return null;
	}

	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		// 
		return null;
	}

	public <T> T getObject(String columnLabel, Class<T> type)
	throws SQLException {
		// 
		return null;
	}

	@Override
	public Ref getRef(int columnIndex) throws SQLException {
		// 
		return null;
	}

	@Override
	public Ref getRef(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public int getRow() throws SQLException {
		return cursor;
	}

	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		// 
		return null;
	}

	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		// 
		return null;
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public short getShort(int columnIndex) throws SQLException {
		// 
		return 0;
	}

	@Override
	public short getShort(String columnLabel) throws SQLException {
		// 
		return 0;
	}

	@Override
	public Statement getStatement() throws SQLException {
		// 
		return stat;
	}

	@Override
	public String getString(int columnIndex) throws SQLException {
		if(cursor <= 0 || metaData.getColumnCount() < columnIndex){
			System.out.println(metaData.getColumnCount());
			throw new SQLException();
		}
		else 
			return  table[cursor-1][columnIndex -1];

	}

	@Override
	public String getString(String columnLabel) throws SQLException {
		return getString(findColumn(columnLabel));
	}

	@Override
	public Time getTime(int columnIndex) throws SQLException {
		// 
		return null;
	}

	@Override
	public Time getTime(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		// 
		return null;
	}

	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		// 
		return null;
	}

	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		// 
		return null;
	}

	@Override
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal)
	throws SQLException {
		// 
		return null;
	}

	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal)
	throws SQLException {
		// 
		return null;
	}

	@Override
	public int getType() throws SQLException {
		// 
		return 0;
	}

	@Override
	public URL getURL(int columnIndex) throws SQLException {
		// 
		return null;
	}

	@Override
	public URL getURL(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		// 
		return null;
	}

	@Override
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		// 
		return null;
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		// 
		return null;
	}

	@Override
	public void insertRow() throws SQLException {
		// 

	}

	@Override
	public boolean isAfterLast() throws SQLException {
		// 
		return (cursor>lastRowIndex);
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		// 
		return (cursor<firstRowIndex);
	}

	@Override
	public boolean isClosed() throws SQLException {
		// 
		try {
			return ((metaData.equals(null))&&(table.equals(null))&&(stat.equals(null)));
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			return true;
		}
	}

	@Override
	public boolean isFirst() throws SQLException {
		// 
		return (cursor==firstRowIndex);
	}

	@Override
	public boolean isLast() throws SQLException {
		// 
		return (cursor==lastRowIndex);
	}

	@Override
	public boolean last() throws SQLException {
		// 
		if(table.length==0)
			return false;
		else
			cursor=lastRowIndex;
		return true;
	}

	@Override
	public void moveToCurrentRow() throws SQLException {
		// 

	}

	@Override
	public void moveToInsertRow() throws SQLException {
		// 

	}

	@Override
	public boolean next() throws SQLException {
		// 
		if(cursor<table.length)
		{
			cursor++;
			return true;
		}
		else
			return false;
	}

	@Override
	public boolean previous() throws SQLException {
		// 
		if(cursor>0)
		{
			cursor--;
			return true;
		}
		else
			return false;
	}

	@Override
	public void refreshRow() throws SQLException {
		// 

	}

	@Override
	public boolean relative(int rows) throws SQLException {
		// 
		return false;
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		// 
		return false;
	}

	@Override
	public boolean rowInserted() throws SQLException {
		// 
		return false;
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		// 
		return false;
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
	public void updateArray(int columnIndex, Array x) throws SQLException {
		// 

	}

	@Override
	public void updateArray(String columnLabel, Array x) throws SQLException {
		// 

	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x)
	throws SQLException {
		// 

	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x)
	throws SQLException {
		// 

	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length)
	throws SQLException {
		// 

	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, int length)
	throws SQLException {
		// 

	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length)
	throws SQLException {
		// 

	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length)
	throws SQLException {
		// 

	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x)
	throws SQLException {
		// 

	}

	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal x)
	throws SQLException {
		// 

	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x)
	throws SQLException {
		// 

	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x)
	throws SQLException {
		// 

	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length)
	throws SQLException {
		// 

	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, int length)
	throws SQLException {
		// 

	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length)
	throws SQLException {
		// 

	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x,
			long length) throws SQLException {
		// 

	}

	@Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		// 

	}

	@Override
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		// 

	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream)
	throws SQLException {
		// 

	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream)
	throws SQLException {
		// 

	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length)
	throws SQLException {
		// 

	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream,
			long length) throws SQLException {
		// 

	}

	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		// 

	}

	@Override
	public void updateBoolean(String columnLabel, boolean x)
	throws SQLException {
		// 

	}

	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		// 

	}

	@Override
	public void updateByte(String columnLabel, byte x) throws SQLException {
		// 

	}

	@Override
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		// 

	}

	@Override
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		// 

	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x)
	throws SQLException {
		// 

	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader)
	throws SQLException {
		// 

	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, int length)
	throws SQLException {
		// 

	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader,
			int length) throws SQLException {
		// 

	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length)
	throws SQLException {
		// 

	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		// 

	}

	@Override
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		// 

	}

	@Override
	public void updateClob(String columnLabel, Clob x) throws SQLException {
		// 

	}

	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		// 

	}

	@Override
	public void updateClob(String columnLabel, Reader reader)
	throws SQLException {
		// 

	}

	@Override
	public void updateClob(int columnIndex, Reader reader, long length)
	throws SQLException {
		// 

	}

	@Override
	public void updateClob(String columnLabel, Reader reader, long length)
	throws SQLException {
		// 

	}

	@Override
	public void updateDate(int columnIndex, Date x) throws SQLException {
		// 

	}

	@Override
	public void updateDate(String columnLabel, Date x) throws SQLException {
		// 

	}

	@Override
	public void updateDouble(int columnIndex, double x) throws SQLException {
		// 

	}

	@Override
	public void updateDouble(String columnLabel, double x) throws SQLException {
		// 

	}

	@Override
	public void updateFloat(int columnIndex, float x) throws SQLException {
		// 

	}

	@Override
	public void updateFloat(String columnLabel, float x) throws SQLException {
		// 

	}

	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		// 

	}

	@Override
	public void updateInt(String columnLabel, int x) throws SQLException {
		// 

	}

	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		// 

	}

	@Override
	public void updateLong(String columnLabel, long x) throws SQLException {
		// 

	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x)
	throws SQLException {
		// 

	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader)
	throws SQLException {
		// 

	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length)
	throws SQLException {
		// 

	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		// 

	}

	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		// 

	}

	@Override
	public void updateNClob(String columnLabel, NClob nClob)
	throws SQLException {
		// 

	}

	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		// 

	}

	@Override
	public void updateNClob(String columnLabel, Reader reader)
	throws SQLException {
		// 

	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length)
	throws SQLException {
		// 

	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length)
	throws SQLException {
		// 

	}

	@Override
	public void updateNString(int columnIndex, String nString)
	throws SQLException {
		// 

	}

	@Override
	public void updateNString(String columnLabel, String nString)
	throws SQLException {
		// 

	}

	@Override
	public void updateNull(int columnIndex) throws SQLException {
		// 

	}

	@Override
	public void updateNull(String columnLabel) throws SQLException {
		// 

	}

	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		// 

	}

	@Override
	public void updateObject(String columnLabel, Object x) throws SQLException {
		// 

	}

	@Override
	public void updateObject(int columnIndex, Object x, int scaleOrLength)
	throws SQLException {
		// 

	}

	@Override
	public void updateObject(String columnLabel, Object x, int scaleOrLength)
	throws SQLException {
		// 

	}

	@Override
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		// 

	}

	@Override
	public void updateRef(String columnLabel, Ref x) throws SQLException {
		// 

	}

	@Override
	public void updateRow() throws SQLException {
		// 

	}

	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		// 

	}

	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		// 

	}

	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject)
	throws SQLException {
		// 

	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject)
	throws SQLException {
		// 

	}

	@Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		// 

	}

	@Override
	public void updateShort(String columnLabel, short x) throws SQLException {
		// 

	}

	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		// 

	}

	@Override
	public void updateString(String columnLabel, String x) throws SQLException {
		// 

	}

	@Override
	public void updateTime(int columnIndex, Time x) throws SQLException {
		// 

	}

	@Override
	public void updateTime(String columnLabel, Time x) throws SQLException {
		// 

	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x)
	throws SQLException {
		// 

	}

	@Override
	public void updateTimestamp(String columnLabel, Timestamp x)
	throws SQLException {
		// 

	}

	@Override
	public boolean wasNull() throws SQLException {
		// 
		return false;
	}
	public static void main(String[] args) {
		//normandy2.jdbc.ResultSetMetaData rsm = new normandy2.jdbc.ResultSetMetaData(, columns_);
		//ResultSet rs = new ResultSet(myMetaData, data);
	}
		

}
