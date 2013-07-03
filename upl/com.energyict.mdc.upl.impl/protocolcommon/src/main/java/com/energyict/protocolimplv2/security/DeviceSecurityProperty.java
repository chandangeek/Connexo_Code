package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecBuilder;
import com.energyict.dynamicattributes.*;

import java.math.BigDecimal;

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
    PASSWORD(PropertySpecBuilder
            .forClass(String.class, new EncryptedStringFactory()).
                    name(SecurityPropertySpecName.PASSWORD.toString()).
                    finish()),
    /**
     * A key used for encryption of bytes
     */
    ENCRYPTION_KEY(PropertySpecBuilder
            .forClass(String.class, new EncryptedStringFactory())
            .name(SecurityPropertySpecName.ENCRYPTION_KEY.toString())
        .finish()),
    /**
     * A key used for authentication to a device
     */
    AUTHENTICATION_KEY(PropertySpecBuilder
            .forClass(String.class, new EncryptedStringFactory())
            .name(SecurityPropertySpecName.AUTHENTICATION_KEY.toString())
        .finish()),
    /**
     * A DLMS clientMacAddress
     */
    CLIENT_MAC_ADDRESS(PropertySpecBuilder.
            forClass(BigDecimal.class, new BigDecimalFactory()).
            name(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString()).
            setDefaultValue(new BigDecimal(1)).
            finish()),
    /**
     * A character identification of the accessing client
     */
    DEVICE_ACCESS_IDENTIFIER(PropertySpecBuilder
            .forClass(String.class, new StringFactory())
            .name(SecurityPropertySpecName.DEVICE_ACCESS_IDENTIFIER.toString())
        .finish()),
    /**
     * A username for ANSI C12 protocols
     */
    ANSI_C12_USER(PropertySpecBuilder
                .forClass(String.class, new StringFactory())
                .name(SecurityPropertySpecName.ANSI_C12_USER.toString())
        .finish()),
    /**
     * A UserId for ANSI C12 protocols
     */
    ANSI_C12_USER_ID(PropertySpecBuilder
                    .forClass(BigDecimal.class, new BigDecimalFactory())
                    .name(SecurityPropertySpecName.ANSI_C12_USER_ID.toString())
                    .setDefaultValue(new BigDecimal(1))
            .finish()),
    /**
     * Indication for ansi protocols to use a binary password
     */
    BINARY_PASSWORD(PropertySpecBuilder
            .forClass(Boolean.class, new BooleanFactory())
            .name(SecurityPropertySpecName.BINARY_PASSWORD.toString())
            .setDefaultValue(Boolean.FALSE)
        .finish()),
    /**
     * ANSI ap title
     */
    ANSI_CALLED_AP_TITLE(PropertySpecBuilder
            .forClass(String.class, new StringFactory())
            .name(SecurityPropertySpecName.ANSI_CALLED_AP_TITLE.toString())
        .finish());

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
