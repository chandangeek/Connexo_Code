package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.dynamic.BigDecimalFactory;

import java.math.BigDecimal;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:22)
 */
public class BoundedBigDecimalPropertySpec extends BasicPropertySpec<BigDecimal> {

    private BigDecimal lowerLimit;
    private BigDecimal upperLimit;

    /**
     * a PropertySpec for properties of type BigDecimal having values between the lower and upper limit (included)
     *
     * @param name for the property
     * @param lowerLimit smallest value allowed
     * @param upperLimit greates value allowed
     */
    public BoundedBigDecimalPropertySpec (String name, BigDecimal lowerLimit, BigDecimal upperLimit) {
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

}