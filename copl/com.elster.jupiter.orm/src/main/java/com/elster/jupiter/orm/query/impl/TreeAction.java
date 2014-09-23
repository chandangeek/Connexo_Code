package com.elster.jupiter.orm.query.impl;

import java.util.function.BiFunction;

public class TreeAction<T> extends JoinTreeAction<T> {
	
	private final BiFunction<String, JoinDataMapper<?>, T> biFunction; 

	private TreeAction(boolean mark, boolean clear, BiFunction<String, JoinDataMapper<?>, T> biFunction) {
		super(mark,clear);
		this.biFunction = biFunction;
	}

	@Override
	T invoke(String fieldName, JoinDataMapper<?> value) {
		return biFunction.apply(fieldName, value);
	}	
	
	static <T> TreeAction<T> mark(BiFunction<String, JoinDataMapper<?>, T> biFunction) {
		return new TreeAction<>(true, false, biFunction);
	}
	
	static <T> TreeAction<T> clear(BiFunction<String, JoinDataMapper<?>, T> biFunction) {
		return new TreeAction<>(false, true, biFunction);
	}
	
	static <T> TreeAction<T> find(BiFunction<String, JoinDataMapper<?>, T> biFunction) {
		return new TreeAction<>(false, false, biFunction);
	}
	
}
