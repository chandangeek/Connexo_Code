/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.query.impl;

import java.util.function.BiFunction;


class JoinTreeAction<T> {
	private final boolean mark;
	private final boolean clear;
	private final BiFunction<JoinDataMapper<?>, String, T> biFunction; 
	
	private JoinTreeAction(boolean mark, boolean clear, BiFunction<JoinDataMapper<?> , String, T> biFunction) {
		this.mark = mark;
		this.clear = clear;
		this.biFunction = biFunction;
		if (mark && clear) {
			throw new IllegalArgumentException("You cannot have both");
		}
	}
	
	void matched(JoinTreeNode<?> node) {
		if (mark) {
			node.mark();
		}
		if (clear) {
			node.clear();
		}
	}
	
	boolean isValid(T lastResult) {
		return lastResult !=  null && lastResult != Boolean.FALSE;
	}
	
	T apply(JoinDataMapper<?> value, String reduced) {
		return biFunction.apply(value, reduced);
	}
	
	static <T> JoinTreeAction<T> mark(BiFunction<JoinDataMapper<?>, String, T> biFunction) {
		return new JoinTreeAction<>(true, false, biFunction);
	}
	
	static <T> JoinTreeAction<T> clear(BiFunction<JoinDataMapper<?>, String, T> biFunction) {
		return new JoinTreeAction<>(false, true, biFunction);
	}
	
	static <T> JoinTreeAction<T> find(BiFunction<JoinDataMapper<?>, String, T> biFunction) {
		return new JoinTreeAction<>(false, false, biFunction);
	}
	
}
