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
     * A DLMS clientMacAddress
     */
    CLIENT_MAC_ADDRESS(PropertySpecFactory.bigDecimalPropertySpec(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString())),
    /**
     * A character identification of the accessing client
     */
    DEVICE_ACCESS_IDENTIFIER(PropertySpecFactory.stringPropertySpec(SecurityPropertySpecName.DEVICE_ACCESS_IDENTIFIER.toString())),
    /**
     * A level definition, indication of which access should be granted to the ComServer
     */
    DEVICE_ACCESS_LEVEL(PropertySpecFactory.bigDecimalPropertySpec(SecurityPropertySpecName.DEVICE_ACCESS_LEVEL.toString())),
    /**
     * A username for ANSI C12 protocols
     */
    ANSI_C12_USER(PropertySpecFactory.stringPropertySpec(SecurityPropertySpecName.ANSI_C12_USER.toString())),
    /**
     * A UserId for ANSI C12 protocols
     */
    ANSI_C12_USER_ID(PropertySpecFactory.bigDecimalPropertySpec(SecurityPropertySpecName.ANSI_C12_USER_ID.toString())),
    /**
     * Indication for ansi protocols to use a binary password
     */
    BINARY_PASSWORD(PropertySpecFactory.booleanPropertySpec(SecurityPropertySpecName.BINARY_PASSWORD.toString())),
    /**
     * ANSI ap title
     */
    CALLED_AP_TITLE(PropertySpecFactory.stringPropertySpec(SecurityPropertySpecName.CALLED_AP_TITLE.toString()));

    private final PropertySpec propertySpec;

    private DeviceSecurityProperty(PropertySpec propertySpec) {
        this.propertySpec = propertySpec;
    }

    /**
     * @return the PropertySpec for this Enum value
     */
    public PropertySpec getPropertySpec() {
        return propertySpec;
    }
}
