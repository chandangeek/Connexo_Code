/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.collections;

import java.util.List;

public class BinarySearch<K extends Comparable<? super K>, T> {

    private final List<T> list;
    private final Key<K, T> keyOf;

    public BinarySearch(List<T> list, Key<K, T> keyOf) {
        this.list = list;
        this.keyOf = keyOf;
    }

    public static <S extends Comparable<? super S>> BinarySearch<S, S> usingValueAsKey(List<S> values) {
        return new BinarySearch<>(values, new Itself<S>());
    }

    public static <S> Builder<S> in(List<S> validationQualities) {
        return new Builder<>(validationQualities);
    }

    public int lastOccurrence(K key) {
        if (list.isEmpty()) {
            return -1;
        }
        int lower = 0;
        int upper = list.size() - 1;
        boolean found = false;
        while (upper > lower) {
            int imid = midpoint2(lower, upper);
            int compare = keyOf.getKey(list.get(imid)).compareTo(key);
            if (compare == 0) {
                lower = imid;
                found = true;
            } else if (compare < 0) {
                lower = imid + 1;
            } else {
                upper = imid - 1;
            }
        }
        found = found || keyOf.getKey(list.get(upper)).compareTo(key) == 0;
        while (!found && upper < list.size() && keyOf.getKey(list.get(upper)).compareTo(key) < 0) {
            upper++;
        }
        return found ? upper : -upper - 1;
    }

    public List<T> getList() {
        return list;
    }

    public static final class Builder<X> {

        private final List<X> list;

        private Builder(List<X> list) {
            this.list = list;
        }

        public <Y extends Comparable<? super Y>> BinarySearch<Y, X> using(Key<Y, X> key) {
            return new BinarySearch<>(list, key);
        }
    }

    public interface Key<K, T> {

        K getKey(T t);
    }

    public static class Itself<T> implements Key<T, T> {

        @Override
        public T getKey(T t) {
            return t;
        }
    }

    public int firstOccurrence(K key) {
        if (list.isEmpty()) {
            return -1;
        }
        int lower = 0;
        int upper = list.size() - 1;
        boolean found = false;
        while (upper > lower) {
            int imid = midpoint(lower, upper);
            int compare = keyOf.getKey(list.get(imid)).compareTo(key);
            if (compare == 0) {
                upper = imid;
                found = true;
            } else if (compare < 0) {
                lower = imid + 1;
            } else {
                upper = imid - 1;
            }
        }
        found = found || keyOf.getKey(list.get(lower)).compareTo(key) == 0;
        while (!found && lower < list.size() && keyOf.getKey(list.get(lower)).compareTo(key) < 0) {
            lower++;
        }
        return found ? lower : -lower - 1;
    }

    private int midpoint(int imin, int imax) {
        return (imin + imax) / 2;
    }

    private int midpoint2(int imin, int imax) {
        return (imin + imax + 1) / 2;
    }

}
