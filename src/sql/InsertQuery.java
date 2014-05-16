package sql;

import java.sql.SQLException;
import java.util.ArrayList;

import dbms.CRUD;
import dbms.Table;
import dbms.TableMeta;
import jdbc.ResultSet;

class InsertQuery extends Query {

	private String table = null;
	private ArrayList<String> fields = new ArrayList<String>();
	private ArrayList<String> values = new ArrayList<String>();
	private ArrayList<String[]> rows = new ArrayList<String[]>();
	private SelectQuery select = null;

	protected InsertQuery(String query, int pointer) throws SQLSyntaxException {
		this.query = query;
		this.parsePointer = pointer;
		parse();
	}

	private boolean parse() throws SQLSyntaxException {
		if (intoClauseFound()) {
			table = parseTable();
			if (table == null)
				throw new SQLSyntaxException(query, parsePointer);
		} else
			throw new SQLSyntaxException(query, parsePointer);

		boolean found = false;
		if (parenthesisFound()) {
			fields = parseFieldList();
			if (fields.size() < 1)
				throw new SQLSyntaxException(query, parsePointer);
		}

		if (valuesClauseFound()) {
			values = parseValues();
			if (values.size() == 0)
				throw new SQLSyntaxException(query, parsePointer);
		} else {
			try {
				select = (SelectQuery) Query.buildQuery(connection, statement,
						query.substring(parsePointer));
			} catch (ClassCastException e) {
				throw new SQLSyntaxException(query, parsePointer);
			} catch (SQLSyntaxException e) {
				throw new SQLSyntaxException(query, parsePointer
						+ e.getPointer());
			}
		}

		return false;
	}

	public ResultSet execute() throws SQLException {

		Table t = null;
		String[] c = null;
		TableMeta meta = null;
		String[][] vals = null;

		if (fields.size() == 0) {
			if (connection.checkIdentity(new String[] { table }, null)) {
				t = connection.getTable(table);
				meta = connection.getTableMeta(table);
				c = meta.getColumnNames();
			}
		} else {
			c = new String[fields.size()];
			fields.toArray(c);
			if (connection.checkIdentity(new String[] { table }, c)) {
				t = connection.getTable(table);
				meta = connection.getTableMeta(table);
			}
		}

		if (select != null){
			select.connection = connection;
			return insertResultSet(select.execute(), t, meta, c);
		}

		if (c.length != values.size())
			throw new SQLException("Column count doesn't match value count");

		vals = new String[1][values.size()];
		values.toArray(vals[0]);
		if (connection.checkType(meta, c, vals)) {
			result = connection.manipulate().insert(t, c, vals);
		}

		return null;
	}

	public ResultSet insertResultSet(ResultSet rs, Table table, TableMeta meta,
			String[] c) throws SQLException {

		if (c.length != rs.getMetaData().getColumnCount())
			throw new SQLException("Column count doesn't match value count");
		if (connection.checkType(meta, c, rs.getTable()))
			connection.manipulate().insert(table, c, rs.getTable());
		return null;
	}

	public String toString() {
		String s = "";
		s += table + "\n";
		for (int i = 0; i < fields.size(); i++) {
			s += fields.get(i) + " ";
		}
		s += "\n";
		for (int i = 0; i < values.size(); i++) {
			s += values.get(i) + " ";
		}

		return s;
	}

}
