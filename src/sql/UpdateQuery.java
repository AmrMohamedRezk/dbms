package sql;

import java.sql.SQLException;
import java.util.ArrayList;

import dbms.CRUD;
import dbms.Table;
import dbms.TableMeta;
import jdbc.ResultSet;

class UpdateQuery extends Query {

	private String table = null;
	private WhereClause cond = null;
	private ArrayList<String> fields = new ArrayList<String>();
	private ArrayList<String> values = new ArrayList<String>();
	private int rows;

	protected UpdateQuery(String query, int pointer) throws SQLSyntaxException {
		this.query = query;
		this.parsePointer = pointer;
		parse();
	}

	public ResultSet execute() throws SQLException {
		Table t = null;
		String[] c = new String[fields.size()];
		TableMeta meta = null;
		String[] vals = new String[values.size()];;	
		fields.toArray(c);
		values.toArray(vals);
		
		if (connection.checkIdentity(new String[] { table }, c)) {
			t = connection.getTable(table);
			meta = connection.getTableMeta(table);
		}

		if (c.length != values.size())
			throw new SQLException("Column count doesn't match value count");

		try {
			connection.checkIdentity(new String[] { table }, cond.fieldList());
		} catch (SQLException e) {
			throw new SQLException(e.getMessage().replace("field list", "where clause"));
		}
		
		if (connection.checkType(meta, c, new String[][] {vals})) {
			result = connection.manipulate().update(t, c, vals,
					"//"+table+"/row" + cond.xPath());
		}	
		return null;
	}

	private boolean parse() throws SQLSyntaxException {
		table = parseTable();
		if (table == null)
			throw new SQLSyntaxException(query, parsePointer);

		if (setClauseFound()) {
			ArrayList<String>[] l = parseFieldValue();
			fields = l[0];
			values = l[1];
			if (fields.size() == 0)
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
		StringBuilder s = new StringBuilder(table + "\n");
		for (int i = 0; i < fields.size(); i++) {
			s.append(fields.get(i));
			s.append('\n');
		}
		for (int i = 0; i < values.size(); i++) {
			s.append(values.get(i));
			s.append('\n');
		}

		return s.toString();
	}

}
