package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;
import com.energyict.protocols.naming.SecurityPropertySpecName;

import javax.validation.constraints.Size;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link Mtu155SecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (17:29)
 */
public class MTU155SecurityProperties extends CommonBaseDeviceSecurityProperties {

    public enum ActualFields {
        PASSWORD("password", DeviceSecurityProperty.PASSWORD.javaName()) {
            @Override
            protected String getValue(MTU155SecurityProperties perClientProperties) {
                return perClientProperties.password;
            }

            @Override
            protected void setValue(MTU155SecurityProperties perClientProperties, String value) {
                perClientProperties.password = value;
            }

            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService, thesaurus);
            }
        },
        SERVICE_ENCRYPTION_KEY("serviceEncryptionKey", SecurityPropertySpecName.ENCRYPTION_KEY_2.getKey()) {
            @Override
            protected String getValue(MTU155SecurityProperties perClientProperties) {
                return perClientProperties.serviceEncryptionKey;
            }

            @Override
            protected void setValue(MTU155SecurityProperties perClientProperties, String value) {
                perClientProperties.serviceEncryptionKey = value;
            }

            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return propertySpecService
                        .encryptedStringSpec()
                        .named(SecurityPropertySpecName.ENCRYPTION_KEY_2)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish();
            }
        },
        FACTORY_ENCRYPTION_KEY("factoryEncryptionKey", SecurityPropertySpecName.ENCRYPTION_KEY_3.getKey()) {
            @Override
            protected String getValue(MTU155SecurityProperties perClientProperties) {
                return perClientProperties.factoryEncryptionKey;
            }

            @Override
            protected void setValue(MTU155SecurityProperties perClientProperties, String value) {
                perClientProperties.factoryEncryptionKey = value;
            }

            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return propertySpecService
                        .encryptedStringSpec()
                        .named(SecurityPropertySpecName.ENCRYPTION_KEY_3)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish();
            }
        },
        TEMPORARY_ENCRYPTION_KEY("temporaryEncryptionKey", SecurityPropertySpecName.ENCRYPTION_KEY_1.getKey()) {
            @Override
            protected String getValue(MTU155SecurityProperties perClientProperties) {
                return perClientProperties.temporaryEncryptionKey;
            }

            @Override
            protected void setValue(MTU155SecurityProperties perClientProperties, String value) {
                perClientProperties.temporaryEncryptionKey = value;
            }

            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return propertySpecService
                        .encryptedStringSpec()
                        .named(SecurityPropertySpecName.ENCRYPTION_KEY_1)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish();
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

        public abstract PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus);

        public void copyPropertyTo(CustomPropertySetValues propertySetValues, MTU155SecurityProperties perClientProperties) {
            perClientProperties.setPropertyIfNotNull(propertySetValues, this.propertySpecName().toString(), this.getValue(perClientProperties));
        }

        public void copyPropertyFrom(CustomPropertySetValues propertySetValues, MTU155SecurityProperties perClientProperties) {
            this.setValue(perClientProperties, (String) propertySetValues.getProperty(this.propertySpecName().toString()));
        }

        protected abstract String getValue(MTU155SecurityProperties perClientProperties);
        protected abstract void setValue(MTU155SecurityProperties perClientProperties, String value);
    }

    @Size(max = Table.MAX_STRING_LENGTH)
    private String password;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String serviceEncryptionKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String factoryEncryptionKey;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String temporaryEncryptionKey;
    @Size(max = Table.MAX_STRING_LENGTH)

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        Stream
            .of(ActualFields.values())
            .forEach(field -> field.copyPropertyFrom(propertyValues, this));
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        Stream
            .of(ActualFields.values())
            .forEach(field -> field.copyPropertyTo(propertySetValues, this));
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