package com.elster.jupiter.orm;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

/*
 * Stream alternative for QueryExecutor
 */
@ProviderType
public interface QueryStream<T> extends Stream<T> {

	/*
	 * eagerly add types to the query
	 */
	QueryStream<T> join(Class<?> clazz);
	/*
	 * adds a query filter condition
	 */
	QueryStream<T> filter(Condition condition);
	/*
	 * sort the result
	 */
	QueryStream<T> sorted(Order order, Order ...orders);
	/*
	 * checks if any tuples matches the condition
	 */
	boolean anyMatch(Condition condition);
	/*
	 * more performant version of collect(Collectors.toList())
	 */
	List<T> select();

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
	QueryStream<T> parallel();
	@Override
	QueryStream<T> unordered();
	@Override
	QueryStream<T> onClose(Runnable closeHandler);

}
