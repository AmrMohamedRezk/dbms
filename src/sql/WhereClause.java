package sql;

import java.util.ArrayList;
import java.util.Arrays;

class WhereClause {

	private static final int LogicalOp = 0;
	private static final int RelationalOp = 1;
	private static final int NegateOp = 2;
	private static final int IsOp = 3;
	private static final int Const = 4;
	private static final String[] LogicalOperators = { "and", "or" };
	private static final String[] RelationalOperators = { "=", "<>", ">", "<",
			">=", "<=" };

	private Condition cond;

	private String query;
	private int pointer;
	private ArrayList<String> fields = new ArrayList<String>();

	protected WhereClause(String query, int pointer) throws SQLSyntaxException {
		this.query = query;
		this.pointer = pointer;
		parse();
	}

	protected WhereClause() {
		this.cond = null;
	}

	public boolean parse() throws SQLSyntaxException {

		StringBuilder buff = new StringBuilder();
		cond = new Condition(null, null, null);
		Condition rel = cond;
		char stQuot = '\0';
		boolean opEnc = false;
		boolean append;
		boolean quotedVal = false;

		int i = pointer;

		for (; i < query.length(); i++) {
			char c = query.charAt(i);
			append = false;
			if (stQuot != '\0') {
				append = true;
				buff.append(c);				
				if (c == stQuot && query.charAt(i - 1) != '\\') {
					stQuot = '\0';
				}
			} else if (c == '\'' || c == '"') {
				buff.append(c);
				quotedVal = true;
				if (opEnc) {
					i--;
				} else {
					append = true;
					stQuot = c;
				}
			} else if (c == '=' || c == '<' || c == '>') {
				if (!opEnc) {
					i--;
					opEnc = true;
				} else {
					buff.append(c);
					append = true;
				}
			} else if (!Character.isWhitespace(c)) {
				if (opEnc) {
					i--;
				} else {
					buff.append(c);
					append = true;
				}
			}

			if (((!append) || (i == query.length() - 1)) && buff.length() != 0) {
				String entity = buff.toString();
				switch (entityType(entity)) {
				case LogicalOp:
					if (rel.rel == null || rel.x == null || rel.y == null) // uncomplete
						throw new SQLSyntaxException(query, i);
					else {
						cond = new Condition(entity, cond, null);
						rel = new Condition(null, null, null);
						cond.cond2 = rel;
					}
					break;
				case RelationalOp:
					if (rel.rel == null)
						rel.rel = (entity.equals("<>")) ? ("!=") : (entity);
					else
						throw new SQLSyntaxException(query, i);
					opEnc = false;
					break;
				case Const:					
					if (rel.x == null) {
						if(quotedVal || isNumeric(entity))
							throw new SQLSyntaxException(query, i);
						fields.add(entity);
						rel.x = entity;						
					} else if (rel.y == null){
						if(!quotedVal && !isNumeric(entity))
							fields.add(entity);
						rel.y = entity;
					}else
						throw new SQLSyntaxException(query, i);
					break;
				}
				quotedVal = false;
				buff = new StringBuilder();
			}

		}
		return true;
	}

	public int entityType(String entity) {
		if (Arrays.asList(LogicalOperators).contains(entity.toLowerCase()))
			return LogicalOp;
		else if (Arrays.asList(RelationalOperators).contains(
				entity.toLowerCase()))
			return RelationalOp;
		else if (entity.equalsIgnoreCase("not"))
			return NegateOp;
		else if (entity.equalsIgnoreCase("is"))
			return IsOp;
		else
			return Const;
	}

	public String toString() {
		return cond.toString();
	}

	public String[] fieldList() {
		String[] f = new String[fields.size()];
		fields.toArray(f);
		return f;
	}

	public String xPath() {
		if (cond == null) {
			return "";
		} else {
			return "[" + cond.xPath() + "]";
		}
	}
	
	public String xPath(String table, String[] cols) {
		if (cond == null) {
			return "";
		} else {
			String c = cond.xPath(table, cols);
			if(c != null)
				return "[" + cond.xPath(table, cols) + "]";
			else
				return "";
		}
	}
	
	private static boolean isNumeric(String val) {
		try {
			Double.parseDouble(val);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

}
