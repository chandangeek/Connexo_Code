package com.energyict.protocolcommon;

/**
 * A pair of values, denoted first, and last.
 *
 * @param <F> type of the first value
 * @param <L> type of the last value.
 */
public class Pair<F, L> {

    private static final int PRIME_FOR_HASH = 31;

    private final F first;
    private final L last;

    public Pair(F first, L last) {
        this.first = first;
        this.last = last;
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

    @Override
    public String toString() {
        return '(' + safeToString(first) + ',' + safeToString(last) + ')';
    }

    private String safeToString(Object object) {
        return String.valueOf(object);
    }

    @Override
    public final int hashCode() {
        return PRIME_FOR_HASH * (PRIME_FOR_HASH + Equality.nullSafeHashCode(first)) + Equality.nullSafeHashCode(last);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Pair)) {
            return false;
        }

        Pair other = (Pair) obj;

        return Equality.equalityHoldsFor(first).and(other.first) && Equality.equalityHoldsFor(last).and(other.last);
    }

}