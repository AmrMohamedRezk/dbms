package sql;

import java.sql.SQLException;

import jdbc.ResultSet;

public class DropQuery extends Query {

	private String table = null;
	
	public DropQuery(String query, int pointer) throws SQLSyntaxException{
		this.query = query;
		this.parsePointer = pointer;
		parse();		
	}
	
	private void parse() throws SQLSyntaxException{
		if(tableClauseFound()){
			table = parseTable();
			if(table == null)
				throw new SQLSyntaxException(query, parsePointer);
		}else
			throw new SQLSyntaxException(query, parsePointer);
	}
	
	@Override
	public ResultSet execute() throws SQLException {
		if(connection.checkIdentity(new String[] {table}, null)){
			connection.getTable(table).delete();
			connection.removeTable(table);
		}
		return null;
	}
}
