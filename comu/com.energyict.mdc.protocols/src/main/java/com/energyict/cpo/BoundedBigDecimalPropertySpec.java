package com.energyict.cpo;

import com.energyict.dynamicattributes.BigDecimalFactory;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 30/08/13
 * Time: 11:31
 */
public class BoundedBigDecimalPropertySpec extends BasicPropertySpec<BigDecimal> {

    private BigDecimal lowerLimit;
    private BigDecimal upperLimit;

    /**
     * a PropertySpec for properties of type BigDecimal having values between the lower and upper limit (included)
     * @param name  for the property
     * @param lowerLimit smallest value allowed
     * @param upperLimit greates value allowed
     */
    public BoundedBigDecimalPropertySpec(String name, BigDecimal lowerLimit, BigDecimal upperLimit){
        super(name, new BigDecimalFactory(),new ValueDomain(BigDecimal.class));
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    public BigDecimal getLowerLimit() {
        return lowerLimit;
    }

    public BigDecimal getUpperLimit() {
        return upperLimit;
    }

}