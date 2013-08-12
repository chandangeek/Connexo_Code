package com.elster.jupiter.util;

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
