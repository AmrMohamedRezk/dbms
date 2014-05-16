package sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import jdbc.Connection;
import jdbc.ResultSet;
import jdbc.Statement;
import sql.DeleteQuery;
import sql.SelectQuery;

//TODO insert support multiple rows
public abstract class Query {
	// TODO handle quoted values
	// TODO check on end of statement reached :)
	protected String query;
	protected int parsePointer;
	protected Connection connection;
	protected int result;
	protected Statement statement;

	private static final String[] supportedQueries = { "select", "delete",
			"insert", "update", "drop", "create" };

	public ResultSet execute() throws SQLException {
		throw new RuntimeException();
	}

	public static Query buildQuery(Connection con, Statement stat, String query)
			throws SQLSyntaxException {
		// TODO should receive query trimmed
		int fsIndex = findFirstSpace(query);
		int index = Arrays.asList(supportedQueries).indexOf(
				query.substring(0, fsIndex).toLowerCase());

		Query q = null;

		switch (index) {
		case 0:
			q = new SelectQuery(query, fsIndex);
			break;
		case 1:
			q = new DeleteQuery(query, fsIndex);
			break;
		case 2:
			q = new InsertQuery(query, fsIndex);
			break;
		case 3:
			q = new UpdateQuery(query, fsIndex);
			break;
		case 4:
			q = new DropQuery(query, fsIndex);
			break;
		case 5:
			q = new CreateQuery(query, fsIndex);
			break;
		default:
			throw new SQLSyntaxException(query, 0);
		}
		q.connection = con;
		q.statement = stat;
		return q;
	}

	private static int findFirstSpace(String query) {
		return findFirstSpace(query, 0);
	}

	private static int findFirstSpace(String query, int start) {
		// TODO may be better return -1 if not found
		int i = start;
		for (; i < query.length() && !Character.isWhitespace(query.charAt(i)); i++)
			;
		return i;
	}

	public static int buildStatementRank(String statement) {
		return Arrays.asList(supportedQueries)
				.indexOf(
						statement.substring(0, findFirstSpace(statement))
								.toLowerCase());
	}

	// TODO 2 versions
	protected ArrayList<String> parseFieldList() throws SQLSyntaxException {
		ArrayList<String> cols = new ArrayList<String>();

		StringBuilder buff = new StringBuilder();
		char stQuot = '\0';
		boolean colQuot = false;
		boolean colStarted = false;
		boolean colExpected = true;
		boolean spaceEnc = false;
		boolean parenthesized = false;
		
		int i = parsePointer;
		
		if(parenthesisFound()){
			parenthesized = true;
			i++;
		}			
		
		i = escapeSpaces(i);

		for (; i < query.length(); i++) {
			char c = query.charAt(i);
			if (stQuot != '\0') {
				buff.append(c);
				if (c == stQuot && query.charAt(i - 1) != '\\') {
					stQuot = '\0';
				}
			} else if (c == '\'' || c == '"') {
				buff.append(c);
				stQuot = c;
				colStarted = true;
			} else if (colQuot) {
				if (c == '`')
					colQuot = false;
				else
					buff.append(c);
			} else if (c == '`') {
				colQuot = true;
				colStarted = true;
			} else if( c == '('){
				throw new SQLSyntaxException(query, i);
			}else if ( c == ')'){
				if(!parenthesized || (colExpected && !colStarted))
					throw new SQLSyntaxException(query, i);
				else{
					i++;
					break;
				}
			}else if (c == ',') {
				if (colStarted) {
					spaceEnc = false;
					colStarted = false;
					colExpected = true;
					cols.add(buff.toString());
					buff = new StringBuilder();
					i = escapeSpaces(i);
				} else if (spaceEnc) {
					spaceEnc = false;
					colExpected = true;
				} else
					throw new SQLSyntaxException(query, i);
			} else if (Character.isWhitespace(query.charAt(i))) {
				if (colStarted) {
					spaceEnc = true;
					colStarted = false;
					colExpected = false;
					cols.add(buff.toString());
					buff = new StringBuilder();
				}
			} else {
				if (colExpected) {
					colStarted = true;
					buff.append(c);
				} else {
					break;
				}
			}
		}

		if (buff.length() != 0)
			if (colExpected)
				cols.add(buff.toString());
			else
				throw new SQLSyntaxException(query, i);

		parsePointer = i;
		return cols;
	}

	protected ArrayList<String> parseTableList() throws SQLSyntaxException {
		ArrayList<String> tables = new ArrayList<String>();

		StringBuilder buff = new StringBuilder();
		boolean tableQuot = false;
		boolean tableStarted = false;
		boolean tableExpected = true;
		boolean spaceEnc = false;

		int i = parsePointer;

		i = escapeSpaces(i);

		for (; i < query.length(); i++) {
			char c = query.charAt(i);
			if (tableQuot) {
				if (c == '`')
					tableQuot = false;
				else
					buff.append(c);
			} else if (c == '`') {
				tableQuot = true;
				tableStarted = true;
			} else if (c == ',') {
				if (tableStarted) {
					spaceEnc = false;
					tableStarted = false;
					tableExpected = true;
					tables.add(buff.toString());
					buff = new StringBuilder();
					i = escapeSpaces(i);
				} else if (spaceEnc) {
					spaceEnc = false;
					tableExpected = true;
				} else
					throw new SQLSyntaxException(query, i);
			} else if (Character.isWhitespace(query.charAt(i))) {
				if (tableStarted) {
					spaceEnc = true;
					tableStarted = false;
					tableExpected = false;
					tables.add(buff.toString());
					buff = new StringBuilder();
				}
			} else {
				if (tableExpected) {
					tableStarted = true;
					buff.append(c);
				} else {
					break;
				}
			}
		}

		if (tableExpected)
			if (buff.length() != 0)
				tables.add(buff.toString());
			else
				throw new SQLSyntaxException(query, i);

		parsePointer = i;

		return tables;
	}

	protected String parseTable() throws SQLSyntaxException {
		String table = null;
		StringBuilder buff = new StringBuilder();
		boolean tableQuot = false;
		boolean tableStarted = false;
		boolean tableExpected = true;

		int i = parsePointer;

		i = escapeSpaces(i);

		for (; i < query.length(); i++) {
			char c = query.charAt(i);
			if (tableQuot) {
				if (c == '`')
					tableQuot = false;
				else
					buff.append(c);
			} else if (c == '`') {
				tableQuot = true;
				tableStarted = true;
			} else if (Character.isWhitespace(query.charAt(i))) {
				if (tableStarted) {
					tableStarted = false;
					tableExpected = false;
					table = buff.toString();
					break;
				}
			} else {
				if (tableExpected) {
					tableStarted = true;
					buff.append(c);
				} else {
					break;
				}
			}
		}

		if (tableExpected)
			if (buff.length() != 0)
				table = buff.toString();

		i = escapeSpaces(i);

		parsePointer = i;

		return table;
	}

	protected ArrayList<String> parseValues() throws SQLSyntaxException {
		// TODO triple check for parse safety
		ArrayList<String> values = new ArrayList<String>();
		StringBuilder buff = new StringBuilder();
		char stQuot = '\0';
		boolean rowExpected = true;
		boolean rowStarted = false;
		boolean valStarted = false;
		boolean valExpected = false;
		boolean spaceEnc = false;
		boolean quotedVal = false;

		int i = parsePointer;

		i = escapeSpaces(i);

		for (; i < query.length(); i++) {
			char c = query.charAt(i);
			if (stQuot != '\0') {
				if (c == stQuot && query.charAt(i - 1) != '\\') {
					stQuot = '\0';
					valExpected = false;
				} else
					buff.append(c);
			} else if (c == '\'' || c == '"') {
				if (valExpected) {
					stQuot = c;
					quotedVal = true;
					valStarted = true;
					valExpected = false;
				} else
					throw new SQLSyntaxException(query, i);
			} else if (c == '(') {
				if (rowStarted)
					throw new SQLSyntaxException(query, i);
				rowStarted = true;
				valExpected = true;
			} else if (c == ')') {
				if (!rowStarted || valExpected)
					throw new SQLSyntaxException(query, i);
				rowExpected = false;
				valExpected = false;
				if (valStarted) {
					spaceEnc = false;
					valStarted = false;
					valExpected = false;
					String val = buff.toString();
					if (!(quotedVal || isNull(val) || isNumeric(val)))
						throw new SQLSyntaxException(query, i);
					if (!quotedVal && isNull(val))
						values.add(null);
					else
						values.add(unEscape(val));
					buff = new StringBuilder();
					quotedVal = false;
				}
			} else if (c == ',') {
				if (valStarted) {
					spaceEnc = false;
					valStarted = false;
					valExpected = true;
					String val = buff.toString();
					if (!(quotedVal || isNull(val) || isNumeric(val)))
						throw new SQLSyntaxException(query, i);
					if (!quotedVal && isNull(val))
						values.add(null);
					else
						values.add(unEscape(val));
					quotedVal = false;
					buff = new StringBuilder();
					i++;
					i = escapeSpaces(i);
					i--;
				} else if (spaceEnc) {
					spaceEnc = false;
					valExpected = true;
				} else
					throw new SQLSyntaxException(query, i);
			} else if (Character.isWhitespace(query.charAt(i))) {
				if (valStarted) {
					spaceEnc = true;
					valStarted = false;
					valExpected = false;
					String val = buff.toString();
					if (!(quotedVal || isNull(val) || isNumeric(val)))
						throw new SQLSyntaxException(query, i);
					if (!quotedVal && isNull(val))
						values.add(null);
					else
						values.add(unEscape(val));
					buff = new StringBuilder();
					quotedVal = false;
				}
			} else {
				if (valExpected || valStarted) {
					valStarted = true;
					valExpected = false;
					buff.append(c);
				} else {
					break;
				}
			}
		}

		if (valExpected || valStarted)
			if (buff.length() != 0) {
				String val = buff.toString();
				if (!(quotedVal || isNull(val) || isNumeric(val)))
					throw new SQLSyntaxException(query, i);
				if (!quotedVal && isNull(val))
					values.add(null);
				else
					values.add(unEscape(val));
			} else
				throw new SQLSyntaxException(query, i);

		if (rowExpected)
			throw new SQLSyntaxException(query, i);

		i = parsePointer;
		return values;
	}

	private static boolean isNumeric(String val) {
		try {
			Double.parseDouble(val);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	private boolean isNull(String val) {
		return (val.equalsIgnoreCase("null"));
	}

	protected ArrayList<String>[] parseFieldValue() throws SQLSyntaxException {
		ArrayList<String> fields = new ArrayList<String>();
		ArrayList<String> values = new ArrayList<String>();
		StringBuilder buff = new StringBuilder();
		char stQuot = '\0';
		boolean colQuot = false;
		boolean colStarted = false;
		boolean valStarted = false;
		boolean valExpected = false;
		boolean colExpected = true;
		boolean spaceEnc = false;
		boolean quotedVal = false;

		int i = parsePointer;

		i = escapeSpaces(i);

		for (; i < query.length(); i++) {
			char c = query.charAt(i);
			if (stQuot != '\0') {
				if (c == stQuot && query.charAt(i - 1) != '\\') {
					stQuot = '\0';
				} else
					buff.append(c);
			} else if (c == '\'' || c == '"') {
				if (colExpected || valStarted)
					throw new SQLSyntaxException(query, i);
				stQuot = c;
				valStarted = true;
				valExpected = false;
				quotedVal = true;
			} else if (colQuot) {
				if (c == '`') {
					colQuot = false;
					colStarted = false;
					valExpected = true;
				} else
					buff.append(c);
			} else if (c == '`') {
				if (valExpected)
					throw new SQLSyntaxException(query, i);
				colQuot = true;
				colStarted = true;
				colExpected = false;
			} else if (c == '=') {
				if (colStarted || valExpected) {
					spaceEnc = false;
					colStarted = false;
					colExpected = false;
					valExpected = true;
					valStarted = false;
					fields.add(buff.toString());
					buff = new StringBuilder();
					i = escapeSpaces(i);
				} else if (spaceEnc) {
					spaceEnc = false;
					valExpected = true;
				} else
					throw new SQLSyntaxException(query, i);
			} else if (c == ',') {
				if (valStarted) {
					spaceEnc = false;
					colStarted = false;
					colExpected = true;
					valStarted = false;
					valExpected = false;
					String val = buff.toString();
					if (!(quotedVal || isNull(val) || isNumeric(val)))
						throw new SQLSyntaxException(query, i);
					if (!quotedVal && isNull(val))
						values.add(null);
					else
						values.add(unEscape(val));
					quotedVal = false;
					buff = new StringBuilder();
					i = escapeSpaces(i);
				} else if (spaceEnc) {
					spaceEnc = false;
					colExpected = true;
				} else
					throw new SQLSyntaxException(query, i);
			} else if (Character.isWhitespace(query.charAt(i))) {
				if (valStarted) {
					spaceEnc = true;
					valStarted = false;
					valExpected = false;
					String val = buff.toString();
					if (!(quotedVal || isNull(val) || isNumeric(val)))
						throw new SQLSyntaxException(query, i);
					if (!quotedVal && isNull(val))
						values.add(null);
					else
						values.add(unEscape(val));
					quotedVal = false;
					buff = new StringBuilder();
				}

			} else {
				if (colExpected || colStarted) {
					colStarted = true;
					colExpected = false;
					buff.append(c);
				} else if (valExpected || valStarted) {
					valStarted = true;
					valExpected = false;
					buff.append(c);
				} else {
					break;
				}
			}
		}

		if (valExpected || valStarted)
			if (buff.length() != 0) {
				String val = buff.toString();
				if (!(quotedVal || isNull(val) || isNumeric(val)))
					throw new SQLSyntaxException(query, i);
				if (!quotedVal && isNull(val))
					values.add(null);
				else
					values.add(unEscape(val));
				quotedVal = false;
			} else
				throw new SQLSyntaxException(query, i);

		if (colExpected)
			throw new SQLSyntaxException(query, i);

		parsePointer = i;

		@SuppressWarnings("unchecked")
		ArrayList<String>[] list = new ArrayList[2];
		list[0] = fields;
		list[1] = values;
		return list;
	}

	protected String[][][] parseFieldsInfo() throws SQLSyntaxException {
		ArrayList<String[]> cols = new ArrayList<String[]>();
		int i = escapeSpaces(parsePointer);
		if (query.charAt(i++) != '(')
			throw new SQLSyntaxException(query, parsePointer);

		boolean colStarted = false;
		boolean colExpected = true;
		int colProgress = 0;
		String[] colInfo = new String[3];
		StringBuilder buff = new StringBuilder();
		String autoColumn = null;

		for (; i < query.length(); i++) {
			char c = query.charAt(i);

			if (c == ')') {
				if (colExpected && !colStarted)
					throw new SQLSyntaxException(query, i);

				if (buff.length() > 0) {
					if (colProgress >= 3)
						throw new SQLSyntaxException(query, i);
					else if (colProgress == 2) {
						if (buff.toString().equalsIgnoreCase("null"))
							colInfo[colProgress++] = "true";
						else if (buff.toString().equalsIgnoreCase(
								"auto_increment")) {
							if (autoColumn == null) {
								autoColumn = colInfo[0];
								colInfo[colProgress++] = "false";
							} else
								throw new SQLSyntaxException(
										"Incorrect table definition; there can be only one auto column and it must be defined as a key");
						} else
							throw new SQLSyntaxException(query, i);
					} else if (colProgress == 1) {
						int t;
						if ((t = SQLTypes.intType(buff.toString())) == -1)
							throw new SQLSyntaxException(query, i);

						colInfo[colProgress++] = t + "";
					} else
						colInfo[colProgress++] = buff.toString();
				}

				if (colProgress < 2)
					throw new SQLSyntaxException(query, i);
				else if (colProgress < 3)
					colInfo[2] = "false";
				
				if(!Character.isLetter(colInfo[0].charAt(0)))
					throw new SQLSyntaxException("Table column name must start with a letter");
				cols.add(colInfo);
				break;
			} else if (c == ',') {
				if (!colStarted)
					throw new SQLSyntaxException(query, i);

				if (buff.length() > 0) {
					if (colProgress >= 3)
						throw new SQLSyntaxException(query, i);
					else if (colProgress == 2) {
						if (buff.toString().equalsIgnoreCase("null"))
							colInfo[colProgress++] = "true";
						else if (buff.toString().equalsIgnoreCase(
								"auto_increment")) {
							if (autoColumn == null) {
								autoColumn = colInfo[0];
								colInfo[colProgress++] = "false";
							} else
								throw new SQLSyntaxException(
										"Incorrect table definition; there can be only one auto column and it must be defined as a key");
						} else
							throw new SQLSyntaxException(query, i);

					} else if (colProgress == 1) {
						int t;
						if ((t = SQLTypes.intType(buff.toString())) == -1)
							throw new SQLSyntaxException(query, i);

						colInfo[colProgress++] = t + "";
					} else
						colInfo[colProgress++] = buff.toString();

					buff = new StringBuilder();
				}

				if (colProgress < 2)
					throw new SQLSyntaxException(query, i);
				else if (colProgress < 3)
					colInfo[2] = "false";

				colStarted = false;
				colExpected = true;
				colProgress = 0;
				if(!Character.isLetter(colInfo[0].charAt(0)))
					throw new SQLSyntaxException("Table column name must start with a letter");
				cols.add(colInfo);
				colInfo = new String[3];
			} else if (Character.isWhitespace(query.charAt(i))) {
				if (colStarted && buff.length() > 0) {
					if (colProgress >= 3)
						throw new SQLSyntaxException(query, i);
					else if (colProgress == 2) {
						if (buff.toString().equalsIgnoreCase("null"))
							colInfo[colProgress++] = "true";
						else if (buff.toString().equalsIgnoreCase(
								"auto_increment")) {
							if (autoColumn == null) {
								autoColumn = colInfo[0];
								colInfo[colProgress++] = "false";
							} else
								throw new SQLSyntaxException(
										"Incorrect table definition; there can be only one auto column and it must be defined as a key");
						} else
							throw new SQLSyntaxException(query, i);
					} else if (colProgress == 1) {
						int t;
						if ((t = SQLTypes.intType(buff.toString())) == -1)
							throw new SQLSyntaxException(query, i);

						colInfo[colProgress++] = t + "";
					} else
						colInfo[colProgress++] = buff.toString();

					buff = new StringBuilder();
				}
			} else {
				colStarted = true;
				buff.append(c);
			}
		}

		String[][] info = new String[cols.size()][3];
		cols.toArray(info);
		String[][][] t = new String[2][][];
		t[0] = info;
		t[1] = new String[][] { { autoColumn } };
		return t;
	}

	protected boolean fromClauseFound() {
		parsePointer = escapeSpaces(parsePointer);
		if (parsePointer + "from ".length() <= query.length()) {
			if (query.substring(parsePointer,
					findFirstSpace(query, parsePointer)).equalsIgnoreCase(
					"from")) {
				parsePointer += "from ".length();
				return true;
			}
		}
		return false;
	}

	protected boolean intoClauseFound() {
		parsePointer = escapeSpaces(parsePointer);
		if (parsePointer + "into ".length() <= query.length()) {
			if (query.substring(parsePointer,
					findFirstSpace(query, parsePointer)).equalsIgnoreCase(
					"into")) {
				parsePointer += "into ".length();
				return true;
			}
		}
		return false;
	}

	protected boolean valuesClauseFound() {
		parsePointer = escapeSpaces(parsePointer);
		if (parsePointer + "values ".length() <= query.length()) {
			if (query.substring(parsePointer,
					findFirstSpace(query, parsePointer)).equalsIgnoreCase(
					"values")) {
				parsePointer += "values ".length();
				return true;
			}
		}
		return false;
	}

	protected boolean setClauseFound() {
		parsePointer = escapeSpaces(parsePointer);
		if (parsePointer + "set ".length() <= query.length()) {
			if (query.substring(parsePointer,
					findFirstSpace(query, parsePointer))
					.equalsIgnoreCase("set")) {
				parsePointer += "set ".length();
				return true;
			}
		}
		return false;
	}

	protected boolean whereClauseFound() {
		parsePointer = escapeSpaces(parsePointer);
		if (parsePointer + "where ".length() <= query.length()) {
			if (query.substring(parsePointer,
					findFirstSpace(query, parsePointer)).equalsIgnoreCase(
					"where")) {
				parsePointer += "where ".length();
				return true;
			}
		}
		return false;
	}

	protected boolean tableClauseFound() {
		parsePointer = escapeSpaces(parsePointer);
		if (parsePointer + "table ".length() <= query.length()) {
			if (query.substring(parsePointer,
					findFirstSpace(query, parsePointer)).equalsIgnoreCase(
					"table")) {
				parsePointer += "table ".length();
				return true;
			}
		}
		return false;
	}

	protected boolean parenthesisFound() {
		parsePointer = escapeSpaces(parsePointer);
		if(query.charAt(parsePointer) == '(')
			return true;
		else 
			return false;
	}

	public int getNumberOfRows() {
		return result;
	}

	private int escapeSpaces(int i) {
		for (; i < query.length() && Character.isWhitespace(query.charAt(i)); i++)
			;
		return i;
	}

	private static String unEscape(String s) {
		s = s.replaceAll("\\\\\"", "\"");
		return s.replaceAll("\\\\'", "'");
	}

}
