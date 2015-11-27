package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;

import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link EiWebPlusDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-27 (08:41)
 */
class EiWebPlusDialectProperties extends CommonDeviceProtocolDialectProperties {

    enum ActualFields {
        SERVER_LOG_LEVEL("serverLogLevel", "ServerLogLevel", "SERVERLOGLEVEL") {
            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService) {
                return propertySpecService
                        .stringPropertySpecWithValuesAndDefaultValue(
                                this.propertySpecName(),
                                false,
                                EiWebPlusDialect.DEFAULT_LOG_LEVEL,
                                "OFF", "SEVERE", "WARNING", "INFO", "FINE", "FINER", "FINEST", "ALL");
            }

        },
        PORT_LOG_LEVEL("portLogLevel", "PortLogLevel", "PORTLOGLEVEL") {
            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService) {
                return propertySpecService
                        .stringPropertySpecWithValuesAndDefaultValue(
                                this.propertySpecName(),
                                false,
                                EiWebPlusDialect.DEFAULT_LOG_LEVEL,
                                "OFF", "SEVERE", "WARNING", "INFO", "FINE", "FINER", "FINEST", "ALL");
            }

        };

        private final String javaName;
        private final String propertySpecName;
        private final String databaseName;

        ActualFields(String javaName, String propertySpecName, String databaseName) {
            this.javaName = javaName;
            this.propertySpecName = propertySpecName;
            this.databaseName = databaseName;
        }

        public String javaName() {
            return this.javaName;
        }

        public String propertySpecName() {
            return this.propertySpecName;
        }

        public String databaseName() {
            return this.databaseName;
        }

        public abstract PropertySpec propertySpec(PropertySpecService propertySpecService);

        public void addTo(Table table) {
            table
                .column(this.databaseName())
                .varChar()
                .map(this.javaName())
                .add();
        }

    }

    @Size(max=Table.MAX_STRING_LENGTH)
    private String serverLogLevel;
    @Size(max=Table.MAX_STRING_LENGTH)
    private String portLogLevel;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.serverLogLevel = (String) propertyValues.getProperty(ActualFields.SERVER_LOG_LEVEL.propertySpecName());
        this.portLogLevel = (String) propertyValues.getProperty(ActualFields.PORT_LOG_LEVEL.propertySpecName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.SERVER_LOG_LEVEL.propertySpecName(), this.serverLogLevel);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.PORT_LOG_LEVEL.propertySpecName(), this.portLogLevel);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}