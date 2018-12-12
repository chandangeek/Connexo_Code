package com.energyict.mdc.common.services;

import com.energyict.obis.ObisCode;

/**
 * Provides human readable descriptions for {@link ObisCode}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-27 (14:59)
 */
public interface ObisCodeDescriptor {
    String describe(ObisCode obisCode);
}