package com.elster.jupiter.properties;

import java.math.BigDecimal;

import com.elster.jupiter.properties.PropertySpec;

/**
 * BoundedBigDecimalPropertySpec is a {@link PropertySpec} for
 * BigDecimal values that are bounded between a lower and an upper limit value.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (13:07)
 */
public interface BoundedBigDecimalPropertySpec extends PropertySpec {

    @Override
    public ValueFactory<BigDecimal> getValueFactory();

    public BigDecimal getLowerLimit ();

    public BigDecimal getUpperLimit ();

}