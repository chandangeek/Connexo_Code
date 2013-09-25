package com.elster.jupiter.orm.impl;

import java.util.Arrays;

final class CompositePrimaryKey {
	
	final Object[] key;
	
	CompositePrimaryKey(Object... values) {
		this.key = values;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
            return true;
        }
        if (!(other instanceof CompositePrimaryKey)) {
            return false;
        }
        return Arrays.equals(key, ((CompositePrimaryKey) other).key);
	}
	
	@Override 
	public int hashCode() {
		return Arrays.hashCode(key);
	}
}
