package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.google.common.collect.Range;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an implementation for the {@link PropertySpec} interface for "long" values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-04 (13:26)
 */
class LongPropertySpec extends AbstractPropertySpec<Long> {

    private final RangeConstraint rangeConstraint;

    LongPropertySpec(String name, boolean required) {
        super(name, required);
        this.rangeConstraint = new NoConstraint();
    }

    LongPropertySpec(String name, boolean required, Range<Long> validRange) {
        super(name, required);
        this.rangeConstraint = new InRange(validRange);
    }

    LongPropertySpec(String name, boolean required, Long... validValues) {
        super(name, required);
        this.rangeConstraint = new InSet(validValues);
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        if (this.isRequired() && value == null) {
            throw MissingPropertyException.forName(this.getName());
        } else if (value instanceof Long) {
            return true;
        } else if (value instanceof String) {
            try {
                long longValue = Long.parseLong((String) value);
                this.rangeConstraint.validateValue(longValue, this.getName());
                return true;
            } catch (NumberFormatException e) {
                throw InvalidPropertyException.forNameAndValue(this.getName(), value, e);
            }
        } else {
            throw InvalidPropertyException.forNameAndValue(this.getName(), value);
        }
    }

    private interface RangeConstraint {
        void validateValue(long value, String propertyName) throws InvalidPropertyException;
    }

    /**
     * Provides an implementation for the {@link RangeConstraint} interface
     * that does not actually impose any constraint at all.
     */
    private static class NoConstraint implements RangeConstraint {
        @Override
        public void validateValue(long value, String propertyName) throws InvalidPropertyException {
            // No constraint so no checks and no exception
        }
    }

    /**
     * Provides an implementation for the {@link RangeConstraint} interface
     * that checks that the value is contained in a Range.
     */
    private static class InRange implements RangeConstraint {
        private final Range<Long> range;

        private InRange(Range<Long> range) {
            this.range = range;
        }

        @Override
        public void validateValue(long value, String propertyName) throws InvalidPropertyException {
            if (!this.range.contains(value)) {
                throw  new InvalidPropertyException(value + " is not a valid value for property " + propertyName + " because it should be contained in " + this.range);
            }
        }
    }

    /**
     * Provides an implementation for the {@link RangeConstraint} interface
     * that checks that the value is one of the predefined values.
     */
    private static class InSet implements RangeConstraint {
        private final Set<Long> range;

        private InSet(Long... range) {
            this(new HashSet<Long>(Arrays.asList(range)));
        }

        private InSet(Set<Long> range) {
            this.range = range;
        }

        @Override
        public void validateValue(long value, String propertyName) throws InvalidPropertyException {
            if (!this.range.contains(value)) {
                throw  new InvalidPropertyException(value + " is not a valid value for property " + propertyName + " because it should be contained in " + Arrays.toString(this.range.toArray(new Long[this.range.size()])));
            }
        }
    }

}