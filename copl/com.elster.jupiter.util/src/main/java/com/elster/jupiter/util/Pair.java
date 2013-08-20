package com.elster.jupiter.util;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.elster.jupiter.util.Checks.is;

/**
 * A pair of values, denoted first, and last.
 * A pair is immutable, in as far as its elements are immutable.
 * Two pairs are equal if their respective elements are equal.
 *
 * @param <F> type of the first value
 * @param <L> type of the last value.
 */
public final class Pair<F, L> {

    private final F first;
    private final L last;

    private Pair(F first, L last) {
        this.first = first;
        this.last = last;
    }

    public static <F, L> Pair<F, L> of(F first, L last) {
        return new Pair<>(first, last);
    }

    public F getFirst() {
        return first;
    }

    public Pair<F, L> withFirst(F newFirst) {
        return new Pair<>(newFirst, last);
    }

    public L getLast() {
        return last;
    }

    public Pair<F, L> withLast(L newLast) {
        return new Pair<>(first, newLast);
    }

    public Map<F, L> asMap() {
        return ImmutableMap.of(first, last);
    }

    @Override
    public String toString() {
        return '(' + NullSafe.of(first).toString() + ',' + NullSafe.of(last).toString() + ')';
    }

    @Override
    public final int hashCode() {
        return NullSafe.of(first).hashCode() ^ NullSafe.of(last).hashCode();
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

        return is(first).equalTo(other.first) && is(last).equalTo(other.last);
    }

}
