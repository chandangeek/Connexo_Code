package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link PropertySpec} interface for "BigDecimal" values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-31 (14:21)
 */
class BigDecimalPropertySpec extends AbstractPropertySpec<BigDecimal> {

    private Constraint constraint;
    private Optional<BigDecimal> defaultValue = Optional.empty();

    BigDecimalPropertySpec(String name, boolean required) {
        super(name, required);
        this.constraint = new NoConstraint();
    }

    public BigDecimalPropertySpec(String name, boolean required, BigDecimal... possibleValues) {
        super(name, required);
        this.constraint = new PossibleValues(possibleValues);
    }

    public void setDefaultValue(BigDecimal defaultValue) {
        this.defaultValue = Optional.ofNullable(defaultValue);
    }

    @Override
    public List<BigDecimal> getPossibleValues() {
        return Collections.unmodifiableList(this.constraint.getPossibleValues());
    }

    @Override
    public Optional<BigDecimal> getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        if (this.isRequired() && value == null) {
            throw MissingPropertyException.forName(this.getName());
        } else if (value instanceof Integer) {
            return true;
        } else if (value instanceof String) {
            try {
                new BigDecimal((String) value);
                return true;
            } catch (NumberFormatException e) {
                throw InvalidPropertyException.forNameAndValue(this.getName(), value, e);
            }
        } else {
            throw InvalidPropertyException.forNameAndValue(this.getName(), value);
        }
    }

    private interface Constraint {
        void validateValue(BigDecimal value, String propertyName) throws InvalidPropertyException;

        default List<BigDecimal> getPossibleValues() {
            return Collections.emptyList();
        }
    }

    /**
     * Provides an implementation for the {@link Constraint} interface
     * that does not actually impose any constraint at all.
     */
    private static class NoConstraint implements Constraint {
        @Override
        public void validateValue(BigDecimal value, String propertyName) throws InvalidPropertyException {
            // No constraint so no checks and no exception
        }
    }

    private static class PossibleValues implements Constraint {
        private final List<BigDecimal> possibleValues;

        private PossibleValues(BigDecimal... possibleValues) {
            this.possibleValues = Arrays.asList(possibleValues);
        }

        @Override
        public void validateValue(BigDecimal value, String propertyName) throws InvalidPropertyException {
            if (!this.possibleValues.contains(value)) {
                throw new InvalidPropertyException(value + " is not a valid value for property " + propertyName + " because it should be contained in " + this.possibleValues);
            }
        }

        @Override
        public List<BigDecimal> getPossibleValues() {
            return possibleValues;
        }
    }

}