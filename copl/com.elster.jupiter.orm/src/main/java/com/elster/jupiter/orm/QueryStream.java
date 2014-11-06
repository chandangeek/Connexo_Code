package com.elster.jupiter.orm;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

public interface QueryStream<T> extends Stream<T> {

	// additional methods
	QueryStream<T> join(Class<?> clazz);
	QueryStream<T> filter(Condition condition);
	QueryStream<T> sorted(Order order, Order ...orders);
	/*
	 * more performant version of collect(Collectors.toList())
	 */
	List<T> select();
	
	// covariant return types
	@Override
	QueryStream<T> distinct();
	@Override
	QueryStream<T> skip(long n);
	@Override
	QueryStream<T> limit(long limit);
	@Override
	QueryStream<T> peek(Consumer<? super T> action);
	@Override
	QueryStream<T> sequential();
	@Override
	QueryStream<T >parallel();
	@Override
	QueryStream<T> unordered();
	@Override
	QueryStream<T> onClose(Runnable closeHandler);
	
}
