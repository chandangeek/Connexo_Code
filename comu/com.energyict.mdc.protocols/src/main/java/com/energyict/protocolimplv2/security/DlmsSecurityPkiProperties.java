/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import javax.validation.constraints.Size;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link DlmsSecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (14:19)
 */
public class DlmsSecurityPkiProperties extends CommonBaseDeviceSecurityProperties {

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

            public void addTo(Table table) {
                table
                        .column(this.databaseName())
                        .varChar()
                        .map(this.javaName())
                        .add();
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
                        .upTo(Version.version(10, 3))
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
            Column keyAccessorType = table.column(this.databaseName())
                    .number()
                    .add();
            table.foreignKey("FK_PKI_CPS_"+this.databaseName())
                    .on(keyAccessorType)
                    .references(SecurityAccessorType.class)
                    .map(this.javaName())
                    .add();
        }

    }

    @Size(max = Table.MAX_STRING_LENGTH)
    private String password;
    private Reference<SecurityAccessorType> authenticationKey = Reference.empty();
    private Reference<SecurityAccessorType> encryptionKey = Reference.empty();

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.password = (String) getTypedPropertyValue(propertyValues, SecurityPropertySpecTranslationKeys.PASSWORD.toString());
        this.authenticationKey.set((SecurityAccessorType) propertyValues.getProperty(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString()));
        this.encryptionKey.set((SecurityAccessorType) propertyValues.getProperty(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString()));
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        if (!is(this.password).empty()) {
            setTypedPropertyValueTo(propertySetValues, SecurityPropertySpecTranslationKeys.PASSWORD.toString(), this.password);
        }
        this.setReferencePropertyIfNotNull(propertySetValues, SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString(), this.authenticationKey);
        this.setReferencePropertyIfNotNull(propertySetValues, SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString(), this.encryptionKey);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}
