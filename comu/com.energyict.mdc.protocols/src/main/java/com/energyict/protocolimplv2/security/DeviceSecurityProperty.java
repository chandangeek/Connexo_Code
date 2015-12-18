package com.energyict.protocolimplv2.security;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.protocols.naming.SecurityPropertySpecName;

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
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService
                    .passwordSpec()
                    .named(SecurityPropertySpecName.PASSWORD)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish();
        }

        @Override
        public String javaName() {
            return SecurityPropertySpecName.PASSWORD.getKey();
        }
    },
    /**
     * A key used for encryption of bytes
     */
    ENCRYPTION_KEY {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService
                    .encryptedStringSpec()
                    .named(SecurityPropertySpecName.ENCRYPTION_KEY)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish();
        }

        @Override
        public String javaName() {
            return SecurityPropertySpecName.ENCRYPTION_KEY.getKey();
        }
    },
    /**
     * A key used for authentication to a device
     */
    AUTHENTICATION_KEY {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService
                    .encryptedStringSpec()
                    .named(SecurityPropertySpecName.AUTHENTICATION_KEY)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish();
        }

        @Override
        public String javaName() {
            return SecurityPropertySpecName.AUTHENTICATION_KEY.getKey();
        }
    },
    /**
     * A DLMS clientMacAddress
     */
    CLIENT_MAC_ADDRESS {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService
                    .bigDecimalSpec()
                    .named(SecurityPropertySpecName.CLIENT_MAC_ADDRESS)
                    .fromThesaurus(thesaurus)
                    .markRequired().
                    finish();
        }

        @Override
        public String javaName() {
            return SecurityPropertySpecName.CLIENT_MAC_ADDRESS.getKey();
        }
    },
    /**
     * A character identification of the accessing client
     */
    DEVICE_ACCESS_IDENTIFIER {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService
                    .stringSpec()
                    .named(SecurityPropertySpecName.DEVICE_ACCESS_IDENTIFIER)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish();
        }

        @Override
        public String javaName() {
            return SecurityPropertySpecName.DEVICE_ACCESS_IDENTIFIER.getKey();
        }
    },
    /**
     * A username for ANSI C12 protocols
     */
    ANSI_C12_USER {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService
                    .stringSpec()
                    .named(SecurityPropertySpecName.ANSI_C12_USER)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish();
        }

        @Override
        public String javaName() {
            return SecurityPropertySpecName.ANSI_C12_USER.getKey();
        }
    },
    /**
     * A UserId for ANSI C12 protocols
     */
    ANSI_C12_USER_ID {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService
                    .stringSpec()
                    .named(SecurityPropertySpecName.ANSI_C12_USER_ID)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish();
        }

        @Override
        public String javaName() {
            return SecurityPropertySpecName.ANSI_C12_USER_ID.getKey();
        }
    },
    /**
     * Indication for ansi protocols to use a binary password
     */
    BINARY_PASSWORD {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService
                    .booleanSpec()
                    .named(SecurityPropertySpecName.BINARY_PASSWORD)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish();
        }

        @Override
        public String javaName() {
            return SecurityPropertySpecName.BINARY_PASSWORD.getKey();
        }
    },
    /**
     * ANSI ap title
     */
    ANSI_CALLED_AP_TITLE {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService
                    .stringSpec()
                    .named(SecurityPropertySpecName.ANSI_CALLED_AP_TITLE)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish();
        }

        @Override
        public String javaName() {
            return SecurityPropertySpecName.ANSI_CALLED_AP_TITLE.getKey();
        }
    },

    /**
     * The manufacturer key used for encryption of bytes
     */
    MANUFACTURER_ENCRYPTION_KEY {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService
                    .encryptedStringSpec()
                    .named(SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER)
                    .fromThesaurus(thesaurus)
                    .finish();
        }

        @Override
        public String javaName() {
            return SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER.getKey();
        }
    },

    /**
     * The customer key used for encryption of bytes
     */
    CUSTOMER_ENCRYPTION_KEY {
        @Override
        protected PropertySpec doGetPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService
                    .encryptedStringSpec()
                    .named(SecurityPropertySpecName.ENCRYPTION_KEY_CUSTOMER)
                    .fromThesaurus(thesaurus)
                    .finish();
        }

        @Override
        public String javaName() {
            return SecurityPropertySpecName.ENCRYPTION_KEY_CUSTOMER.getKey();
        }
    }
    ;

    private PropertySpec cachedPropertySpec;

    public PropertySpec getPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        if (this.cachedPropertySpec == null) {
            this.cachedPropertySpec = this.doGetPropertySpec(propertySpecService, thesaurus);
        }
        return this.cachedPropertySpec;
    }

    /**
     * @return the PropertySpec for this Enum value
     */
    protected abstract PropertySpec doGetPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus);

    public abstract String javaName();

    public String databaseName() {
        return javaName().toUpperCase();
    }

}
