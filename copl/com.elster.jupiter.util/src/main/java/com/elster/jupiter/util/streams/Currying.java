/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.streams;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public enum Currying {
    ;

    public static class FunctionBuilder<A, B, R> {

        private final BiFunction<A, B, R> biFunction;

        private FunctionBuilder(BiFunction<A, B, R> biFunction) {
            this.biFunction = biFunction;
        }

        public Function<B, R> on(A a) {
            return b -> biFunction.apply(a, b);
        }

        public Function<A, R> with(B b) {
            return a -> biFunction.apply(a, b);
        }
    }

    public static <A, B, R> FunctionBuilder<A, B, R> use(BiFunction<A, B, R> biFunction) {
        return new FunctionBuilder<>(biFunction);
    }

    public static <A, B> PredicateBuilder<A, B> test(BiFunction<A, B, Boolean> biFunction) {
        return new PredicateBuilder<>(biFunction);
    }

    public static class PredicateBuilder<A, B> {

        private final BiFunction<A, B, Boolean> biFunction;

        public PredicateBuilder(BiFunction<A, B, Boolean> biFunction) {
            this.biFunction = biFunction;
        }

        public Predicate<B> on(A a) {
            return b -> biFunction.apply(a, b);
        }

        public Predicate<A> with(B b) {
            return a -> biFunction.apply(a, b);
        }

    }

    public static class ConsumerBuilder<A, B> {

        private final BiConsumer<A, B> biConsumer;

        private ConsumerBuilder(BiConsumer<A, B> biConsumer) {
            this.biConsumer = biConsumer;
        }

        public Consumer<B> on(A a) {
            return b -> biConsumer.accept(a, b);
        }

        public Consumer<A> with(B b) {
            return a -> biConsumer.accept(a, b);
        }
    }

    public static <A, B> ConsumerBuilder<A, B> perform(BiConsumer<A, B> biConsumer) {
        return new ConsumerBuilder<>(biConsumer);
    }

    public static class RunnableBuilder<A> {

        private final Consumer<A> consumer;

        private RunnableBuilder(Consumer<A> consumer) {
            this.consumer = consumer;
        }

        public Runnable on(A a) {
            return () -> consumer.accept(a);
        }

    }

    public static <A> RunnableBuilder<A> perform(Consumer<A> consumer) {
        return new RunnableBuilder<>(consumer);
    }

}