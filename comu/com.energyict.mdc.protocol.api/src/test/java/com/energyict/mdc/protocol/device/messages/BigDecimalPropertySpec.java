package com.energyict.mdc.protocol.device.messages;

import com.energyict.mdc.dynamic.PropertySpec;

import java.math.BigDecimal;

/**
 * Provides an implementation for the  {@link PropertySpec} interface
 * for BigDecimal values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-05 (14:58)
 */
public class BigDecimalPropertySpec extends SimplePropertySpec<BigDecimal> {

    public BigDecimalPropertySpec (String name) {
        super(name);
    }

    public BigDecimalPropertySpec (String name, boolean required) {
        super(name, required);
    }

}