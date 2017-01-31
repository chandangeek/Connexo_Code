/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.streams;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by tgr on 10/09/2014.
 */
public enum Predicates {
    ;

    public static <T> Predicate<T> not(Predicate<T> predicate) {
        return predicate.negate();
    }

    public static <T> Predicate<T> either(Predicate<T> predicate) {
        return predicate;
    }

    public static Predicate<Boolean> self() {
        return b -> b != null && b;
    }

    public static <R, T> PredicateBuilder<R, T> on(Function<R, T> getter) {
        return new PredicateBuilder<>(getter);
    }

    public static class PredicateBuilder<R, T> {
        private final Function<R, T> getter;

        private PredicateBuilder(Function<R, T> getter) {
            this.getter = getter;
        }

        public Predicate<R> test(Predicate<T> propertyTest) {
            return r -> propertyTest.test(getter.apply(r));
        }

        public Predicate<R> map(Function<T, Boolean> propertyTest) {
            return r -> propertyTest.apply(getter.apply(r));
        }
    }
}
