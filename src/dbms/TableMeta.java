package dbms;

public class TableMeta {

	private String[][] infoArray;
	private String name;
	private String autoColumn;
	public int arrayIndex = 0;
	private String[] keyArray;
	private Table table;

	/*
	 * set the array of info
	 */
	public void setInfoArray(String[][] array) {
		infoArray = array;
	}

	public String getName() {
		return name;
	}
	public String [] getKeyArray()
	{
		return keyArray;
	}

	/*
	 * return the info array
	 */
	public String[][] getInfoArray() {
		return infoArray;
	}

	public TableMeta(String name, Table table) {
		this.name = name;
		this.table = table;
		keyArray = new String[5];
	}
	
	public TableMeta(String name){
		this.name = name;		
		keyArray = new String[5];
	}

	public void setName(String name) {
		this.name = name;
	}

	public String tableName() {
		// 
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TableMeta#hasColumn(java.lang.String) check whether the file contain
	 * a column
	 */

	public boolean hasColumn(String colName) {
		for (int i = 0; i < arrayIndex; i++) {
			if (infoArray[i][0].equals(colName))
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TableMeta#autoColumn() return the auto column
	 */

	public String autoColumn() {
		// 
		return autoColumn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TableMeta#isAuto(java.lang.String) return whether the the file is
	 * auto or not
	 */

	public boolean isAuto(String colName) {
		// 
		if (colName.equals(autoColumn))
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TableMeta#isUnique(java.lang.String) return whether this object is
	 * unique or not
	 */

	public boolean isUnique(String colName) {
		// 
		if (colName.equals(autoColumn))
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TableMeta#isNullable(java.lang.String) return whether this object is
	 * nullable or not
	 */

	public boolean isNullable(String colName) {
		// 
		for (int i = 0; i < arrayIndex; i++) {
			if (infoArray[i][0].equals(colName))
				if (infoArray[i][2].equals("true")) {
					return true;
				} else {
					return false;
				}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TableMeta#colIndex(java.lang.String) return the index of a given
	 * String
	 */

	public int colIndex(String colIndex) {
		// 
		for (int i = 0; i < infoArray.length; i++) {
			if (infoArray[i][0].equals(colIndex))
				return i;
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TableMeta#colAtIndex(int) return the index of a given String
	 */

	public String colAtIndex(int index) {
		// 
		return infoArray[index][0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TableMeta#colType(java.lang.String) return the type of a given
	 * String
	 */

	public int colType(String colName) {
		// 
		for (int i = 0; i < infoArray.length; i++) {
			if (infoArray[i][0].equals(colName))
				return Integer.parseInt(infoArray[i][1]);
		}

		return -1;
	}

	public String colType(int index) {
		// 

		return infoArray[index][1];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TableMeta#addColumn(java.lang.String, boolean, java.lang.String) Add
	 * a full column to the meta data file
	 */

	public void addColumn(String colName, boolean nullable, String type)
			throws Exception {
		// 
		if (arrayIndex < infoArray.length) {
			copyElements(colName, nullable, type);
		} else {
			String[][] temp = new String[infoArray.length + 10][3];
			for (int i = 0; i < infoArray.length; i++) {
				for (int j = 0; j < infoArray[i].length; j++)
					temp[i][j] = infoArray[i][j];
			}
			infoArray = temp;
			copyElements(colName, nullable, type);
		}
		
		table.addColumn(colName, nullable, type);

	}

	/*
	 * copy the elements to the array
	 */
	private void copyElements(String colName, boolean nullable, String type) {
		infoArray[arrayIndex][0] = colName;
		infoArray[arrayIndex][2] = Boolean.toString(nullable);
		infoArray[arrayIndex][1] = type;
		arrayIndex++;
	}

	/*
	 * Method that take the key name and the col name and add it to the array
	 */
	public void addKey(String keyName, String colName) throws Exception {
		// 
		for (int i = 0; i < keyArray.length; i++) {
			if (keyArray[i] == null) {
				keyArray[i] = keyName + "," + colName;
				break;
			}
			if (i == keyArray.length - 1) {
				String test[] = new String[keyArray.length * 2];
				System.arraycopy(keyArray, 0, test, 0, keyArray.length);
				keyArray = test;
			}
		}
		
		table.addKey(keyName, colName);
	}

	public void addKeyArray(String keyName, String colName) throws Exception {
		// 
		for (int i = 0; i < keyArray.length; i++) {
			if (keyArray[i] == null) {
				keyArray[i] = keyName + "," + colName;
				return;
			}
			if (i == keyArray.length - 1) {
				String test[] = new String[keyArray.length * 2];
				System.arraycopy(keyArray, 0, test, 0, keyArray.length);
				keyArray = test;
			}
		}
	}

	public int numberOfColumns() {
		return infoArray.length;
	}

	/*
	 * Method that take the key Name and return the column
	 */
	public String getcolumnName(String keyName) {
		String key = "";
		for (int i = 0; i < keyArray.length; i++) {
			if (keyArray[i].contains(keyName)) {
				key = getKeyCol(i);
				break;
			}
		}
		return key;
	}

	public String getKeyName(String colName) {
		String key = "";
		for (int i = 0; i < keyArray.length; i++) {
			if (keyArray[i].contains(colName)) {
				key = getKeyName(i);
				break;
			}
		}
		return key;

	}

	/*
	 * Take an integer and return the keyName at this pos
	 */
	private String getKeyName(int pos) {
		String temp = "";
		for (int i = 0; i < keyArray[pos].length(); i++) {
			if (keyArray[pos].charAt(i) == ',')
				return temp;
			else
				temp += keyArray[pos].charAt(i);
		}
		return temp;
	}

	/*
	 * Take an integer and return the keycol in this pos
	 */
	private String getKeyCol(int pos) {
		String temp = "";
		for (int i = keyArray[pos].length() - 1; i > 0; i--) {
			if (keyArray[pos].charAt(i) == ',')
				return temp;
			else
				temp = keyArray[pos].charAt(i) + temp;
		}
		return temp;

	}

	public void setAutoColumn(String colName) {
		autoColumn = colName;
	}


	public String[] getColumnNames(){
		String[] names = new String[infoArray.length];
		for(int i = 0; i < infoArray.length; i++){
			names[i] = infoArray[i][0];
		}
		
		return names;
	}

}
