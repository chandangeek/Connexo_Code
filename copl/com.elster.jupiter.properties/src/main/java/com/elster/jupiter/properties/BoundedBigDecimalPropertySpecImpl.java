package com.elster.jupiter.properties;

import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link BoundedBigDecimalPropertySpec} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:22)
 */
public class BoundedBigDecimalPropertySpecImpl extends BasicPropertySpec implements BoundedBigDecimalPropertySpec {

    private BigDecimal lowerLimit;
    private BigDecimal upperLimit;

    /**
     * a PropertySpec for properties of type BigDecimal having values between the lower and upper limit (included)
     *
     * @param name for the property
     * @param lowerLimit smallest value allowed
     * @param upperLimit greates value allowed
     */
    public BoundedBigDecimalPropertySpecImpl(String name, BigDecimal lowerLimit, BigDecimal upperLimit) {
        super(name, new BigDecimalFactory());
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    public BigDecimal getLowerLimit () {
        return lowerLimit;
    }

    public BigDecimal getUpperLimit () {
        return upperLimit;
    }

    @Override
    public boolean validateValue(Object objectValue) throws InvalidValueException {
        if (objectValue instanceof BigDecimal) {
            BigDecimal value = (BigDecimal) objectValue;
            boolean valid = super.validateValue(value);
            if (lowerLimit != null && value.compareTo(lowerLimit)<0) {
                throw new InvalidValueException("XisTooLow", "The objectValue is too small", this.getName(), lowerLimit, upperLimit);
            }
            if (upperLimit != null && value.compareTo(upperLimit)>0) {
                throw new InvalidValueException("XisTooHigh", "The objectValue is too high", this.getName(), lowerLimit, upperLimit);
            }
            return valid;
        }
        else {
            return false;
        }
    }

}