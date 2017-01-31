/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import java.util.function.Supplier;

public enum HolderBuilder {
    ;

    public static <M> AndThenBuilder<M> first(M first) {
        return new AndThenBuilder<>(first);
    }

    public static <M> Holder<M> holding(M element) {
        return new SimpleHolder<M>(element);
    }

    /**
     * @param actualInitialization a Supplier that will execute the actual initialization.
     * @param <M> the type of the held object
     * @return a Holder that will initialize the held instance only upon first request. Even if the Holder is shared across threads, initialization is guaranteed to occur only once.
     */
    public static <M> Holder<M> lazyInitialize(Supplier<M> actualInitialization) {
        return new LazyInitializer<>(actualInitialization);
    }

    public static class AndThenBuilder<M> {
        private final M first;

        public AndThenBuilder(M first) {
            this.first = first;
        }

        public Holder<M> andThen(M after) {
            return new FirstAndThen<M>(first, after);
        }
    }

    private static final class FirstAndThen<R> implements Holder<R> {

        private final R first;
        private final R after;
        private boolean firstReturned;

        private FirstAndThen(R first, R after) {
            this.after = after;
            this.first = first;
        }

        @Override
        public R get() {
            try {
                return firstReturned ? after : first;
            } finally {
                firstReturned = true;
            }
        }

    }

    private static class SimpleHolder<R> implements Holder<R> {

        private SimpleHolder(R held) {
            this.held = held;
        }

        private final R held;

        @Override
        public R get() {
            return held;
        }
    }

    private static class LazyInitializer<T> implements Holder<T> {
        private final Supplier<T> heavySupplier;
        private final Object initLock = new Object();

        private Supplier<T> heavy = this::createAndCacheHeavy;

        public LazyInitializer(Supplier<T> heavySupplier) {
            this.heavySupplier = heavySupplier;
        }

        public T get() {
            return heavy.get();
        }

        private T createAndCacheHeavy() {
            synchronized (initLock) {
                class HeavyFactory implements Supplier<T> {
                    private final T heavyInstance = heavySupplier.get();

                    public T get() {
                        return heavyInstance;
                    }
                }

                if (!HeavyFactory.class.isInstance(heavy)) {
                    heavy = new HeavyFactory();
                }

                return heavy.get();
            }
        }
    }
}
