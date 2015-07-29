package com.elster.jupiter.util.streams;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
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
import java.util.stream.StreamSupport;

public class DecoratedStream<T> implements Stream<T> {

    private final Stream<T> decorated;

    private DecoratedStream(Stream<T> decorated) {
        this.decorated = decorated;
    }

    public static <X> DecoratedStream<X> decorate(Stream<X> stream) {
        return new DecoratedStream<>(stream);
    }

    public DecoratedStream<T> filter(Predicate<? super T> predicate) {
        return new DecoratedStream<>(decorated.filter(predicate));
    }

    public <R> DecoratedStream<R> map(Function<? super T, ? extends R> mapper) {
        return new DecoratedStream<>(decorated.map(mapper));
    }

    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return decorated.mapToInt(mapper);
    }

    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return decorated.mapToLong(mapper);
    }

    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return decorated.mapToDouble(mapper);
    }

    public <R> DecoratedStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return new DecoratedStream<>(decorated.flatMap(mapper));
    }

    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return decorated.flatMapToInt(mapper);
    }

    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return decorated.flatMapToLong(mapper);
    }

    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return decorated.flatMapToDouble(mapper);
    }

    @Override
    public DecoratedStream<T> distinct() {
        return new DecoratedStream<>(decorated.distinct());
    }

    public <R> DecoratedStream<T> distinct(Function<? super T, ? extends R> property) {
        return new DecoratedStream<>(
            decorated
                .map(v -> KeyValue.of(v, property))
                .distinct()
                .map(KeyValue::getValue)
        );
    }

    private static final class KeyValue<K, V> {
        private final K key;
        private final V value;

        private static <KK, VV> KeyValue<KK, VV> of(VV value, Function<? super VV, ? extends KK> keyGetter) {
            return new KeyValue<>(keyGetter.apply(value), value);
        }

        private KeyValue(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            KeyValue keyValue = (KeyValue) o;

            return !(key != null ? !key.equals(keyValue.key) : keyValue.key != null);

        }

        @Override
        public int hashCode() {
            return key != null ? key.hashCode() : 0;
        }
    }

    @Override
    public DecoratedStream<T> sorted() {
        return new DecoratedStream<>(decorated.sorted());
    }

    public DecoratedStream<T> sorted(Comparator<? super T> comparator) {
        return new DecoratedStream<>(decorated.sorted(comparator));
    }

    public DecoratedStream<T> peek(Consumer<? super T> action) {
        return new DecoratedStream<>(decorated.peek(action));
    }

    @Override
    public DecoratedStream<T> limit(long maxSize) {
        return new DecoratedStream<>(decorated.limit(maxSize));
    }

    public DecoratedStream<T> takeWhile(Predicate<? super T> predicate) {
        return new DecoratedStream<>(StreamSupport.stream(new ConditionedLimitSpliterator<>(decorated.spliterator(), predicate), decorated.isParallel()));
    }

    @Override
    public DecoratedStream<T> skip(long n) {
        return new DecoratedStream<>(decorated.skip(n));
    }

    public void forEach(Consumer<? super T> action) {
        decorated.forEach(action);
    }

    public void forEachOrdered(Consumer<? super T> action) {
        decorated.forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return decorated.toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return decorated.toArray(generator);
    }

    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return decorated.reduce(identity, accumulator);
    }

    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return decorated.reduce(accumulator);
    }

    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return decorated.reduce(identity, accumulator, combiner);
    }

    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return decorated.collect(supplier, accumulator, combiner);
    }

    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return decorated.collect(collector);
    }

    public Optional<T> min(Comparator<? super T> comparator) {
        return decorated.min(comparator);
    }

    public Optional<T> max(Comparator<? super T> comparator) {
        return decorated.max(comparator);
    }

    @Override
    public long count() {
        return decorated.count();
    }

    public boolean anyMatch(Predicate<? super T> predicate) {
        return decorated.anyMatch(predicate);
    }

    public boolean allMatch(Predicate<? super T> predicate) {
        return decorated.allMatch(predicate);
    }

    public boolean noneMatch(Predicate<? super T> predicate) {
        return decorated.noneMatch(predicate);
    }

    @Override
    public Optional<T> findFirst() {
        return decorated.findFirst();
    }

    @Override
    public Optional<T> findAny() {
        return decorated.findAny();
    }

    @Override
    public Iterator<T> iterator() {
        return decorated.iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return decorated.spliterator();
    }

    @Override
    public boolean isParallel() {
        return decorated.isParallel();
    }

    @Override
    public Stream<T> sequential() {
        return decorated.sequential();
    }

    @Override
    public Stream<T> parallel() {
        return decorated.parallel();
    }

    @Override
    public Stream<T> unordered() {
        return decorated.unordered();
    }

    @Override
    public Stream<T> onClose(Runnable closeHandler) {
        return new DecoratedStream<>(decorated.onClose(closeHandler));
    }

    public <S extends T> DecoratedStream<S> filterSubType(Class<S> clazz) {
        return decorate(decorated.filter(clazz::isInstance).map(clazz::cast));
    }

    @Override
    public void close() {
        decorated.close();
    }

    /**
     * @param n number of elements per list
     * @return a DecoratedStream of List of T, where each list will contain n elements, except perhaps the last, which will simply contain the remaining elements.
     */
    public DecoratedStream<List<T>> partitionPer(int n) {
        return new DecoratedStream<>(StreamSupport.stream(new GroupPerSpliterator<T>(decorated.spliterator(), n), decorated.isParallel()));
    }

    public DecoratedStream<List<T>> partitionWhen(BiPredicate<? super T, ? super T> startNewPartition) {
        return new DecoratedStream<>(StreamSupport.stream(new PartitionWhenSpliterator<T>(decorated.spliterator(), startNewPartition), decorated.isParallel()));
    }
}
