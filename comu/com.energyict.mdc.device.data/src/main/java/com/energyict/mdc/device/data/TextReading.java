package com.energyict.mdc.device.data;

import java.math.BigDecimal;

/**
 * Models a {@link Reading} for alphanumerical data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (12:05)
 */
public interface TextReading extends Reading {

    public String getValue();

}