package sql;

import java.sql.SQLException;

import dbms.CRUD;
import dbms.Table;
import jdbc.ResultSet;

class DeleteQuery extends Query {

	private String table = null;
	private WhereClause cond = null;

	protected DeleteQuery(String query, int pointer) throws SQLSyntaxException {
		this.query = query;
		this.parsePointer = pointer;
		parse();
	}

	public ResultSet execute() throws SQLException {
		if (connection.checkIdentity(new String[] { table }, null)) {
			try {
				connection.checkIdentity(new String[] { table }, cond.fieldList());
			} catch (SQLException e) {
				throw new SQLException(e.getMessage().replace("field list", "where clause"));
			}
			
			result = connection.manipulate().detele(connection.getTable(table),
					"//"+table+"/row" + cond.xPath());
		}

		return null;
	}

	private boolean parse() throws SQLSyntaxException {
		if (fromClauseFound()) {
			table = parseTable();
			if (table == null)
				throw new SQLSyntaxException(query, parsePointer);
		} else
			throw new SQLSyntaxException(query, parsePointer);

		if (whereClauseFound())
			cond = new WhereClause(query, parsePointer);
		else
			cond = new WhereClause();
		return false;
	}

	public String toString() {
		// return table+"\n"+cond.toString();
		return table;
	}
}
