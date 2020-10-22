/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.query.impl;

import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class QueryStreamImpl<T> implements QueryStream<T> {
    private DataMapperImpl<T> dataMapper;
    private final List<Class<?>> eagers = new ArrayList<>();
    private Condition condition = Condition.TRUE;
    private Consumer<T> peeker;
    private long limit;
    private long skip;
    private Order[] orders = Order.NOORDER;
    private Boolean parallel;
    private final List<Runnable> runnables = new ArrayList<>();

    public QueryStreamImpl(DataMapperImpl<T> dataMapper) {
        this.dataMapper = dataMapper;
    }

    @Override
    public Stream<T> filter(Predicate<? super T> predicate) {
        if (condition == Condition.TRUE) {
            throw new IllegalStateException("Condition not yet set");
        }
        return stream().filter(predicate);
    }

    @Override
    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return stream().map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return stream().mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return stream().mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return stream().mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return stream().flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return stream().flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return stream().flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return stream().flatMapToDouble(mapper);
    }

    @Override
    public QueryStream<T> distinct() {
        // queries always return distinct values
        return this;
    }

    @Override
    public Stream<T> sorted() {
        return stream().sorted();
    }

    @Override
    public Stream<T> sorted(Comparator<? super T> comparator) {
        return stream().sorted(comparator);
    }

    @Override
    public QueryStream<T> peek(Consumer<? super T> action) {
        peeker = peeker == null ?
                (Consumer<T>) Objects.requireNonNull(action) :
                peeker.andThen(action);
        return this;
    }

    @Override
    public QueryStreamImpl<T> limit(long maxSize) {
        this.limit = maxSize;
        return this;
    }

    @Override
    public QueryStream<T> skip(long n) {
        this.skip = n;
        return this;
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        select().forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        select().forEach(action);
    }

    @Override
    public Object[] toArray() {
        return stream().toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return stream().toArray(generator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return stream().reduce(identity, accumulator);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return stream().reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return stream().reduce(identity, accumulator, combiner);

    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return stream().collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return stream().collect(collector);
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return stream().min(comparator);
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return stream().max(comparator);
    }

    @Override
    public long count() {
        return peeker == null ?
                dataMapper.query(eagers.toArray(new Class<?>[eagers.size()])).count(condition) :
                stream().peek(peeker).count();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return stream().anyMatch(predicate);
    }

    @Override
    public boolean anyMatch(Condition condition) {
        return filter(condition).findFirst().isPresent();
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return stream().allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return stream().noneMatch(predicate);
    }

    @Override
    public Optional<T> findFirst() {
        return limit(1).stream().findFirst();
    }

    @Override
    public Optional<T> findAny() {
        return findFirst();
    }

    @Override
    public Iterator<T> iterator() {
        return select().iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return select().spliterator();
    }

    @Override
    public boolean isParallel() {
        return false;
    }

    @Override
    public QueryStream<T> sequential() {
        parallel = Boolean.FALSE;
        return this;
    }

    @Override
    public QueryStream<T> parallel() {
        parallel = Boolean.TRUE;
        return this;
    }

    @Override
    public QueryStream<T> unordered() {
        orders = Order.NOORDER;
        return this;
    }

    @Override
    public QueryStream<T> onClose(Runnable closeHandler) {
        runnables.add(closeHandler);
        return this;
    }

    @Override
    public void close() {
        runnables.forEach(Runnable::run);
    }

    @Override
    public QueryStream<T> join(Class<?> clazz) {
        this.eagers.add(clazz);
        return this;
    }

    @Override
    public QueryStream<T> filter(Condition newCondition) {
        this.condition = condition.and(newCondition);
        return this;
    }

    @Override
    public <R extends T> QueryStream<R> filter(Class<? extends R> newApi) {
        dataMapper = (DataMapperImpl) dataMapper.subMapper(newApi);
        return (QueryStream) this;
    }

    @Override
    public QueryStream<T> sorted(Order order, Order... extra) {
        this.orders = new Order[extra.length + 1];
        this.orders[0] = order;
        System.arraycopy(extra, 0, orders, 1, extra.length);
        return this;
    }

    @Override
    public QueryStream<T> sorted(Collection<Order> orders) {
        this.orders = orders.toArray(new Order[orders.size()]);
        return this;
    }

    @Override
    public List<T> select() {
        List<T> result = doSelect();
        if (peeker != null) {
            result.forEach(peeker);
        }
        return result;
    }

    private List<T> doSelect() {
        List<T> result;
        QueryExecutorImpl<T> queryExecutor = dataMapper.query(eagers.toArray(new Class<?>[eagers.size()]));
        if (limit == 0) {
            result = queryExecutor.select(condition, orders);
        } else {
            result = queryExecutor.select(condition, orders, true, new String[0], (int) (skip + 1), (int) (skip + limit));
        }
        return result;
    }

    private Stream<T> stream() {
        Stream<T> result = doSelect().stream();
        if (peeker != null) {
            result = result.peek(peeker);
        }
        return parallel == null ?
                result :
                parallel ?
                        result.parallel() :
                        result.sequential();
    }
}
