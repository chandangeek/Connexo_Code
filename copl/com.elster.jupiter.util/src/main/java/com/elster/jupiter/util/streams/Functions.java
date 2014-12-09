package com.elster.jupiter.util.streams;

import java.util.Optional;
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

}
