package sql;

import java.sql.SQLSyntaxErrorException;

public class SQLSyntaxException extends SQLSyntaxErrorException {
	private static final long serialVersionUID = 1L;
	private int pointer;
	
	public SQLSyntaxException(String query, int location) {		
		super("You have an error in your SQL syntax; near '"+
				query.substring(Math.max(0, location - 10), query.length()) + "'");
		pointer = location;
	}
	
	public SQLSyntaxException(String error){
		super(error);
	}

	public int getPointer() {
		return pointer;
	}

}
