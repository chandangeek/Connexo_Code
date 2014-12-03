package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecBuilder;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dynamicattributes.BigDecimalFactory;
import com.energyict.dynamicattributes.BooleanFactory;
import com.energyict.dynamicattributes.EncryptedStringFactory;
import com.energyict.dynamicattributes.StringFactory;

import java.math.BigDecimal;
import java.util.ArrayList;

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
    CLIENT_MAC_ADDRESS(PropertySpecFactory.boundedDecimalPropertySpecWithDefaultValue(
            SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(),
            BigDecimal.valueOf(1),
            BigDecimal.valueOf(0x7F),
            BigDecimal.valueOf(1),
            getPossibleClientMacAddressValues(1, 0x7F))),

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
            .finish()),
    /**
     * A key used for encryption of bytes
     */
    ANSI_SECURITY_KEY(PropertySpecBuilder
            .forClass(String.class, new EncryptedStringFactory())
            .name(SecurityPropertySpecName.ANSI_SECURITY_KEY.toString())
            .finish()),
    /**
     * The manufacturer key used for encryption of bytes
     */
    MANUFACTURER_ENCRYPTION_KEY(PropertySpecBuilder
            .forClass(String.class, new EncryptedStringFactory())
            .name(SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER.toString())
            .finish()),
    /**
     * The customer key used for encryption of bytes
     */
    CUSTOMER_ENCRYPTION_KEY(PropertySpecBuilder
            .forClass(String.class, new EncryptedStringFactory())
            .name(SecurityPropertySpecName.ENCRYPTION_KEY_CUSTOMER.toString())
            .finish());

    /**
     * Generates a list of possible values for the client mac address property spec
     */
    private static ArrayList<BigDecimal> getPossibleClientMacAddressValues(int lowerLimit, int upperLimit) {
        ArrayList<BigDecimal> result = new ArrayList<>();
        for (int index = lowerLimit; index <= upperLimit; index++) {
            result.add(BigDecimal.valueOf(index));
        }
        return result;
    }

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
