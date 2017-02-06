/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.KeyAccessorType;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;

import javax.validation.constraints.Size;
import java.math.BigDecimal;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link DlmsSecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (14:19)
 */
public class DlmsSecurityCryptogaphicProperties extends CommonBaseDeviceSecurityProperties {

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

    @Size(max = Table.MAX_STRING_LENGTH)
    private String password;
    private BigDecimal clientMacAddress;
    private Reference<KeyAccessorType> authenticationKey = Reference.empty();
    private Reference<KeyAccessorType> encryptionKey = Reference.empty();

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.password = (String) getTypedPropertyValue(propertyValues, DeviceSecurityProperty.PASSWORD.javaName());
        this.clientMacAddress = (BigDecimal) getTypedPropertyValue(propertyValues, DeviceSecurityProperty.CLIENT_MAC_ADDRESS
                .javaName());
        this.authenticationKey.set((KeyAccessorType) getTypedPropertyValue(propertyValues, DeviceSecurityProperty.AUTHENTICATION_KEY.javaName()));
        this.encryptionKey.set((KeyAccessorType) getTypedPropertyValue(propertyValues, DeviceSecurityProperty.ENCRYPTION_KEY.javaName()));
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        if (!is(this.password).empty()) {
            setTypedPropertyValueTo(propertySetValues, DeviceSecurityProperty.PASSWORD.javaName(), this.password);
        }
        this.setPropertyIfNotNull(propertySetValues, DeviceSecurityProperty.CLIENT_MAC_ADDRESS.javaName(), this.clientMacAddress);
        this.setPropertyIfNotNull(propertySetValues, DeviceSecurityProperty.AUTHENTICATION_KEY.javaName(), this.authenticationKey);
        this.setPropertyIfNotNull(propertySetValues, DeviceSecurityProperty.ENCRYPTION_KEY.javaName(), this.encryptionKey);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}