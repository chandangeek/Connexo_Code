package com.energyict.protocolcommon;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Provides commonly used equality tests on various types of objects.
 * All methods are static so there is no need to create instances of this class.
 * The class' equalityHoldsFor() method is intended to be a static import, allowing you to form fluent equality expressions starting with that method.
 * These fluent expressions read like  <code>if (equalityHoldsFor(a).and(b)) {...}}</code>.
 *
 * This class is to be used in preference to a.equals(b) in the following cases :
 *
 * <ul>
 *     <li>if you want null safe equality checks (where null == null is also expected to yield true)</li>
 *     <li>if you want to compare the values of two BigDecimals, without taking their scale into account.</li>
 *     <li>if you want to compare the values of two Dates, one of which may be a java.sqlTimestamp, without taking Timestamp's nanoseconds into account./li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @author Tom De Greyt (tgr)
 * @since March 27, 2012 (11:35:36)
 */
public enum Equality {

    ; // implemented as an empty enum to prevent instantiation.

    /**
     * Starting method for the fluent equality expression.
     * @param first the first object to be compared for equality, or null. Note that if underlying logic decides to call the equals() method, it will be on this argument rather than the one passed to and().
     */
    public static EqualityBuilder equalityHoldsFor(Object first) {
        if (first == null) {
            return NullEqualityBuilder.INSTANCE;
        }
        if (first instanceof BigDecimal) {
            return new BigDecimalEqualityBuilder((BigDecimal) first);
        }
        if (first instanceof Date) {
            return new DateEqualityBuilder((Date) first);
        }
        return new DefaultEqualityBuilder(first);
    }

    /**
     * Construct to accept the second argument of the fluent expression, and which will deliver the result.
     */
    public static interface EqualityBuilder {

        boolean and(Object second);
    }

    private static enum NullEqualityBuilder implements EqualityBuilder {
        INSTANCE;

        @Override
        public boolean and(Object second) {
            return second == null;
        }
    }

    private static class BigDecimalEqualityBuilder implements EqualityBuilder {

        private final BigDecimal first;

        private BigDecimalEqualityBuilder(BigDecimal first) {
            this.first = first;
        }

        @Override
        public boolean and(Object second) {
            return second instanceof BigDecimal && first.compareTo((BigDecimal) second) == 0;
        }
    }

    private static class DefaultEqualityBuilder implements EqualityBuilder {

        private final Object first;

        private DefaultEqualityBuilder(Object first) {
            assert first != null;
            this.first = first;
        }

        @Override
        public boolean and(Object second) {
            return first == second || second != null && first.equals(second);
        }
    }

    private static class DateEqualityBuilder implements EqualityBuilder {

        private final long millis;

        private DateEqualityBuilder(Date date) {
            assert date != null;
            millis = date.getTime();
        }

        @Override
        public boolean and(Object second) {
            return second instanceof Date && ((Date) second).getTime() == millis;
        }
    }

    public static int nullSafeHashCode(Object object) {
        return object == null ? 0 : object.hashCode();
    }

}