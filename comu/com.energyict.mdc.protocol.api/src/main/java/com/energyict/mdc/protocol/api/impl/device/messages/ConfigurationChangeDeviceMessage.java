/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceMessageFile;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

enum ConfigurationChangeDeviceMessage implements DeviceMessageSpecEnum {

    WriteExchangeStatus(DeviceMessageId.CONFIGURATION_CHANGE_WRITE_EXCHANGE_STATUS, "Write exchange status") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.WriteExchangeStatus)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    WriteRadioAcknowledge(DeviceMessageId.CONFIGURATION_CHANGE_WRITE_RADIO_ACKNOWLEDGE, "Write radio acknowledge") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .booleanSpec()
                            .named(DeviceMessageAttributes.WriteRadioAcknowledge)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    WriteRadioUserTimeout(DeviceMessageId.CONFIGURATION_CHANGE_WRITE_RADIO_USER_TIMEOUT, "Write radio user timeout") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(DeviceMessageAttributes.WriteRadioUserTimeout)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    WriteNewPDRNumber(DeviceMessageId.CONFIGURATION_CHANGE_WRITE_NEW_PDR_NUMBER, "Write new PDR number") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.WriteExchangeStatus)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    ConfigureConverterMasterData(DeviceMessageId.CONFIGURATION_CHANGE_CONFIGURE_CONVERTER_MASTER_DATA, "Configure converter master data") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.converterTypeAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues("VOL1", "VOL2", "VEN1", "VEN2")
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.converterSerialNumberAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    ConfigureGasMeterMasterData(DeviceMessageId.CONFIGURATION_CHANGE_CONFIGURE_GAS_METER_MASTER_DATA, "Configure gas meter master data") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.meterTypeAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues("MASS", "USON", "CORI", "VENT", "MEMB", "TURB", "ROTO", "Axxx")
                            .markExhaustive()
                            .finish());
            Stream.of(DeviceMessageAttributes.meterCaliberAttributeName, DeviceMessageAttributes.meterSerialNumberAttributeName)
                .map(attributeName -> propertySpecService
                            .stringSpec()
                            .named(attributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .markExhaustive()
                            .finish())
                .forEach(propertySpecs::add);
        }
    },
    ConfigureGasParameters(DeviceMessageId.CONFIGURATION_CHANGE_CONFIGURE_GAS_PARAMETERS, "Configure gas parameters") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.gasDensityAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.airDensityAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.relativeDensityAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.molecularNitrogenAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.carbonDioxideAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.molecularHydrogenAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.higherCalorificValueAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },

    //EIWeb general messages
    SetDescription(DeviceMessageId.CONFIGURATION_CHANGE_SET_DESCRIPTION, "Set description") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.SetDescriptionAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    SetIntervalInSeconds(DeviceMessageId.CONFIGURATION_CHANGE_SET_INTERVAL_IN_SECONDS, "Set interval in seconds") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.SetIntervalInSecondsAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    SetUpgradeUrl(DeviceMessageId.CONFIGURATION_CHANGE_SET_UPGRADE_URL, "Set upgrade URL") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.SetUpgradeUrlAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    SetUpgradeOptions(DeviceMessageId.CONFIGURATION_CHANGE_SET_UPGRADE_OPTIONS, "Set upgrade options") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.SetUpgradeOptionsAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    SetDebounceTreshold(DeviceMessageId.CONFIGURATION_CHANGE_SET_DEBOUNCE_TRESHOLD, "Set debounce treshold") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.SetDebounceTresholdAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    SetTariffMoment(DeviceMessageId.CONFIGURATION_CHANGE_SET_TARIFF_MOMENT, "Set tariff moment") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.SetTariffMomentAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    SetCommOffset(DeviceMessageId.CONFIGURATION_CHANGE_SET_COMMUNICATION_OFFSET, "Set comm offset") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.SetCommOffsetAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    SetAggIntv(DeviceMessageId.CONFIGURATION_CHANGE_SET_AGGREGATION_INTERVAL, "Set aggregation interval") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.SetAggIntvAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    SetPulseTimeTrue(DeviceMessageId.CONFIGURATION_CHANGE_SET_PULSE_TIME_TRUE, "Set pulse time true") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.SetPulseTimeTrueAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },

    SetDukePowerID(DeviceMessageId.CONFIGURATION_CHANGE_SET_DUKE_POWER_ID, "Set dukepower ID") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.SetDukePowerIDAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    SetDukePowerPassword(DeviceMessageId.CONFIGURATION_CHANGE_SET_DUKE_POWER_PASSWORD, "Set dukepower password") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.SetDukePowerPasswordAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    SetDukePowerIdleTime(DeviceMessageId.CONFIGURATION_CHANGE_SET_DUKE_POWER_IDLE_TIME, "Set dukepower idle time") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.SetDukePowerIdleTimeAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },

    UploadMeterScheme(DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_METER_SCHEME, "Upload meter scheme") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .referenceSpec(DeviceMessageFile.class)
                            .named(ConfigurationChangeDeviceMessageAttributes.MeterScheme)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    UploadSwitchPointClockSettings(DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_SWITCH_POINT_CLOCK_SETTINGS, "Upload switch point clock settings") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .referenceSpec(DeviceMessageFile.class)
                            .named(ConfigurationChangeDeviceMessageAttributes.SwitchPointClockSettings)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    UploadSwitchPointClockUpdateSettings(DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_SWITCH_POINT_CLOCK_UPDATE_SETTINGS, "Upload switch point clock update settings") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .referenceSpec(DeviceMessageFile.class)
                            .named(ConfigurationChangeDeviceMessageAttributes.SwitchPointClockUpdateSettings)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },

    ProgramBatteryExpiryDate(DeviceMessageId.CONFIGURATION_CHANGE_PROGRAM_BATTERY_EXPIRY_DATE, "Program battery expiry date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateFactory())
                            .named(DeviceMessageAttributes.ConfigurationChangeDate)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },

    ChangeOfSupplier(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_OF_SUPPLIER, "Change of supplier") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.ChangeOfSupplierName)
                            .fromThesaurus(thesaurus)
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.ChangeOfSupplierID)
                            .fromThesaurus(thesaurus)
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DeviceMessageAttributes.ConfigurationChangeActivationDate)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    ChangeOfTenancy(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_OF_TENANCY, "Change of tenancy") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DeviceMessageAttributes.ConfigurationChangeActivationDate)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    SetCalorificValue(DeviceMessageId.CONFIGURATION_CHANGE_SET_CALORIFIC_VALUE, "Set calorific value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.CalorificValue)
                            .fromThesaurus(thesaurus)
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DeviceMessageAttributes.ConfigurationChangeActivationDate)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    SetConversionFactor(DeviceMessageId.CONFIGURATION_CHANGE_SET_CONVERSION_FACTOR, "Set conversion factor") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.ConversionFactor)
                            .fromThesaurus(thesaurus)
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DeviceMessageAttributes.ConfigurationChangeActivationDate)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    SetAlarmFilter(DeviceMessageId.CONFIGURATION_CHANGE_SET_ALARM_FILTER, "Set alarm filter") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .hexStringSpec()
                            .named(DeviceMessageAttributes.AlarmFilterAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    ChangeDefaultResetWindow(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_DEFAULT_RESET_WINDOW, "Change default reset window") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.DefaultResetWindowAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    ChangeAdministrativeStatus(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_ADMINISTRATIVE_STATUS, "Change administrative status") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                        .bigDecimalSpec()
                        .named(DeviceMessageAttributes.AdministrativeStatusAttributeName)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .addValues(BigDecimal.ZERO, BigDecimal.ONE, BigDecimals.TWO, BigDecimals.THREE)
                        .markExhaustive()
                        .finish());
        }

    },
    EnableFW(DeviceMessageId.CONFIGURATION_CHANGE_ENABLE_FW, "Enable firmware"),
    DisableFW(DeviceMessageId.CONFIGURATION_CHANGE_DISABLE_FW, "Disable firmware"),
    EnableSSL(DeviceMessageId.CONFIGURATION_CHANGE_ENABLE_SSL, "Enable SSL"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .booleanSpec()
                            .named(DeviceMessageAttributes.enableSSL)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetDeviceName(DeviceMessageId.CONFIGURATION_CHANGE_SET_DEVICENAME, "Set device name"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.deviceName)
                            .fromThesaurus(thesaurus)
                            .setDefaultValue("")
                            .markRequired()
                            .finish());
        }
    },
    SetNTPAddress(DeviceMessageId.CONFIGURATION_CHANGE_SET_NTPADDRESS, "Set NTP address" ){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(ConfigurationChangeDeviceMessageAttributes.ntpAddress)
                            .fromThesaurus(thesaurus)
                            .setDefaultValue("")
                            .markRequired()
                            .finish());
        }
    },
    SYNC_NTP_SERVER(DeviceMessageId.CONFIGURATION_CHANGE_SYNC_NTPSERVER, "Synchronize NTP server"),
    CLEAR_FAULTS_FLAGS(DeviceMessageId.CONFIGURATION_CHANGE_CLEAR_FAULTS_FLAGS, "Clear faults flags"),
    CLEAR_STATISTICAL_VALUES(DeviceMessageId.CONFIGURATION_CHANGE_CLEAR_STATISTICAL_VALUES, "Clear statistical values"),
    ENABLE_DISCOVERY_ON_POWER_UP(DeviceMessageId.CONFIGURATION_CHANGE_ENABLE_DISCOVERY_ON_POWER_UP, "Enable discovery on power up"),
    DISABLE_DISCOVERY_ON_POWER_UP(DeviceMessageId.CONFIGURATION_CHANGE_DISABLE_DISCOVERY_ON_POWER_UP, "Disable discovery on power up"),
    CONFIGURE_AUTOMATIC_DEMAND_RESET(DeviceMessageId.CONFIGURATION_CHANGE_CONFIGURE_AUTOMATIC_DEMAND_RESET, "Configure automatic demand reset") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .booleanSpec()
                            .named(ConfigurationChangeDeviceMessageAttributes.enableAutomaticDemandResetAttributeName)
                            .fromThesaurus(thesaurus)
                            .setDefaultValue(false)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .boundedBigDecimalSpec(BigDecimal.ZERO, BigDecimal.valueOf(31))
                            .named(ConfigurationChangeDeviceMessageAttributes.day)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .boundedBigDecimalSpec(BigDecimal.ZERO, BigDecimal.valueOf(23))
                            .named(ConfigurationChangeDeviceMessageAttributes.hour)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CONFIGURE_MASTER_BOARD_PARAMETERS(DeviceMessageId.CONFIGURATION_CHANGE_CONFIGURE_MASTER_BOARD_PARAMETERS, "Configure master board parameters") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(ConfigurationChangeDeviceMessageAttributes.localMacAddress, ConfigurationChangeDeviceMessageAttributes.maxCredit, ConfigurationChangeDeviceMessageAttributes.zeroCrossDelay, ConfigurationChangeDeviceMessageAttributes.synchronisationBit)
                .map(attributName -> propertySpecService
                        .bigDecimalSpec()
                        .named(attributName)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .setDefaultValue(BigDecimal.valueOf(-1))
                        .finish())
                .forEach(propertySpecs::add);
        }
    }
    ;

    private DeviceMessageId id;
    private String defaultTranslation;

    ConfigurationChangeDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }


    @Override
    public String getKey() {
        return ConfigurationChangeDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultTranslation;
    }

    @Override
    public DeviceMessageId getId() {
        return this.id;
    }

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
        return propertySpecs;
    }

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        // Default behavior is not to add anything
    };

}