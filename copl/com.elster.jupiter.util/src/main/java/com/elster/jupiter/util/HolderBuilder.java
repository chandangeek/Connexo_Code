package com.elster.jupiter.util;

public class HolderBuilder<T> {


    public static <M> AndThenBuilder<M> first(M first) {
        return new AndThenBuilder<>(first);
    }

    public static <M> Holder<M> holding(M element) {
        return new SimpleHolder<M>(element);
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
}
