package sql;

import java.sql.SQLException;
import java.sql.Types;

import dbms.Table;
import dbms.TableMeta;
import jdbc.ResultSet;

public class CreateQuery extends Query {

	//TODO modify parser to neglect field lengths
	TableMeta meta = null;		
	
	public CreateQuery(String query, int pointer) throws SQLSyntaxException {
		this.query = query;
		this.parsePointer = pointer;
		parse();
	}
	
	private void parse() throws SQLSyntaxException{
		String tname = null;
		if(tableClauseFound())
			tname = parseTable();
		else
			throw new SQLSyntaxException(query, parsePointer);

		if(!Character.isLetter(tname.charAt(0)))
			throw new SQLSyntaxException("Table name must start with a letter");
		
		meta = new TableMeta(tname);
		String[][][] info = parseFieldsInfo(); 
		meta.setInfoArray(info[0]);
		meta.setAutoColumn(info[1][0][0]);		
	}
	
	@Override
	public ResultSet execute() throws SQLException {
		int autoColType = meta.colType(meta.autoColumn());
		if(autoColType != -1 && autoColType != Types.INTEGER)
			throw new SQLException("Auto column must be of integral type");
		if(connection.tableExists(meta.tableName()))
			throw new SQLException("Table '"+meta.tableName()+"' already exists.");
		connection.addTable(Table.createFromMeta(connection.getDBPath(), meta));
		return null;
	}
}
