package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.KeyAccessorType;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import com.energyict.protocolimpl.properties.DescriptionTranslationKey;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Summarizes all used DeviceSecurityProperty
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/01/13
 * Time: 16:40
 */
public enum DeviceSecurityProperty {

    /**
     * A DLMS clientMacAddress.
     */
    CLIENT_MAC_ADDRESS {
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService) {
            return this.getPropertySpec(propertySpecService, BigDecimal.ONE);
        }

        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService, Object defaultValue) {
            return this.getPropertySpec(propertySpecService, (BigDecimal) defaultValue);
        }

        private PropertySpec getPropertySpec(PropertySpecService propertySpecService, BigDecimal defaultValue) {
            return propertySpecService
                    .boundedBigDecimalSpec(BigDecimal.ONE, BigDecimal.valueOf(0x7F))
                    .named(SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.getKey(), SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS)
                    .describedAs(new DescriptionTranslationKey(SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS))
                    .setDefaultValue(defaultValue)
                    .addValues(getPossibleClientMacAddressValues(1, 0x7F))
                    .markExhaustive()
                    .markRequired()
                    .finish();
        }
    },
    /**
     * A character identification of the accessing client.
     */
    DEVICE_ACCESS_IDENTIFIER {
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService) {
            return UPLPropertySpecFactory
                    .specBuilder(SecurityPropertySpecTranslationKeys.DEVICE_ACCESS_IDENTIFIER.getKey(), false, SecurityPropertySpecTranslationKeys.DEVICE_ACCESS_IDENTIFIER, propertySpecService::stringSpec)
                    .markRequired()
                    .finish();
        }
    },

    /**
     * A plain old password, can be a high- or low level password.
     */
    PASSWORD {
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService) {
            return this.keyAccessorTypeReferenceSpecBuilder(propertySpecService, SecurityPropertySpecTranslationKeys.PASSWORD).markRequired().finish();
        }
    },
    /**
     * A key used for encryption of bytes.
     */
    ENCRYPTION_KEY {
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService) {
            return this.keyAccessorTypeReferenceSpecBuilder(propertySpecService, SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY).markRequired().finish();
        }
    },
    /**
     * A key used for authentication to a device.
     */
    AUTHENTICATION_KEY {
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService) {
            return this.keyAccessorTypeReferenceSpecBuilder(propertySpecService, SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY).markRequired().finish();
        }
    },

    /**
     * The certificate that matches the private key of the server (the DLMS device) used for digital signature.
     * The protocols can use this certificate to verify that a received DLMS signature is valid.
     */
    SERVER_SIGNATURE_CERTIFICATE {
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService) {
            return keyAccessorTypeReferenceSpecBuilder(propertySpecService, SecurityPropertySpecTranslationKeys.SERVER_SIGNING_CERTIFICATE).markRequired().finish();
        }
    },

    /**
     * The certificate that matches the private key of the server (the DLMS device) used for key agreement.
     */
    SERVER_KEY_AGREEMENT_CERTIFICATE {
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService) {
            return keyAccessorTypeReferenceSpecBuilder(propertySpecService, SecurityPropertySpecTranslationKeys.SERVER_KEY_AGREEMENT_CERTIFICATE).markRequired().finish();
        }
    },

    /**
     * Defines the phase of Cryptoserver usage.
     * 0: No Cryptoserver usage.
     * 1: Phase 1 communication. The keys are decrypted at runtime using the Cryptoserver. The resulting plain text keys are used for the communication session but are never stored or visible.
     * 2: Phase 2 communication. The keys are never decrypted. Each frame in the communication is encrypted/decrypted by the Cryptoserver, using the encrypted keys.
     * S: Phase S has the same key format as for phase 0. The s indicates the meter has service keys injected.
     */
    CRYPTOSERVER_PHASE {    //TODO: should this also be ReferenceSpec<KeyAccessorType>?
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService) {
            return UPLPropertySpecFactory
                    .specBuilder(SecurityPropertySpecTranslationKeys.CRYPTOSERVER_PHASE.getKey(), false, SecurityPropertySpecTranslationKeys.CRYPTOSERVER_PHASE, propertySpecService::stringSpec)
                    .setDefaultValue("0")
                    .addValues("1", "2", "S")
                    .markExhaustive()
                    .markRequired()
                    .finish();
        }
    },

    SECURITY_LEVEL {    //TODO: should this also be ReferenceSpec<KeyAccessorType>?
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService) {
            return UPLPropertySpecFactory
                    .specBuilder(SecurityPropertySpecTranslationKeys.SECURITY_LEVEL.getKey(), false, SecurityPropertySpecTranslationKeys.SECURITY_LEVEL, propertySpecService::stringSpec)
                    .markRequired()
                    .finish();
        }
    },
    /**
     * A username for ANSI C12 protocols.
     */
    ANSI_C12_USER { //TODO: should this also be ReferenceSpec<KeyAccessorType>?
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService) {
            return UPLPropertySpecFactory
                    .specBuilder(SecurityPropertySpecTranslationKeys.ANSI_C12_USER.getKey(), false, SecurityPropertySpecTranslationKeys.ANSI_C12_USER, propertySpecService::stringSpec)
                    .markRequired()
                    .finish();
        }
    },
    /**
     * A UserId for ANSI C12 protocols.
     */
    ANSI_C12_USER_ID {  //TODO: should this also be ReferenceSpec<KeyAccessorType>?
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService) {
            return UPLPropertySpecFactory
                    .specBuilder(SecurityPropertySpecTranslationKeys.ANSI_C12_USER_ID.getKey(), false, SecurityPropertySpecTranslationKeys.ANSI_C12_USER_ID, propertySpecService::bigDecimalSpec)
                    .setDefaultValue(BigDecimal.ONE)
                    .markRequired()
                    .finish();
        }
    },
    /**
     * Indication for ansi protocols to use a binary password.
     */
    BINARY_PASSWORD {   //TODO: should this also be ReferenceSpec<KeyAccessorType>?
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService) {
            return UPLPropertySpecFactory
                    .specBuilder(SecurityPropertySpecTranslationKeys.BINARY_PASSWORD.getKey(), false, SecurityPropertySpecTranslationKeys.BINARY_PASSWORD, propertySpecService::booleanSpec)
                    .setDefaultValue(false)
                    .markRequired()
                    .finish();
        }
    },
    /**
     * ANSI ap title.
     */
    ANSI_CALLED_AP_TITLE {  //TODO: should this also be ReferenceSpec<KeyAccessorType>?
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService) {
            return UPLPropertySpecFactory
                    .specBuilder(SecurityPropertySpecTranslationKeys.ANSI_CALLED_AP_TITLE.getKey(), false, SecurityPropertySpecTranslationKeys.ANSI_CALLED_AP_TITLE, propertySpecService::stringSpec)
                    .markRequired()
                    .finish();
        }
    },
    /**
     * A key used for encryption of bytes.
     */
    ANSI_SECURITY_KEY { //TODO: should this also be ReferenceSpec<KeyAccessorType>?
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService) {
            return this.encryptedStringSpecBuilder(propertySpecService, SecurityPropertySpecTranslationKeys.ANSI_SECURITY_KEY)
                    .markRequired()
                    .finish();
        }
    },
    /**
     * The manufacturer key used for encryption of bytes.
     */
    MANUFACTURER_ENCRYPTION_KEY {
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService) {
            return this.keyAccessorTypeReferenceSpecBuilder(propertySpecService, SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_MANUFACTURER)
                    .markRequired()
                    .finish();
        }
    },
    /**
     * The customer key used for encryption of bytes.
     */
    CUSTOMER_ENCRYPTION_KEY {
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService) {
            return this.keyAccessorTypeReferenceSpecBuilder(propertySpecService, SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_CUSTOMER)
                    .markRequired()
                    .finish();
        }
    };

    protected PropertySpecBuilder<Object> keyAccessorTypeReferenceSpecBuilder(PropertySpecService propertySpecService, SecurityPropertySpecTranslationKeys securityPropertySpecTranslationKey) {
        return propertySpecService
                .referenceSpec(KeyAccessorType.class.getName())
                .named(securityPropertySpecTranslationKey.getKey(), securityPropertySpecTranslationKey)
                .describedAs(new DescriptionTranslationKey(securityPropertySpecTranslationKey));
    }

    /**
     * Generates a list of possible values for the client mac address property spec
     */
    public static BigDecimal[] getPossibleClientMacAddressValues(int lowerLimit, int upperLimit) {
        return IntStream
                .range(lowerLimit, upperLimit + 1)
                .mapToObj(BigDecimal::valueOf)
                .collect(Collectors.toList())
                .toArray(new BigDecimal[upperLimit - lowerLimit + 1]);
    }

    protected PropertySpecBuilder<String> encryptedStringSpecBuilder(PropertySpecService propertySpecService, SecurityPropertySpecTranslationKeys securityPropertySpecTranslationKey) {
        return propertySpecService
                .encryptedStringSpec()
                .named(securityPropertySpecTranslationKey.getKey(), securityPropertySpecTranslationKey)
                .describedAs(new DescriptionTranslationKey(securityPropertySpecTranslationKey));
    }

    public abstract PropertySpec getPropertySpec(PropertySpecService propertySpecService);

    public PropertySpec getPropertySpec(PropertySpecService propertySpecService, Object defaultValue) {
        return this.getPropertySpec(propertySpecService);
    }

}
