package com.elster.jupiter.orm.query.impl;


public abstract class JoinTreeAction<T> {
	private final boolean mark;
	private final boolean clear;
	
	JoinTreeAction(boolean mark, boolean clear) {
		this.mark = mark;
		this.clear = clear;
		if (mark && clear) {
			throw new IllegalArgumentException("You cannot have both");
		}
	}
	
	final boolean mark() {
		return mark;
	}
	
	final boolean clear() {
		return clear;
	}
	
	final boolean proceed(T lastResult) {
		return lastResult == null || (lastResult instanceof Boolean && !((Boolean) lastResult).booleanValue());
	}
	
	abstract T invoke(String fieldName , JoinDataMapper<?> value);	
	
}
