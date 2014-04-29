package com.elster.jupiter.util;

import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Factory for Counter implementations
 *
 */
@NotThreadSafe
public enum Counters {
    ; // empty enum to disallow instantiation

    public static final String NO_DECREMENTS_ALLOWED_MESSAGE = "No decrements allowed.";

    /**
     * @return a Counter that does not allow decrements.
     */
    public static Counter newStrictCounter() {
        return new SimpleCounter() {
            @Override
            public void add(int value) {
                if (value < 0) {
                    throw new IllegalArgumentException(NO_DECREMENTS_ALLOWED_MESSAGE);
                }
                super.add(value);
            }
        };
    }

    /**
     * @return a Counter that does not allow decrements.
     */
    public static Counter newLenientCounter() {
        return new SimpleCounter();
    }

    /**
     * @return a Counter that does allow decrements, yet disallows a negative total.
     */
    public static Counter newLenientNonNegativeCounter() {
        return new SimpleCounter() {
            @Override
            public void add(int value) {
                if (getValue() + value < 0) {
                    throw new IllegalArgumentException("No negative total allowed.");
                }
                super.add(value);
            }
        };
    }

    /**
     * @return a thread safe Counter that does not allow decrements.
     */
    public static Counter newStrictThreadSafeCounter() {
        return new AtomicCounter() {
            @Override
            public void add(int value) {
                if (value < 0) {
                    throw new IllegalArgumentException(NO_DECREMENTS_ALLOWED_MESSAGE);
                }
                super.add(value);
            }
        };
    }

    /**
     * @return a Counter that does not allow decrements.
     */
    public static Counter newLenientThreadSafeCounter() {
        return new AtomicCounter();
    }

    /**
     * Simple implementation that directly manipulates an int total.
     */
    @NotThreadSafe
    private static class SimpleCounter implements Counter {
        private int total;

        private SimpleCounter() {
        }

        @Override
        public void reset() {
            total = 0;
        }

        @Override
        public void increment() {
            total++;
        }

        @Override
        public void add(int value) {
            total += value;
        }

        @Override
        public int getValue() {
            return total;
        }

    }

    /**
    * A very simple, yet thread safe counter that will e.g. allow you to count
    * the occurrence of, say, a type of event.
    *
    * @author Rudi Vankeirsbilck (rudi)
    * @since 2012-07-19 (13:38)
    */
    @ThreadSafe
    private static class AtomicCounter implements Counter {

        private AtomicInteger total = new AtomicInteger();

        @Override
        public void reset() {
            this.total.getAndSet(0);
        }

        @Override
        public void increment() {
            this.total.incrementAndGet();
        }

        @Override
        public void add(int value) {
            this.total.addAndGet(value);
        }

        @Override
        public int getValue() {
            return this.total.get();
        }

    }
}
