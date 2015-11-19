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
    private String customerEencryptionKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String manufacturerEencryptionKey;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.customerEencryptionKey = (String) propertyValues.getProperty(DeviceSecurityProperty.CUSTOMER_ENCRYPTION_KEY.javaName());
        this.manufacturerEencryptionKey = (String) propertyValues.getProperty(DeviceSecurityProperty.MANUFACTURER_ENCRYPTION_KEY.javaName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, DeviceSecurityProperty.CUSTOMER_ENCRYPTION_KEY.javaName(), this.customerEencryptionKey);
        this.setPropertyIfNotNull(propertySetValues, DeviceSecurityProperty.MANUFACTURER_ENCRYPTION_KEY.javaName(), this.manufacturerEencryptionKey);
    }

    private void setPropertyIfNotNull(CustomPropertySetValues propertySetValues, String propertyName, Object propertyValue) {
        if (propertyValue != null) {
            propertySetValues.setProperty(propertyName, propertyValue);
        }
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}