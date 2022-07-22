package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;

import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import javax.validation.constraints.Size;
import java.math.BigDecimal;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link DlmsSecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (14:19)
 */
public class DlmsSecurityProperties extends CommonBaseDeviceSecurityProperties {

    @Size(max = Table.MAX_STRING_LENGTH)
    private String password;
    private BigDecimal clientMacAddress;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String authenticationKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String encryptionKey;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.password = (String) getTypedPropertyValue(propertyValues, SecurityPropertySpecTranslationKeys.PASSWORD.toString());
        this.clientMacAddress = (BigDecimal) getTypedPropertyValue(propertyValues, SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.toString());
        this.authenticationKey = (String) getTypedPropertyValue(propertyValues, SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString());
        this.encryptionKey = (String) getTypedPropertyValue(propertyValues, SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        if (!is(this.password).empty()) {
            setTypedPropertyValueTo(propertySetValues, SecurityPropertySpecTranslationKeys.PASSWORD.toString(), this.password);
        }
        this.setPropertyIfNotNull(propertySetValues, SecurityPropertySpecTranslationKeys.CLIENT_MAC_ADDRESS.toString(), this.clientMacAddress);
        this.setPropertyIfNotNull(propertySetValues, SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString(), this.authenticationKey);
        this.setPropertyIfNotNull(propertySetValues, SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString(), this.encryptionKey);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

    public enum ActualFields {
        PASSWORD {
            @Override
            public String javaName() {
                return "password";
            }

            @Override
            public String databaseName() {
                return "PASSWORD";
            }
        },
        CLIENT_MAC_ADDRESS {
            @Override
            public String javaName() {
                return "clientMacAddress";
            }

            @Override
            public String databaseName() {
                return "CLIENTMACADDRESS";
            }

            @Override
            public void addTo(Table table) {
                table
                        .column(this.databaseName())
                        .number()
                        .map(this.javaName())
                        .add();
            }
        },
        AUTHENTICATION_KEY {
            @Override
            public String javaName() {
                return "authenticationKey";
            }

            @Override
            public String databaseName() {
                return "AUTHKEY";
            }
        },
        ENCRYPTION_KEY {
            @Override
            public String javaName() {
                return "encryptionKey";
            }

            @Override
            public String databaseName() {
                return "ENCRYPTIONKEY";
            }
        };

        public abstract String javaName();

        public abstract String databaseName();

        public void addTo(Table table) {
            table
                    .column(this.databaseName())
                    .varChar()
                    .map(this.javaName())
                    .add();
        }

    }
}
