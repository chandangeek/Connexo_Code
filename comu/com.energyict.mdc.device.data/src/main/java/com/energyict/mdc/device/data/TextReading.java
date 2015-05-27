package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a {@link Reading} for alphanumerical data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (12:05)
 */
@ProviderType
public interface TextReading extends Reading {

    public String getValue();

}