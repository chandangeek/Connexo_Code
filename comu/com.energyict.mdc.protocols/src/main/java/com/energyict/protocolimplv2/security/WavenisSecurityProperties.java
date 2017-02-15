/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;

import javax.validation.constraints.Size;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link WavenisSecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-20 (11:30)
 */
public class WavenisSecurityProperties extends CommonBaseDeviceSecurityProperties {

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
    @Size(max = Table.MAX_STRING_LENGTH)
    private String encryptionKey;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.password = (String) getTypedPropertyValue(propertyValues, DeviceSecurityProperty.PASSWORD.javaName());
        this.encryptionKey = (String) getTypedPropertyValue(propertyValues, DeviceSecurityProperty.ENCRYPTION_KEY.javaName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        if (!is(this.password).empty()) {
            setTypedPropertyValueTo(propertySetValues, DeviceSecurityProperty.PASSWORD.javaName(), this.password);
        }
        this.setPropertyIfNotNull(propertySetValues, DeviceSecurityProperty.ENCRYPTION_KEY.javaName(), this.encryptionKey);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}