package com.elster.jupiter.orm.query.impl;

import java.util.ArrayList;
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

import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

public class QueryStreamImpl<T> implements QueryStream<T> {

	private final DataMapperImpl<T> dataMapper;
	private final List<Class<?>> eagers = new ArrayList<>();
	private Optional<Condition> condition = Optional.empty(); 
	private Optional<Consumer<T>> peeker = Optional.empty();
	private long limit;
	private long skip;
	private Order[] orders = new Order[0];
	private boolean parallel = false;
	private final List<Runnable> runnables = new ArrayList<>();
	
	public QueryStreamImpl(DataMapperImpl<T> dataMapper) {
		this.dataMapper = dataMapper;
	}
	
	@Override
	public Stream<T> filter(Predicate<? super T> predicate) {
		return condition.map(ignored -> stream().filter(predicate)).orElseThrow(() -> new IllegalStateException("Condition not yet set"));
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

	public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
		return stream().flatMapToDouble(mapper);
	}

	public QueryStream<T> distinct() {
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

	@SuppressWarnings("unchecked")
	@Override
	public QueryStream<T> peek(Consumer<? super T> action) {
		Consumer<T> newAction = (Consumer<T>) Objects.requireNonNull(action);
		peeker = Optional.of(peeker.map(peek -> peek.andThen(newAction)).orElse(newAction));
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
		return stream().count();
	}

	@Override
	public boolean anyMatch(Predicate<? super T> predicate) {
		return stream().anyMatch(predicate);
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
		return this;
	}

	@Override
	public QueryStream<T> parallel() {
		return this;
	}

	@Override
	public QueryStream<T> unordered() {
		return this;
	}

	@Override
	public QueryStream<T> onClose(Runnable closeHandler) {
		runnables.add(closeHandler);
		return this;
	}

	@Override
	public void close() {
		runnables.forEach(runnable -> runnable.run());
	}

	@Override
	public QueryStream<T> join(Class<?> clazz) {
		this.eagers.add(clazz);
		return this;
	}

	@Override
	public QueryStream<T> filter(Condition newCondition) {
		this.condition = Optional.of(condition.map(current -> current.and(newCondition)).orElse(newCondition));
		return this;
	}

	@Override
	public QueryStream<T> sorted(Order order, Order... extra) {
		this.orders = new Order[extra.length + 1];
		this.orders[0] = order;
		System.arraycopy(extra, 0, orders, 1, extra.length);
		return this;
	}

	@Override
	public List<T> select() {
		List<T> result = doSelect();
		peeker.ifPresent(consumer -> result.forEach(consumer));
		return result;
	}
	
	private List<T> doSelect() {
		List<T> result;
		Condition condition = this.condition.orElse(Condition.TRUE);
		if (limit == 0) {
			result = dataMapper.query(eagers.toArray(new Class<?>[eagers.size()])).select(condition,orders);
		} else {
			result = dataMapper.query(eagers.toArray(new Class<?>[eagers.size()])).select(condition,orders,true,new String[0], (int) (skip + 1) , (int) (skip + limit));
		}
		return result;
	}
	
	private Stream<T> stream() {
		Stream<T> result = doSelect().stream();
		peeker.ifPresent(consumer -> result.peek(consumer));
		return parallel ? result.parallel() : result;
	}
}
