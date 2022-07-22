package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;

import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import javax.validation.constraints.Size;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link DlmsSecuritySupportPerClient}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (14:46)
 */
public class DlmsSecurityPerClientProperties extends CommonBaseDeviceSecurityProperties {

    @Size(max = Table.MAX_STRING_LENGTH)
    private String publicPassword;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String dataPassword;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String extraDataPassword;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String managementPassword;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String firmwarePassword;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String manufacturerPassword;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String publicEncryptionKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String dataEncryptionKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String extraDataEncryptionKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String managementEncryptionKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String firmwareEncryptionKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String manufacturerEncryptionKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String publicAuthenticationKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String dataAuthenticationKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String extraDataAuthenticationKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String managementAuthenticationKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String firmwareAuthenticationKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String manufacturerAuthenticationKey;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        Stream
                .of(ActualFields.values())
                .forEach(field -> field.setValue(this, (String) getTypedPropertyValue(propertyValues, field.propertySpecName().getKey())));
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        Stream
                .of(ActualFields.values())
                .forEach(field -> field.copyPropertyTo(propertySetValues, this));
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

    public enum ActualFields {
        PUBLIC_PASSWORD("publicPassword", SecurityPropertySpecTranslationKeys.PASSWORD_PUBLIC) {
            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.publicPassword;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.publicPassword = value;
            }
        },
        DATA_PASSWORD("dataPassword", SecurityPropertySpecTranslationKeys.PASSWORD_DATA) {
            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.dataPassword;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.dataPassword = value;
            }
        },
        EXTRA_DATA_PASSWORD("extraDataPassword", SecurityPropertySpecTranslationKeys.PASSWORD_EXT_DATA) {
            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.extraDataPassword;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.extraDataPassword = value;
            }
        },
        MANAGEMENT_PASSWORD("managementPassword", SecurityPropertySpecTranslationKeys.PASSWORD_MANAGEMENT) {
            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.managementPassword;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.managementPassword = value;
            }
        },
        FIRMWARE_PASSWORD("firmwarePassword", SecurityPropertySpecTranslationKeys.PASSWORD_FIRMWARE) {
            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.firmwarePassword;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.firmwarePassword = value;
            }
        },
        MANUFACTURER_PASSWORD("manufacturerPassword", SecurityPropertySpecTranslationKeys.PASSWORD_MANUFACTURER) {
            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.manufacturerPassword;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.manufacturerPassword = value;
            }
        },
        PUBLIC_ENCRYPTION_KEY("publicEncryptionKey", SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_PUBLIC) {
            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.publicEncryptionKey;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.publicEncryptionKey = value;
            }
        },
        DATA_ENCRYPTION_KEY("dataEncryptionKey", SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_DATA) {
            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.dataEncryptionKey;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.dataEncryptionKey = value;
            }
        },
        EXTRA_DATA_ENCRYPTION_KEY("extraDataEncryptionKey", SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_EXT_DATA) {
            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.extraDataEncryptionKey;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.extraDataEncryptionKey = value;
            }
        },
        MANAGEMENT_ENCRYPTION_KEY("managementEncryptionKey", SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_MANAGEMENT) {
            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.managementEncryptionKey;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.managementEncryptionKey = value;
            }
        },
        FIRMWARE_ENCRYPTION_KEY("firmwareEncryptionKey", SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_FIRMWARE) {
            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.firmwareEncryptionKey;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.firmwareEncryptionKey = value;
            }
        },
        MANUFACTURER_ENCRYPTION_KEY("manufacturerEncryptionKey", SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY_MANUFACTURER) {
            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.manufacturerEncryptionKey;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.manufacturerEncryptionKey = value;
            }
        },
        PUBLIC_AUTHENTICATION_KEY("publicAuthenticationKey", SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_PUBLIC) {
            @Override
            public String databaseName() {
                return "PUBLIC_AUTH_KEY";
            }

            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.publicAuthenticationKey;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.publicAuthenticationKey = value;
            }
        },
        DATA_AUTHENTICATION_KEY("dataAuthenticationKey", SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_DATA) {
            @Override
            public String databaseName() {
                return "DATA_AUTH_KEY";
            }

            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.dataAuthenticationKey;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.dataAuthenticationKey = value;
            }
        },
        EXTRA_DATA_AUTHENTICATION_KEY("extraDataAuthenticationKey", SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_EXT_DATA) {
            @Override
            public String databaseName() {
                return "EXTRA_DATA_AUTH_KEY";
            }

            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.extraDataAuthenticationKey;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.extraDataAuthenticationKey = value;
            }
        },
        MANAGEMENT_AUTHENTICATION_KEY("managementAuthenticationKey", SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_MANAGEMENT) {
            @Override
            public String databaseName() {
                return "MANAGEMENT_AUTH_KEY";
            }

            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.managementAuthenticationKey;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.managementAuthenticationKey = value;
            }
        },
        FIRMWARE_AUTHENTICATION_KEY("firmwareAuthenticationKey", SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_FIRMWARE) {
            @Override
            public String databaseName() {
                return "FIRMWARE_AUTH_KEY";
            }

            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.firmwareAuthenticationKey;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.firmwareAuthenticationKey = value;
            }
        },
        MANUFACTURER_AUTHENTICATION_KEY("manufacturerAuthenticationKey", SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY_MANUFACTURER) {
            @Override
            public String databaseName() {
                return "MANUFACTURER_AUTH_KEY";
            }

            @Override
            protected String getValue(DlmsSecurityPerClientProperties perClientProperties) {
                return perClientProperties.manufacturerAuthenticationKey;
            }

            @Override
            protected void setValue(DlmsSecurityPerClientProperties perClientProperties, String value) {
                perClientProperties.manufacturerAuthenticationKey = value;
            }
        };

        private final String javaName;
        private final SecurityPropertySpecTranslationKeys propertySpecName;

        ActualFields(String javaName, SecurityPropertySpecTranslationKeys propertySpecName) {
            this.javaName = javaName;
            this.propertySpecName = propertySpecName;
        }

        public String javaName() {
            return this.javaName;
        }

        public String databaseName() {
            return name();
        }

        public SecurityPropertySpecTranslationKeys propertySpecName() {
            return propertySpecName;
        }

        public void addTo(Table table) {
            table
                    .column(this.databaseName())
                    .varChar()
                    .map(this.javaName)
                    .add();
        }

        public void copyPropertyTo(CustomPropertySetValues propertySetValues, DlmsSecurityPerClientProperties perClientProperties) {
            perClientProperties.setPropertyIfNotNull(propertySetValues, this.propertySpecName().getKey(), this.getValue(perClientProperties));
        }

        protected abstract String getValue(DlmsSecurityPerClientProperties perClientProperties);

        protected abstract void setValue(DlmsSecurityPerClientProperties perClientProperties, String value);
    }
}
