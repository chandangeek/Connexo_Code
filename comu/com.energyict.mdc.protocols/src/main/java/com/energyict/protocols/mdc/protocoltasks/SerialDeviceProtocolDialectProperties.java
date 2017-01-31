/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;

import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.common.CommonV2TranslationKeys;

import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link SerialDeviceProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-26 (14:35)
 */
public class SerialDeviceProtocolDialectProperties extends CommonDeviceProtocolDialectProperties {

    public enum ActualFields {

        ADDRESSING_MODE("addressingMode", DlmsProtocolProperties.ADDRESSING_MODE, CommonV2TranslationKeys.ADDRESSING_MODE, "ADDRESSINGMODE") {
            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return this.propertySpec(propertySpecService, thesaurus, SerialDeviceProtocolDialect.DEFAULT_ADDRESSING_MODE, SerialDeviceProtocolDialect.PREDEFINED_VALUES_ADDRESSING_MODE);
            }

            @Override
            public void addTo(Table table) {
                this.addAsBigDecimalColumnTo(table);
            }
        },

        INFORMATION_FIELD_SIZE("informationFieldSize", DlmsProtocolProperties.INFORMATION_FIELD_SIZE, CommonV2TranslationKeys.INFORMATION_FIELD_SIZE, "INFOFIELDSIZE"){
            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return this.propertySpec(propertySpecService, thesaurus, SerialDeviceProtocolDialect.DEFAULT_INFORMATION_FIELD_SIZE);
            }

            @Override
            public void addTo(Table table) {
                this.addAsBigDecimalColumnTo(table);
            }
        },

        RETRIES("retries", DlmsProtocolProperties.RETRIES, CommonV2TranslationKeys.RETRIES, "RETRIES") {
            @Override
            public void addTo(Table table) {
                this.addAsBigDecimalColumnTo(table);
            }

            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return this.propertySpec(propertySpecService, thesaurus, SerialDeviceProtocolDialect.DEFAULT_RETRIES);
            }
        },
        TIMEOUT_PROPERTY("timeoutMillis", DlmsProtocolProperties.TIMEOUT, CommonV2TranslationKeys.TIMEOUT, "TIMEOUTMILLIS") {
            @Override
            public void addTo(Table table) {
                this.addAsTimeDurationColumnTo(table);
            }

            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return this.propertySpec(propertySpecService, thesaurus, SerialDeviceProtocolDialect.DEFAULT_TIMEOUT);
            }
        },

        ROUND_TRIP_CORRECTION("roundTripCorrection", DlmsProtocolProperties.ROUND_TRIP_CORRECTION, CommonV2TranslationKeys.ROUNDTRIP_CORRECTION, "ROUNDTRIPCORRECTION"){
            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return this.propertySpec(propertySpecService, thesaurus, SerialDeviceProtocolDialect.DEFAULT_ROUND_TRIP_CORRECTION);
            }

            @Override
            public void addTo(Table table) {
                this.addAsBigDecimalColumnTo(table);
            }
        }
        ;

        private final String javaName;
        private final String propertySpecName;
        private final CommonV2TranslationKeys translationKey;
        private final String databaseName;

        ActualFields(String javaName, String propertySpecName, CommonV2TranslationKeys translationKey, String databaseName) {
            this.javaName = javaName;
            this.propertySpecName = propertySpecName;
            this.translationKey = translationKey;
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

        protected PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus, BigDecimal defaultValue) {
            return propertySpecService
                    .bigDecimalSpec()
                    .named(this.propertySpecName, this.translationKey)
                    .fromThesaurus(thesaurus)
                    .setDefaultValue(defaultValue)
                    .finish();
        }

        protected PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus, BigDecimal defaultValue, BigDecimal... predefinedValues) {
            return propertySpecService
                    .bigDecimalSpec()
                    .named(this.propertySpecName, this.translationKey)
                    .fromThesaurus(thesaurus)
                    .setDefaultValue(defaultValue)
                    .addValues(predefinedValues)
                    .markExhaustive()
                    .finish();
        }

        protected PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus, TimeDuration defaultValue) {
            return propertySpecService
                    .timeDurationSpec()
                    .named(this.propertySpecName, this.translationKey)
                    .fromThesaurus(thesaurus)
                    .setDefaultValue(defaultValue)
                    .finish();
        }

        public abstract void addTo(Table table);

        protected void addAsBigDecimalColumnTo(Table table) {
            table
                    .column(this.databaseName())
                    .number()
                    .map(this.javaName())
                    .add();
        }

        protected void addAsTimeDurationColumnTo(Table table) {
            table
                    .column(this.databaseName() + "VALUE")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(this.javaName() + ".count")
                    .add();
            table
                    .column(this.databaseName() + "UNIT")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(this.javaName() + ".timeUnitCode")
                    .add();
        }
    }

    private BigDecimal retries;
    private TimeDuration timeoutMillis;
    private BigDecimal addressingMode;
    private BigDecimal informationFieldSize;
    private BigDecimal roundTripCorrection;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.retries = (BigDecimal) propertyValues.getProperty(ActualFields.RETRIES.propertySpecName());
        this.timeoutMillis = (TimeDuration) propertyValues.getProperty(ActualFields.TIMEOUT_PROPERTY.propertySpecName());
        this.addressingMode = (BigDecimal) propertyValues.getProperty(ActualFields.ADDRESSING_MODE.propertySpecName());
        this.informationFieldSize = (BigDecimal) propertyValues.getProperty(ActualFields.INFORMATION_FIELD_SIZE.propertySpecName());
        this.roundTripCorrection = (BigDecimal) propertyValues.getProperty(ActualFields.ROUND_TRIP_CORRECTION.propertySpecName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.RETRIES.propertySpecName(), this.retries);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.TIMEOUT_PROPERTY.propertySpecName(), this.timeoutMillis);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.ADDRESSING_MODE.propertySpecName(), this.addressingMode);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.INFORMATION_FIELD_SIZE.propertySpecName(), this.informationFieldSize);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.ROUND_TRIP_CORRECTION.propertySpecName(), this.roundTripCorrection);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}