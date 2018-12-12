package com.energyict.mdc.upl.properties;

import aQute.bnd.annotation.ConsumerType;

/**
 * Provides services to convert between different types of properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-02 (12:33)
 */
@ConsumerType
public interface Converter {
    HexString hexFromString(String value);
}