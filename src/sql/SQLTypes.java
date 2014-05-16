package sql;

import java.lang.reflect.Field;

public class SQLTypes {
	
	//TODO limit to supported types only :)
	public static int intType(String type){
		if(type.equalsIgnoreCase("int"))
			type = "integer";
		
		Field[] fields = java.sql.Types.class.getFields();
		for(int i = 0; i < fields.length; i++)
			if(fields[i].getName().equalsIgnoreCase(type))
				try {
					return fields[i].getInt(null);
				} catch (IllegalArgumentException e) {
					
				} catch (IllegalAccessException e) {
					
				}
		
		return -1;
	}
}
