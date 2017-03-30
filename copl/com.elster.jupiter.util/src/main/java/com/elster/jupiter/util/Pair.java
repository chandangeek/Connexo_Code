/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Function;

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

    public <N> Pair<N, L> withFirst(N newFirst) {
        return new Pair<>(newFirst, last);
    }

    public <N> Pair<N, L> withFirst(Function<F, N> function) {
        return new Pair<>(function.apply(first), last);
    }

    public <N> Pair<N, L> withFirst(BiFunction<? super F, ? super L, N> function) {
        return new Pair<>(function.apply(first, last), last);
    }

    public L getLast() {
        return last;
    }

    public <N> Pair<F, N> withLast(N newLast) {
        return new Pair<>(first, newLast);
    }

    public <N> Pair<F, N> withLast(Function<L, N> function) {
        return new Pair<>(first, function.apply(last));
    }

    public <N> Pair<F, N> withLast(BiFunction<? super F, ? super L, N> function) {
        return new Pair<>(first, function.apply(first, last));
    }

    public Map<F, L> asMap() {
        return ImmutableMap.of(first, last);
    }

    public Pair<L, F> flipped() {
        return Pair.of(last, first);
    }

    @Override
    public String toString() {
        return new StringJoiner(",", "(", ")").add(Objects.toString(first)).add(Objects.toString(last)).toString();
    }

    public boolean hasFirst() {
        return first != null;
    }

    public boolean hasLast() {
        return last != null;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(first,last);
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
