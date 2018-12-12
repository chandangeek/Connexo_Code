/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.streams;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public enum Functions {
    ;

    /**
     * This Function converts an Optional to a Stream. This is especially handy to convert a Stream of Optionals of T to a Stream of T.
     * Chances are that Optional will be getting a new method toStream() or will implement Stream in a future version of java, which will make this function obsolete.
     *
     * @param <T>
     * @return a function that converts an Optional to a Stream
     */
    public static <T> Function<Optional<? extends T>, Stream<T>> asStream() {
        return op -> op.map(Stream::of).orElseGet(Stream::empty);
    }

    /**
     * This method exists solely to cleanly decorate a method reference as a Consumer so the andThen() can be called on it :
     * <p>
     * e.g. : first(System.out::println).andThen(System.err::println)
     */
    public static <T> Consumer<T> first(Consumer<T> consumer) {
        return consumer;
    }

    /**
     * This method exists solely to cleanly decorate a method reference as a Function so the andThen() can be called on it :
     * <p>
     * e.g. : map(HasName::getName).andThen(String::toLowerCase)
     */
    public static <X, Y> Function<X, Y> map(Function<X, Y> function) {
        return function;
    }
}
