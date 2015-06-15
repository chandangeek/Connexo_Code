package com.elster.jupiter.util.collections;

import com.elster.jupiter.util.Pair;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The DualIterable is a wrapper around two Iterables that will loop over matching pairs.
 * <br/>
 * It comes in two flavors (provided by the factory methods).
 * <br/>
 * The first, endWithShortest, will loop all pairs so that both wrapped Iterables will have contributed.
 * Once either wrapped Iterable runs out, the DualIterable will stop too.
 * <br/>
 * The second, endWithLogest, will loop and return pairs as long as at least one of both wrapped Iterables still has elements.
 * The pairs will be filled with null values, in case 1 wrapped Iterable has no more elements.
 * <br/>
 * In case you need to loop even more Iterables concurrently you can simply wrap DualIterables in other DualIterables.
 *
 * @author Tom De Greyt (tgr)
 */
public final class DualIterable<T, U> implements Iterable<Pair<T, U>> {
    private interface IteratingStrategy<T, U> extends Iterator<Pair<T, U>> {
    }

    private class EndWithShortest implements IteratingStrategy<T, U> {
        public boolean hasNext() {
            return first.hasNext() && second.hasNext();
        }

        public Pair<T, U> next() {
            return Pair.of(first.next(), second.next());
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class EndWithLongest implements IteratingStrategy<T, U> {
        public boolean hasNext() {
            return first.hasNext() || second.hasNext();
        }

        public Pair<T, U> next() {
            if (hasNext()) {
                T firstValue = first.hasNext() ? first.next() : null;
                U secondValue = second.hasNext() ? second.next() : null;
                return Pair.of(firstValue, secondValue);
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private IteratingStrategy<T, U> strategy;
    private Iterator<T> first;
    private Iterator<U> second;

    public static <T, U> DualIterable<T, U> endWithShortest(Iterable<T> first, Iterable<U> second) {
        DualIterable<T, U> dualIterator = new DualIterable<T, U>(first, second);
        dualIterator.setStrategy(dualIterator.new EndWithShortest());
        return dualIterator;
    }

    public static <T, U> DualIterable<T, U> endWithLongest(Iterable<T> first, Iterable<U> second) {
        DualIterable<T, U> dualIterator = new DualIterable<T, U>(first, second);
        dualIterator.setStrategy(dualIterator.new EndWithLongest());
        return dualIterator;
    }

    public Iterator<Pair<T, U>> iterator() {
        return strategy;
    }

    public Stream<Pair<T, U>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    private DualIterable(Iterable<T> first, Iterable<U> second) {
        this.first = first.iterator();
        this.second = second.iterator();
    }

    private void setStrategy(IteratingStrategy<T, U> strategy) {
        this.strategy = strategy;
    }
}
