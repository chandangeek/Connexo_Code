package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.security.Certificate;

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
     * A plain old password, can be a high- or low level password.
     */
    PASSWORD {
        @Override
        public PropertySpec getPropertySpec() {
            return this.encryptedStringSpecBuilder(SecurityPropertySpecName.PASSWORD).finish();
        }
    },
    /**
     * A key used for encryption of bytes.
     */
    ENCRYPTION_KEY {
        @Override
        public PropertySpec getPropertySpec() {
            return this.encryptedStringSpecBuilder(SecurityPropertySpecName.ENCRYPTION_KEY).finish();
        }
    },
    /**
     * A key used for authentication to a device.
     */
    AUTHENTICATION_KEY {
        @Override
        public PropertySpec getPropertySpec() {
            return this.encryptedStringSpecBuilder(SecurityPropertySpecName.AUTHENTICATION_KEY).finish();
        }
    },
    /**
     * A DLMS clientMacAddress.
     */
    CLIENT_MAC_ADDRESS {
        @Override
        public PropertySpec getPropertySpec() {
            return this.getPropertySpec(BigDecimal.ONE);
        }

        @Override
        public PropertySpec getPropertySpec(Object defaultValue) {
            return this.getPropertySpec((BigDecimal) defaultValue);
        }

        private PropertySpec getPropertySpec(BigDecimal defaultValue) {
            return Services
                    .propertySpecService()
                    .boundedBigDecimalSpec(BigDecimal.ONE, BigDecimal.valueOf(0x7F))
                    .named(SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), SecurityPropertySpecName.AUTHENTICATION_KEY.toString())
                    .describedAs("Description for " + SecurityPropertySpecName.AUTHENTICATION_KEY.toString())
                    .setDefaultValue(defaultValue)
                    .addValues(getPossibleClientMacAddressValues(1, 0x7F))
                    .finish();
        }
    },

    /**
     * The certificate that matches the private key of the server (the DLMS device) used for digital signature.
     * The protocols can use this certificate to verify that a received DLMS signature is valid.
     */
    SERVER_SIGNATURE_CERTIFICATE {
        @Override
        public PropertySpec getPropertySpec() {
            return certificatePropertySpecBuilder(SecurityPropertySpecName.SERVER_SIGNING_CERTIFICATE).finish();
        }
    },

    /**
     * The certificate that matches the private key of the server (the DLMS device) used for key agreement.
     */
    SERVER_KEY_AGREEMENT_CERTIFICATE {
        @Override
        public PropertySpec getPropertySpec() {
            return certificatePropertySpecBuilder(SecurityPropertySpecName.SERVER_KEY_AGREEMENT_CERTIFICATE).finish();
        }
    },

    /**
     * Defines the phase of Cryptoserver usage.
     * 0: No Cryptoserver usage.
     * 1: Phase 1 communication. The keys are decrypted at runtime using the Cryptoserver. The resulting plain text keys are used for the communication session but are never stored or visible.
     * 2: Phase 2 communication. The keys are never decrypted. Each frame in the communication is encrypted/decrypted by the Cryptoserver, using the encrypted keys.
     * S: Phase S has the same key format as for phase 0. The s indicates the meter has service keys injected.
     */
    CRYPTOSERVER_PHASE {
        @Override
        public PropertySpec getPropertySpec() {
            return UPLPropertySpecFactory.stringWithDefault(SecurityPropertySpecName.CRYPTOSERVER_PHASE.toString(), false, "0", "1", "2", "S");
        }
    },

    SECURITY_LEVEL {
        @Override
        public PropertySpec getPropertySpec() {
            return UPLPropertySpecFactory.string(SecurityPropertySpecName.SECURITY_LEVEL.toString(), false);
        }
    },

    /**
     * A character identification of the accessing client.
     */
    DEVICE_ACCESS_IDENTIFIER {
        @Override
        public PropertySpec getPropertySpec() {
            return UPLPropertySpecFactory.string(SecurityPropertySpecName.DEVICE_ACCESS_IDENTIFIER.toString(), false);
        }
    },
    /**
     * A username for ANSI C12 protocols.
     */
    ANSI_C12_USER {
        @Override
        public PropertySpec getPropertySpec() {
            return UPLPropertySpecFactory.string(SecurityPropertySpecName.ANSI_C12_USER.toString(), false);
        }
    },
    /**
     * A UserId for ANSI C12 protocols.
     */
    ANSI_C12_USER_ID {
        @Override
        public PropertySpec getPropertySpec() {
            return UPLPropertySpecFactory.bigDecimal(SecurityPropertySpecName.ANSI_C12_USER_ID.toString(), false, BigDecimal.ONE);
        }
    },
    /**
     * Indication for ansi protocols to use a binary password.
     */
    BINARY_PASSWORD {
        @Override
        public PropertySpec getPropertySpec() {
            return UPLPropertySpecFactory.booleanValue(SecurityPropertySpecName.BINARY_PASSWORD.toString(), false, false);
        }
    },
    /**
     * ANSI ap title.
     */
    ANSI_CALLED_AP_TITLE {
        @Override
        public PropertySpec getPropertySpec() {
            return UPLPropertySpecFactory.string(SecurityPropertySpecName.ANSI_CALLED_AP_TITLE.toString(), false);
        }
    },
    /**
     * A key used for encryption of bytes.
     */
    ANSI_SECURITY_KEY {
        @Override
        public PropertySpec getPropertySpec() {
            return this.encryptedStringSpecBuilder(SecurityPropertySpecName.ANSI_SECURITY_KEY).finish();
        }
    },
    /**
     * The manufacturer key used for encryption of bytes.
     */
    MANUFACTURER_ENCRYPTION_KEY {
        @Override
        public PropertySpec getPropertySpec() {
            return this.encryptedStringSpecBuilder(SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER).finish();
        }
    },
    /**
     * The customer key used for encryption of bytes.
     */
    CUSTOMER_ENCRYPTION_KEY {
        @Override
        public PropertySpec getPropertySpec() {
            return this.encryptedStringSpecBuilder(SecurityPropertySpecName.ENCRYPTION_KEY_CUSTOMER).finish();
        }
    };

    protected static PropertySpecBuilder certificatePropertySpecBuilder(SecurityPropertySpecName name) {
        return Services
                .propertySpecService()
                .referenceSpec(Certificate.class.getName())
                .named(name.toString(), name.toString())
                .describedAs("Description for " + name.toString());
    }

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

    public PropertySpec getPropertySpec(Object defaultValue) {
        return this.getPropertySpec();
    }

}
