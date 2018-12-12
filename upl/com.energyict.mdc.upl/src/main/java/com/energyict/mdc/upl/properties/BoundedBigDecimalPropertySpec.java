package com.energyict.mdc.upl.properties;

import java.math.BigDecimal;

/**
 * BoundedBigDecimalPropertySpec is a {@link PropertySpec} for
 * BigDecimal values that are bounded between a lower and an upper limit value.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-21 (09:38)
 */
public interface BoundedBigDecimalPropertySpec extends PropertySpec {

    BigDecimal getLowerLimit();

    BigDecimal getUpperLimit();

}