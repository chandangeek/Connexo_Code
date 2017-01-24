package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
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
 * Copyrights EnergyICT
 * Date: 17/10/16
 * Time: 14:47
 */
public class GatewayTcpDeviceProtocolDialectProperties extends CommonDeviceProtocolDialectProperties {


    enum ActualFields {
        RETRIES("retries", DlmsProtocolProperties.RETRIES, CommonV2TranslationKeys.RETRIES, "RETRIES") {
            @Override
            public void addTo(Table table) {
                this.addAsBigDecimalColumnTo(table);
            }

            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return this.propertySpec(propertySpecService, thesaurus, BigDecimal.ZERO);
            }
        },
        TIMEOUT_PROPERTY("timeoutMillis", DlmsProtocolProperties.TIMEOUT, CommonV2TranslationKeys.TIMEOUT, "TIMEOUTMILLIS") {
            @Override
            public void addTo(Table table) {
                this.addAsTimeDurationColumnTo(table);
            }

            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return this.propertySpec(propertySpecService, thesaurus, GatewayTcpDeviceProtocolDialect.DEFAULT_TIMEOUT);
            }
        },
        ROUND_TRIP_CORRECTION("roundTripCorrection", DlmsProtocolProperties.ROUND_TRIP_CORRECTION, CommonV2TranslationKeys.ROUNDTRIP_CORRECTION, "ROUND_TRIP_CORRECTION") {
            @Override
            public void addTo(Table table) {
                this.addAsTimeDurationColumnTo(table);
            }

            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return this.propertySpec(propertySpecService, thesaurus, GatewayTcpDeviceProtocolDialect.DEFAULT_DELAY_AFTER_ERROR);
            }
        };

        private final String javaName;
        private final String propertySpecName;
        private final TranslationKey translationKey;
        private final String databaseName;

        ActualFields(String javaName, String propertySpecName, TranslationKey translationKey, String databaseName) {
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

        ;

        protected PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus, TimeDuration defaultValue) {
            return propertySpecService
                    .timeDurationSpec()
                    .named(this.propertySpecName, this.translationKey)
                    .fromThesaurus(thesaurus)
                    .setDefaultValue(defaultValue)
                    .finish();
        }

        ;

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
    private TimeDuration roundTripCorrection;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.retries = (BigDecimal) propertyValues.getProperty(ActualFields.RETRIES.propertySpecName());
        this.timeoutMillis = (TimeDuration) propertyValues.getProperty(ActualFields.TIMEOUT_PROPERTY.propertySpecName());
        this.roundTripCorrection = (TimeDuration) propertyValues.getProperty(ActualFields.ROUND_TRIP_CORRECTION.propertySpecName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.RETRIES.propertySpecName(), this.retries);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.TIMEOUT_PROPERTY.propertySpecName(), this.timeoutMillis);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.ROUND_TRIP_CORRECTION.propertySpecName(), this.roundTripCorrection);
    }

    @Override
    public void validateDelete() {

    }
}
