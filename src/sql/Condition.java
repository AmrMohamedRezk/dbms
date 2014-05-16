package sql;

import java.util.Arrays;

class Condition {
	public String comOperator;
	public Condition cond1;
	public Condition cond2;

	public String x;
	public String y;
	public String rel;

	public boolean negate;
	public boolean isRel;
	public boolean isLog;

	public Condition(String op, Condition con1, Condition con2) {
		comOperator = op;
		cond1 = con1;
		cond2 = con2;
	}

	public String toString() {
		if (comOperator != null) {
			return cond1.toString() + comOperator + cond2.toString();
		} else if (rel != null) {
			return x + rel + y;
		} else {
			return null;
		}
	}

	public String xPath() {
		if (comOperator != null) {
			return cond1.xPath() + " " + comOperator.toLowerCase() + " "
					+ cond2.xPath();
		} else if (rel != null) {
			return x + rel + y;
		} else {
			return null;
		}
	}

	public String xPath(String table, String[] cols) {
		if (comOperator != null) {
			String c1 = cond1.xPath(table, cols);
			String c2 = cond2.xPath(table, cols);
			
			if(comOperator.toLowerCase() == "or")
				return null;

			if (c1 == null && c2 == null)
				return null;
			else if (c1 == null)
				return c2;
			else if (c2 == null)
				return c1;
			else
				return c1 + " " + comOperator.toLowerCase() + " " + c2;

		} else if (rel != null) {
			String cond = "";

			int point = x.indexOf('.');
			if ((point > 0 && x.substring(0, point).equals(table))
					|| Arrays.asList(cols).contains(x)) {
				cond += (point > 0)?(x.substring(point+1)):(x);
			} else {
				return null;
			}

			cond += rel;

			if (y.charAt(0) == '\'' || y.charAt(0) == '\"' || isNumeric(y))
				cond += y;
			else {
				point = y.indexOf('.');
				if ((point > 0 && y.substring(0, point).equals(table))
						|| Arrays.asList(cols).contains(y)) {
					cond += (point > 0)?(y.substring(point+1)):(y);
				} else {
					return null;
				}
			}

			return cond;
		} else {
			return null;
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
