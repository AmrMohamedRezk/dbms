package sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import dbms.CRUD;
import dbms.Table;
import dbms.TableMeta;
import jdbc.ResultSet;
import jdbc.ResultSetMetaData;
import jdbc.Statement;

class SelectQuery extends Query {

	private ArrayList<String> cols;
	private ArrayList<String> tables;
	private WhereClause cond = null;

	protected SelectQuery(String query, int pointer) throws SQLSyntaxException {
		this.query = query;
		this.parsePointer = pointer;
		parse();
	}

	public ResultSet execute() throws SQLException {
		if (tables.size() == 1)
			return doSingleTableSelect();
		else
			return doMultipleTableSelect();

	}

	private ResultSet doMultipleTableSelect() throws SQLException {
		ArrayList<String[][]> select = new ArrayList<String[][]>();
		String[] t = new String[tables.size()];
		String[] c = new String[cols.size()];
		tables.toArray(t);
		cols.toArray(c);
		ArrayList<String>[] idList = connection.getIdentityMatrix(t, c);
		ArrayList<String>[] condIdList = connection.getIdentityMatrix(t, cond.fieldList());
		ArrayList<String> allCols = new ArrayList<String>();

		//TODO add constant columns
		for (int i = 1; i < idList.length; i++) {
			if (idList[i].size() > 0) {
				String[] tc = new String[idList[i].size()];
				idList[i].toArray(tc);
				String[] condC = new String[condIdList[i].size()];
				condIdList[i].toArray(condC);
				allCols.addAll(idList[i]);
				select.add(connection.manipulate().select(
						connection.getTable(t[i - 1]), tc, "//row"+cond.xPath(t[i-1], condC)));
			}
		}

		TableMeta[] meta = new TableMeta[t.length];
		for(int i = 0; i < t.length; i++)
			meta[i] = connection.getTableMeta(t[i]);
		
		ArrayList<String[]> rowsSelected = doJoin(select, null, 0, 0, allCols.size());
		String[] allC = new String[allCols.size()];
		allCols.toArray(allC);
		ResultSetMetaData k = new ResultSetMetaData(meta, allC);		
		result = rowsSelected.size();
		String[][] rows = new String[rowsSelected.size()][];
		rowsSelected.toArray(rows);
		ResultSet rs = new ResultSet(k, rows, statement);
		return rs;
	}

	private ArrayList<String[]> doJoin(ArrayList<String[][]> select,
			ArrayList<String[]> r, int depth, int rowIndex, int rowLength) {

		int nxtRowIndex = 0;
		ArrayList<String[]> rows = new ArrayList<String[]>();
		if (r == null) {
			for (int i = 0; i < select.get(depth).length; i++) {
				String[] row = new String[rowLength];
				String[] val = select.get(depth)[i];
				System.arraycopy(val, 0, row, 0, val.length);
				rows.add(row);
				nxtRowIndex = rowIndex+val.length;
			}
		} else {
			for (int i = 0; i < select.get(depth).length; i++) {
				for (int j = 0; j < r.size(); j++) {
					String[] row = new String[rowLength];
					String[] val = r.get(j);
					System.arraycopy(val, 0, row, 0, rowIndex);
					val = select.get(depth)[i];
					System.arraycopy(val, 0, row, rowIndex, val.length);
					rows.add(row);
					nxtRowIndex = rowIndex+val.length;
				}
			}
		}
		if (depth == select.size() - 1)
			return rows;
		else
			return doJoin(select, rows, depth + 1, nxtRowIndex, rowLength);
	}

	private ResultSet doSingleTableSelect() throws SQLException {
		Table t = null;
		String tname = tables.get(0);
		String[] c = null;
		TableMeta[] tabl = new TableMeta[tables.size()];

		if (cols.size() == 1 && cols.get(0).equals("*")) {
			if (connection.checkIdentity(new String[] { tname }, null)) {
				t = connection.getTable(tables.get(0));
				tabl[0] = connection.getTableMeta(tname);
				c = tabl[0].getColumnNames();
			}
		} else {
			c = new String[cols.size()];
			cols.toArray(c);
			if (connection.checkIdentity(new String[] { tname }, c)) {
				t = connection.getTable(tables.get(0));
				tabl[0] = connection.getTableMeta(tname);
			}
		}

		try {
			connection.checkIdentity(new String[] { tname }, cond.fieldList());
		} catch (SQLException e) {
			throw new SQLException(e.getMessage().replace("field list",
					"where clause"));
		}

		ResultSetMetaData k = new ResultSetMetaData(tabl, c);
		String[][] rows = connection.manipulate().select(t, c,
				"//" + tname + "/row" + cond.xPath());
		result = rows.length;
		ResultSet rs = new ResultSet(k, rows, statement);
		return rs;
	}

	private boolean parse() throws SQLSyntaxException {
		cols = parseFieldList();

		if (cols.size() < 1)
			throw new SQLSyntaxException(query, parsePointer);

		if (fromClauseFound())
			tables = parseTableList();
		else
			throw new SQLSyntaxException(query, parsePointer);

		if (whereClauseFound())
			cond = new WhereClause(query, parsePointer);
		else
			cond = new WhereClause();
		return true;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tables.size(); i++) {
			sb.append(tables.get(i));
			sb.append('\n');
		}
		for (int i = 0; i < cols.size(); i++) {
			sb.append(cols.get(i));
			sb.append('\n');
		}
		sb.append(cond);
		return sb.toString();
	}
}
