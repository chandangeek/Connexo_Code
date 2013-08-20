package com.elster.jupiter.util;

/**
 * Wrapper for objects that are potentially null, that provides default behavior for null values, or simply delegates to the actual object if it is not null.
 * A NullSafe wrapping a null value will return 0 as its hash code, and will return true for equals only if the argument is itself a null. It will return "null" as return value for toString().
 * A NullSafe wrapping an actual object will simply delegate these methods to the underlying object.
 */
public abstract class NullSafe {

    private static NullSafe NULL = new Null();

    public static final NullSafe of(Object o) {
        return o == null ? NULL : new Actual(o);
    }

    private static class Null extends NullSafe {

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "null";
        }

        @Override
        public boolean equals(Object obj) {
            return obj == null;
        }
    }

    private static class Actual extends NullSafe {

        private final Object wrapped;

        private Actual(Object wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public boolean equals(Object obj) {
            return wrapped.equals(obj);
        }

        @Override
        public int hashCode() {
            return wrapped.hashCode();
        }

        @Override
        public String toString() {
            return wrapped.toString();
        }
    }

}
