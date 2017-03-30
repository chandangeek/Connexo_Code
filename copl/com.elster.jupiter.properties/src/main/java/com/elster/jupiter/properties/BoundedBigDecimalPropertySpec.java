/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import java.math.BigDecimal;

/**
 * BoundedBigDecimalPropertySpec is a {@link PropertySpec} for
 * BigDecimal values that are bounded between a lower and an upper limit value.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (13:07)
 */
public interface BoundedBigDecimalPropertySpec extends PropertySpec {

    @Override
    ValueFactory<BigDecimal> getValueFactory();

    BigDecimal getLowerLimit();

    BigDecimal getUpperLimit();

}