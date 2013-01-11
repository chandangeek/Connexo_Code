package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;

/**
 * Summarizes all used DeviceSecurityProperty
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/01/13
 * Time: 16:40
 */
public enum DeviceSecurityProperty {

    PASSWORD(PropertySpecFactory.passwordPropertySpec(SecurityPropertySpecName.PASSWORD.toString())),
    ENCRYPTION_KEY(PropertySpecFactory.passwordPropertySpec(SecurityPropertySpecName.ENCRYPTION_KEY.toString())),
    AUTHENTICATION_KEY(PropertySpecFactory.passwordPropertySpec(SecurityPropertySpecName.AUTHENTICATION_KEY.toString())),
    CLIENT_IDENTIFIER(PropertySpecFactory.bigDecimalPropertySpec(SecurityPropertySpecName.CLIENT_ID.toString()));

    private final PropertySpec propertySpec;

    private DeviceSecurityProperty(PropertySpec propertySpec) {
        this.propertySpec = propertySpec;
    }

    public PropertySpec getPropertySpec() {
        return propertySpec;
    }
}
