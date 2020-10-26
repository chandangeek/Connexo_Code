/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import aQute.bnd.annotation.ProviderType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

    <R extends T> QueryStream<R> filter(Class<? extends R> newApi);

    /*
     * sort the result
     */
    QueryStream<T> sorted(Order order, Order... orders);

    QueryStream<T> sorted(Collection<Order> orders);

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

    default Optional<T> min(String field, String... fields) {
        return sorted(Order.ascending(field), Arrays.stream(fields).map(Order::ascending).toArray(Order[]::new))
                .findFirst();
    }

    default Optional<T> max(String field, String... fields) {
        return sorted(Order.descending(field), Arrays.stream(fields).map(Order::descending).toArray(Order[]::new))
                .findFirst();
    }
}
