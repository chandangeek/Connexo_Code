/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.impl;

import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BoundedBigDecimalPropertySpec;
import com.elster.jupiter.properties.InvalidValueException;

import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link BoundedBigDecimalPropertySpec} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:22)
 */
class BoundedBigDecimalPropertySpecImpl extends BasicPropertySpec implements BoundedBigDecimalPropertySpec {

    private BigDecimal lowerLimit;
    private BigDecimal upperLimit;

    BoundedBigDecimalPropertySpecImpl(BigDecimal lowerLimit, BigDecimal upperLimit) {
        super(new BigDecimalFactory());
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    @Override
    public BigDecimal getLowerLimit () {
        return lowerLimit;
    }

    @Override
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