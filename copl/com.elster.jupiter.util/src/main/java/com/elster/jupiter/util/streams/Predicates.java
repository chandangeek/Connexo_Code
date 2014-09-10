package com.elster.jupiter.util.streams;

/**
 * Created by tgr on 10/09/2014.
 */
public enum Predicates {
    ;

    public static <T> java.util.function.Predicate<T> notNull() {
        return r -> r != null;
    }

}
