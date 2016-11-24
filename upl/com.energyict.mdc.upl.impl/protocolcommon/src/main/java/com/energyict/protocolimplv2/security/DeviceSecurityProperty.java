package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;

import com.energyict.dynamicattributes.EncryptedStringFactory;

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
     * A plain old password, can be a high- or low level password
     */
    PASSWORD {
        @Override
        public PropertySpec getPropertySpec() {
            return this.encryptedStringSpecBuilder(SecurityPropertySpecName.PASSWORD).finish();
        }
    },
    /**
     * A key used for encryption of bytes
     */
    ENCRYPTION_KEY {
        @Override
        public PropertySpec getPropertySpec() {
            return this.encryptedStringSpecBuilder(SecurityPropertySpecName.ENCRYPTION_KEY).finish();
        }
    },
    /**
     * A key used for authentication to a device
     */
    AUTHENTICATION_KEY {
        @Override
        public PropertySpec getPropertySpec() {
            return this.encryptedStringSpecBuilder(SecurityPropertySpecName.AUTHENTICATION_KEY).finish();
        }
    },
    /**
     * A DLMS clientMacAddress
     */
    CLIENT_MAC_ADDRESS {
        @Override
        public PropertySpec getPropertySpec() {
            return Services
                    .propertySpecService()
                    .boundedBigDecimalSpec(BigDecimal.valueOf(1), BigDecimal.valueOf(0x7F))
                    .named(SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), SecurityPropertySpecName.AUTHENTICATION_KEY.toString())
                    .describedAs("Description for " + SecurityPropertySpecName.AUTHENTICATION_KEY.toString())
                    .setDefaultValue(BigDecimal.valueOf(1))
                    .addValues(getPossibleClientMacAddressValues(1, 0x7F))
                    .finish();
        }
    },

    /**
     * The certificate that matches the private key of the server (the DLMS device) used for digital signature.
     * The protocols can use this certificate to verify that a received DLMS signature is valid.
     */
    SERVER_SIGNATURE_CERTIFICATE(PropertySpecFactory.certificateWrapperIdPropertySpec(SecurityPropertySpecName.SERVER_SIGNING_CERTIFICATE.toString())),

    /**
     * The certificate that matches the private key of the server (the DLMS device) used for key agreement.
     */
    SERVER_KEY_AGREEMENT_CERTIFICATE(PropertySpecFactory.certificateWrapperIdPropertySpec(SecurityPropertySpecName.SERVER_KEY_AGREEMENT_CERTIFICATE.toString())),

    /**
     * Defines the phase of Cryptoserver usage.
     * 0: No Cryptoserver usage.
     * 1: Phase 1 communication. The keys are decrypted at runtime using the Cryptoserver. The resulting plain text keys are used for the communication session but are never stored or visible.
     * 2: Phase 2 communication. The keys are never decrypted. Each frame in the communication is encrypted/decrypted by the Cryptoserver, using the encrypted keys.
     * S: Phase S has the same key format as for phase 0. The s indicates the meter has service keys injected.
     */
    CRYPTOSERVER_PHASE(PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(
            SecurityPropertySpecName.CRYPTOSERVER_PHASE.toString(),
            "0",
            "0", "1", "2", "S")),

    SECURITY_LEVEL(PropertySpecFactory.stringPropertySpec(SecurityPropertySpecName.SECURITY_LEVEL.toString())),

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

    protected PropertySpecBuilder<String> encryptedStringSpecBuilder(SecurityPropertySpecName name) {
        return Services
                .propertySpecService()
                .encryptedStringSpec()
                .named(name.toString(), name.toString())
                .describedAs("Description for" + name.toString());
    }

    /**
     * Generates a list of possible values for the client mac address property spec
     */
    public static BigDecimal[] getPossibleClientMacAddressValues(int lowerLimit, int upperLimit) {
        return IntStream
                .range(lowerLimit, upperLimit)
                .mapToObj(BigDecimal::valueOf)
                .collect(Collectors.toList())
                .toArray(new BigDecimal[upperLimit - lowerLimit + 1]);
    }

    public abstract PropertySpec getPropertySpec();

}
