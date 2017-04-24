package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;

import javax.validation.constraints.Size;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link Mtu155SecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (17:29)
 */
public class MTU155SecurityProperties extends CommonBaseDeviceSecurityProperties {

    @Size(max = Table.MAX_STRING_LENGTH)
    private String password;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String serviceEncryptionKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String factoryEncryptionKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String temporaryEncryptionKey;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        Stream
                .of(ActualFields.values())
                .forEach(field -> field.setValue(this, (String) getTypedPropertyValue(propertyValues, field.propertySpecName())));
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
        PASSWORD("password", com.energyict.protocolimplv2.security.SecurityPropertySpecName.PASSWORD.toString()) {
            @Override
            protected String getValue(MTU155SecurityProperties perClientProperties) {
                return perClientProperties.password;
            }

            @Override
            protected void setValue(MTU155SecurityProperties perClientProperties, String value) {
                perClientProperties.password = value;
            }
        },
        SERVICE_ENCRYPTION_KEY("serviceEncryptionKey", com.energyict.protocolimplv2.security.SecurityPropertySpecName.ENCRYPTION_KEY_2.toString()) {
            @Override
            protected String getValue(MTU155SecurityProperties perClientProperties) {
                return perClientProperties.serviceEncryptionKey;
            }

            @Override
            protected void setValue(MTU155SecurityProperties perClientProperties, String value) {
                perClientProperties.serviceEncryptionKey = value;
            }
        },
        FACTORY_ENCRYPTION_KEY("factoryEncryptionKey", com.energyict.protocolimplv2.security.SecurityPropertySpecName.ENCRYPTION_KEY_3.toString()) {
            @Override
            protected String getValue(MTU155SecurityProperties perClientProperties) {
                return perClientProperties.factoryEncryptionKey;
            }

            @Override
            protected void setValue(MTU155SecurityProperties perClientProperties, String value) {
                perClientProperties.factoryEncryptionKey = value;
            }
        },
        TEMPORARY_ENCRYPTION_KEY("temporaryEncryptionKey", com.energyict.protocolimplv2.security.SecurityPropertySpecName.ENCRYPTION_KEY_1.toString()) {
            @Override
            protected String getValue(MTU155SecurityProperties perClientProperties) {
                return perClientProperties.temporaryEncryptionKey;
            }

            @Override
            protected void setValue(MTU155SecurityProperties perClientProperties, String value) {
                perClientProperties.temporaryEncryptionKey = value;
            }
        };

        private final String javaName;
        private final String propertySpecName;

        ActualFields(String javaName, String propertySpecName) {
            this.javaName = javaName;
            this.propertySpecName = propertySpecName;
        }

        public String javaName() {
            return this.javaName;
        }

        public String databaseName() {
            return name();
        }

        public String propertySpecName() {
            return propertySpecName;
        }

        public void addTo(Table table) {
            table
                    .column(this.databaseName())
                    .varChar()
                    .map(this.javaName)
                    .add();
        }

        public void copyPropertyTo(CustomPropertySetValues propertySetValues, MTU155SecurityProperties perClientProperties) {
            perClientProperties.setPropertyIfNotNull(propertySetValues, this.propertySpecName(), this.getValue(perClientProperties));
        }

        protected abstract String getValue(MTU155SecurityProperties perClientProperties);

        protected abstract void setValue(MTU155SecurityProperties perClientProperties, String value);
    }

}