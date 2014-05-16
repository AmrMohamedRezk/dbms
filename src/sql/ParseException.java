package sql;

class ParseException extends Exception {

	private static final long serialVersionUID = 1L;
	private long column;
	
	public ParseException(long column){
		this.column = column;
	}
	
	public void setColumn(long column){
		this.column = column;
	}

	public long getColumn(){
		return column;
	}
}
