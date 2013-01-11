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

    /**
     * A plain old password, can be a high- or low level password
     */
    PASSWORD(PropertySpecFactory.passwordPropertySpec(SecurityPropertySpecName.PASSWORD.toString())),
    /**
     * A key used for encryption of bytes
     */
    ENCRYPTION_KEY(PropertySpecFactory.passwordPropertySpec(SecurityPropertySpecName.ENCRYPTION_KEY.toString())),
    /**
     * A key used for authentication to a device
     */
    AUTHENTICATION_KEY(PropertySpecFactory.passwordPropertySpec(SecurityPropertySpecName.AUTHENTICATION_KEY.toString())),
    /**
     * A numeric identification of an accessing client
     */
    CLIENT_IDENTIFIER(PropertySpecFactory.bigDecimalPropertySpec(SecurityPropertySpecName.CLIENT_ID.toString())),
    /**
     * A character identification, can be a username or just a level number ...
     */
    DEVICE_ACCESS_IDENTIFIER(PropertySpecFactory.stringPropertySpec(SecurityPropertySpecName.DEVICE_ACCESS_LEVEL_IDENTIFIER.toString()));

    private final PropertySpec propertySpec;

    private DeviceSecurityProperty(PropertySpec propertySpec) {
        this.propertySpec = propertySpec;
    }

    public PropertySpec getPropertySpec() {
        return propertySpec;
    }
}
