package com.elster.jupiter.util.comparators;

import java.util.Comparator;

/**
 * Copyrights EnergyICT
 * Date: 5/08/2014
 * Time: 9:48
 */
public enum NullSafeOrdering {
    NULL_IS_SMALLEST {
        @Override
        public <T extends Comparable<T>> Comparator<T> get() {
            return new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    if (o1 == o2) {
                        return 0;
                    }
                    if (o1 == null) {
                        return -1;
                    }
                    if (o2 == null) {
                        return 1;
                    }
                    return o1.compareTo(o2);
                }
            };
        }
    },
    NULL_IS_GREATEST {
        @Override
        public <T extends Comparable<T>> Comparator<T> get() {
            return new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    if (o1 == o2) {
                        return 0;
                    }
                    if (o1 == null) {
                        return 1;
                    }
                    if (o2 == null) {
                        return -1;
                    }
                    return o1.compareTo(o2);
                }
            };
        }
    };

    public abstract <T extends Comparable<T>> Comparator<T> get();
}
