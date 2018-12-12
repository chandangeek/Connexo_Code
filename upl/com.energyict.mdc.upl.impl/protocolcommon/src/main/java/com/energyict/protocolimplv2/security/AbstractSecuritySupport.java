package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpecService;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 27/01/2017 - 10:18
 */
public abstract class AbstractSecuritySupport {

    protected final PropertySpecService propertySpecService;

    public AbstractSecuritySupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }
}