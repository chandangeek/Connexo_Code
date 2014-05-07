package com.elster.jupiter.util.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import static com.elster.jupiter.util.Checks.is;

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
public final class DualIterable<T, U> implements Iterable<DualIterable.Pair<T, U>> {
    private interface IteratingStrategy<T, U> extends Iterator<Pair<T, U>> {
    }

    private class EndWithShortest implements IteratingStrategy<T, U> {
        public boolean hasNext() {
            return first.hasNext() && second.hasNext();
        }

        public Pair<T, U> next() {
            return pairOf(first.next(), second.next());
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
                return pairOf(firstValue, secondValue);
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public interface Pair<A, B> {
        A getFirst();
        B getLast();
    }

    private static class SimplePair<F, L> implements Pair<F, L> {
        private static final int PRIME_FOR_HASH = 31;

        private final F first;
        private final L last;

        public SimplePair(F first, L last) {
            this.first = first;
            this.last = last;
        }

        @Override
        public final boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Pair)) {
                return false;
            }

            Pair<?, ?> other = (Pair<?, ?>) obj;

            return is(first).equalTo(other.getFirst()) && is(last).equalTo(other.getLast());
        }

        public F getFirst() {
            return first;
        }

        public L getLast() {
            return last;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(first, last);
        }

        @Override
        public String toString() {
            return '(' + safeToString(first) + ',' + safeToString(last) + ')';
        }

        private String safeToString(Object object) {
            return String.valueOf(object);
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

    private DualIterable(Iterable<T> first, Iterable<U> second) {
        this.first = first.iterator();
        this.second = second.iterator();
    }

    private SimplePair<T, U> pairOf(T firstValue, U secondValue) {
        return new SimplePair<>(firstValue, secondValue);
    }

    private void setStrategy(IteratingStrategy<T, U> strategy) {
        this.strategy = strategy;
    }
}
