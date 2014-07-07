package com.energyict.protocolimplv2.security;

import com.energyict.mdc.dynamic.EncryptedStringFactory;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;

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
    PASSWORD {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService) {
            return propertySpecService.
                    newPropertySpecBuilder(new EncryptedStringFactory()).
                    name(SecurityPropertySpecName.PASSWORD.name()).
                    finish();
        }
    },
    /**
     * A key used for encryption of bytes
     */
    ENCRYPTION_KEY {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService) {
            return propertySpecService.
                    newPropertySpecBuilder(new EncryptedStringFactory()).
                    name(SecurityPropertySpecName.ENCRYPTION_KEY.name()).
                    finish();
        }
    },
    /**
     * A key used for authentication to a device
     */
    AUTHENTICATION_KEY {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService) {
            return propertySpecService.
                    newPropertySpecBuilder(new EncryptedStringFactory()).
                    name(SecurityPropertySpecName.AUTHENTICATION_KEY.name()).
                    finish();
        }
    },
    /**
     * A DLMS clientMacAddress
     */
    CLIENT_MAC_ADDRESS {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService) {
            return propertySpecService.
                    newPropertySpecBuilder(new BigDecimalFactory()).
                    name(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.name()).
                    finish();
        }
    },
    /**
     * A character identification of the accessing client
     */
    DEVICE_ACCESS_IDENTIFIER {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService) {
            return propertySpecService.
                    newPropertySpecBuilder(new StringFactory()).
                    name(SecurityPropertySpecName.DEVICE_ACCESS_IDENTIFIER.name()).
                    finish();
        }
    },
    /**
     * A username for ANSI C12 protocols
     */
    ANSI_C12_USER {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService) {
            return propertySpecService.
                    newPropertySpecBuilder(new StringFactory()).
                    name(SecurityPropertySpecName.ANSI_C12_USER.name()).
                    finish();
        }
    },
    /**
     * A UserId for ANSI C12 protocols
     */
    ANSI_C12_USER_ID {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService) {
            return propertySpecService.
                    newPropertySpecBuilder(new StringFactory()).
                    name(SecurityPropertySpecName.ANSI_C12_USER_ID.name()).
                    finish();
        }
    },
    /**
     * Indication for ansi protocols to use a binary password
     */
    BINARY_PASSWORD {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService) {
            return propertySpecService.
                    newPropertySpecBuilder(new BooleanFactory()).
                    name(SecurityPropertySpecName.BINARY_PASSWORD.name()).
                    finish();
        }
    },
    /**
     * ANSI ap title
     */
    ANSI_CALLED_AP_TITLE {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService) {
            return propertySpecService.
                    newPropertySpecBuilder(new StringFactory()).
                    name(SecurityPropertySpecName.ANSI_CALLED_AP_TITLE.name()).
                    finish();
        }
    };

    private PropertySpec cachedPropertySpec;

    public PropertySpec getPropertySpec (PropertySpecService propertySpecService) {
        if (this.cachedPropertySpec == null) {
            this.cachedPropertySpec = this.doGetPropertySpec(propertySpecService);
        }
        return this.cachedPropertySpec;
    }

    /**
     * @return the PropertySpec for this Enum value
     */
    protected abstract PropertySpec doGetPropertySpec(PropertySpecService propertySpecService);

}
