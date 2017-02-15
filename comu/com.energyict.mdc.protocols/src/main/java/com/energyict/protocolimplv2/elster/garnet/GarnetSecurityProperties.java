/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;

import com.energyict.protocolimplv2.security.DeviceSecurityProperty;

import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link SecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (16:28)
 */
public class GarnetSecurityProperties extends CommonBaseDeviceSecurityProperties {

    public enum ActualFields {
        CUSTOMER_ENCRYPTION_KEY {
            @Override
            public String javaName() {
                return "customerEncryptionKey";
            }

            @Override
            public String databaseName() {
                return "CUST_ENCRYPTIONKEY";
            }
        },
        MANUFACTURER_ENCRYPTION_KEY {
            @Override
            public String javaName() {
                return "manufacturerEncryptionKey";
            }

            @Override
            public String databaseName() {
                return "MANU_ENCRYPTIONKEY";
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
    private String customerEncryptionKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String manufacturerEncryptionKey;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.customerEncryptionKey = (String) getTypedPropertyValue(propertyValues, DeviceSecurityProperty.CUSTOMER_ENCRYPTION_KEY.javaName());
        this.manufacturerEncryptionKey = (String) getTypedPropertyValue(propertyValues, DeviceSecurityProperty.MANUFACTURER_ENCRYPTION_KEY.javaName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, DeviceSecurityProperty.CUSTOMER_ENCRYPTION_KEY.javaName(), this.customerEncryptionKey);
        this.setPropertyIfNotNull(propertySetValues, DeviceSecurityProperty.MANUFACTURER_ENCRYPTION_KEY.javaName(), this.manufacturerEncryptionKey);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}