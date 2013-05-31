package com.elster.jupiter.util.conditions;

import com.elster.jupiter.util.sql.SqlFragment;

public class Expression {
	
	private Expression() {
		// pure static class
	}
	
	public static Text create(String text) {
		return new Text(text);
	}
	
	public static FragmentExpression create(SqlFragment fragment) {
		return new FragmentExpression(fragment);
	}
	
}
