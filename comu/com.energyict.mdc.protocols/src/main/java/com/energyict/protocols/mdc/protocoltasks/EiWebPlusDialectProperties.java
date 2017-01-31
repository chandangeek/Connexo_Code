/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;

import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link EiWebPlusDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-27 (08:41)
 */
public class EiWebPlusDialectProperties extends CommonDeviceProtocolDialectProperties {

    public static final String SERVER_LOG_LEVEL_KEY = "serverLogLevel";
    public static final String PORT_LOG_LEVEL_KEY = "portLogLevel";

    public enum TranslationKeys implements TranslationKey {
        SERVER_LOG_LEVEL(SERVER_LOG_LEVEL_KEY, "Server log level"),
        SERVER_LOG_LEVEL_DESCRIPTION(SERVER_LOG_LEVEL_KEY + ".description", "Server log level"),
        PORT_LOG_LEVEL(PORT_LOG_LEVEL_KEY, "Port log level"),
        PORT_LOG_LEVEL_DESCRIPTION(PORT_LOG_LEVEL_KEY + ".description", "Port log level");

        private final String key;
        private final String defaultFormat;

        TranslationKeys(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return "eiwebplus.dialect." + this.key;
        }

        @Override
        public String getDefaultFormat() {
            return this.defaultFormat;
        }
    }

    enum ActualFields {
        SERVER_LOG_LEVEL(SERVER_LOG_LEVEL_KEY, "ServerLogLevel", "SERVERLOGLEVEL") {
            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return propertySpecService
                        .specForValuesOf(new StringFactory())
                        .named(TranslationKeys.SERVER_LOG_LEVEL)
                        .describedAs(TranslationKeys.SERVER_LOG_LEVEL_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .addValues("SEVERE", "WARNING", "INFO", "FINE", "FINER", "FINEST", "ALL")
                        .markExhaustive()
                        .setDefaultValue("OFF")
                        .finish();
            }

        },
        PORT_LOG_LEVEL(PORT_LOG_LEVEL_KEY, "PortLogLevel", "PORTLOGLEVEL") {
            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return propertySpecService
                        .specForValuesOf(new StringFactory())
                        .named(TranslationKeys.PORT_LOG_LEVEL)
                        .describedAs(TranslationKeys.PORT_LOG_LEVEL_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .addValues("OFF", "SEVERE", "WARNING", "INFO", "FINE", "FINER", "FINEST", "ALL")
                        .markExhaustive()
                        .setDefaultValue(EiWebPlusDialect.DEFAULT_LOG_LEVEL)
                        .finish();
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

        public abstract PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus);

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