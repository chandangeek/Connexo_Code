package com.elster.jupiter.orm.impl;

import java.util.Arrays;

class CompositePrimaryKey {
	
	final Object[] key;
	
	CompositePrimaryKey(Object... values) {
		this.key = values;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		try {
			CompositePrimaryKey o = (CompositePrimaryKey) other;
			return Arrays.equals(key,o.key);
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	@Override 
	public int hashCode() {
		return Arrays.hashCode(key);
	}
}
