/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import java.util.Arrays;
import java.util.Objects;

public final class KeyValue {

	private final Object[] key;

	private KeyValue(Object[] values) {
		this.key = values;
	}

	static KeyValue of(Object[] key) {
		return new KeyValue(Objects.requireNonNull(key));
	}

	@Override
	public boolean equals(Object other) {
        if (this == other) {
        	return true;
	    }
	    if (!(other instanceof KeyValue)) {
	    	return false;
	    }
	    return Arrays.equals(key, ((KeyValue) other).key);
		}

	@Override
	public int hashCode() {
		return Arrays.hashCode(key);
	}

	public Object[] getKey() {
		return key;
	}

	public Object get(int index) {
		return key[index];
	}

	public boolean isNullAllowZero() {
		for (int i = 0 ; i < key.length ; i++) {
			if (key[i] == null) {
				return true;
			}
		}
		return false;
	}

	public boolean isNull() {
		if (isNullAllowZero()) {
			return true;
		}
		if (key[0] instanceof Number) {
			return ((Number) key[0]).longValue() == 0;
		} else {
			return false;
		}
	}

	public long getId() {
		if (key[0] instanceof Number) {
			return ((Number) key[0]).longValue();
		} else {
			return 0;
		}
	}

	public int size() {
		return key.length;
	}

	@Override
	public String toString() {
		return "KeyValue: " + Arrays.toString(key);
	}

}