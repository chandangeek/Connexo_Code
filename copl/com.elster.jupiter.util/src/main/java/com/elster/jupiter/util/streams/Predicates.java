package com.elster.jupiter.util.streams;

import java.util.function.Predicate;

/**
 * Created by tgr on 10/09/2014.
 */
public enum Predicates {
    ;

    public static <T> Predicate<T> notNull() {
        return r -> r != null;
    }

    public static <T> Predicate<T> isNull() {
        return r -> r == null;
    }

    public static <T> Predicate<T> not(Predicate<T> predicate) {
        return e -> !predicate.test(e);
    }

}
