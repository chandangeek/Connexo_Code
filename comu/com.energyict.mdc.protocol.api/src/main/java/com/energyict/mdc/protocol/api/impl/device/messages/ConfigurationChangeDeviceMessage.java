package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 4/06/13
 * Time: 9:45
 * Author: khe
 */
public enum ConfigurationChangeDeviceMessage implements DeviceMessageSpecEnum {

    WriteExchangeStatus(DeviceMessageId.CONFIGURATION_CHANGE_WRITE_EXCHANGE_STATUS, "Write exchange status") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.WriteExchangeStatus, true, new BigDecimalFactory()));
        }
    },
    WriteRadioAcknowledge(DeviceMessageId.CONFIGURATION_CHANGE_WRITE_RADIO_ACKNOWLEDGE, "Write radio acknowledge") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.WriteRadioAcknowledge, true, new BooleanFactory()));
        }
    },
    WriteRadioUserTimeout(DeviceMessageId.CONFIGURATION_CHANGE_WRITE_RADIO_USER_TIMEOUT, "Write radio user timeout") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.WriteRadioUserTimeout, true, new TimeDurationValueFactory()));
        }
    },
    WriteNewPDRNumber(DeviceMessageId.CONFIGURATION_CHANGE_WRITE_NEW_PDR_NUMBER, "Write new PDR number") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.WriteRadioUserTimeout, true, new StringFactory()));
        }
    },
    ConfigureConverterMasterData(DeviceMessageId.CONFIGURATION_CHANGE_CONFIGURE_CONVERTER_MASTER_DATA, "Configure converter master data") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(DeviceMessageConstants.converterTypeAttributeName, true, "VOL1", "VOL2", "VEN1", "VEN2"));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.converterSerialNumberAttributeName, true, new StringFactory()));
        }
    },
    ConfigureGasMeterMasterData(DeviceMessageId.CONFIGURATION_CHANGE_CONFIGURE_GAS_METER_MASTER_DATA, "Configure gas meter master data") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(DeviceMessageConstants.meterTypeAttributeName, true, "MASS", "USON", "CORI", "VENT", "MEMB", "TURB", "ROTO", "Axxx"));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.meterCaliberAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.meterSerialNumberAttributeName, true, new StringFactory()));
        }
    },
    ConfigureGasParameters(DeviceMessageId.CONFIGURATION_CHANGE_CONFIGURE_GAS_PARAMETERS, "Configure gas parameters") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.gasDensityAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.airDensityAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.relativeDensityAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.molecularNitrogenAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.carbonDioxideAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.molecularHydrogenAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.higherCalorificValueAttributeName, true, new BigDecimalFactory()));
        }
    },

    //EIWeb general messages
    SetDescription(DeviceMessageId.CONFIGURATION_CHANGE_SET_DESCRIPTION, "Set description") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetDescriptionAttributeName, true, new StringFactory()));
        }
    },
    SetIntervalInSeconds(DeviceMessageId.CONFIGURATION_CHANGE_SET_INTERVAL_IN_SECONDS, "Set interval in seconds") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetIntervalInSecondsAttributeName, true, new StringFactory()));
        }
    },
    SetUpgradeUrl(DeviceMessageId.CONFIGURATION_CHANGE_SET_UPGRADE_URL, "Set upgrade URL") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetUpgradeUrlAttributeName, true, new StringFactory()));
        }
    },
    SetUpgradeOptions(DeviceMessageId.CONFIGURATION_CHANGE_SET_UPGRADE_OPTIONS, "Set upgrade options") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetUpgradeOptionsAttributeName, true, new StringFactory()));
        }
    },
    SetDebounceTreshold(DeviceMessageId.CONFIGURATION_CHANGE_SET_DEBOUNCE_TRESHOLD, "Set debounce treshold") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetDebounceTresholdAttributeName, true, new StringFactory()));
        }
    },
    SetTariffMoment(DeviceMessageId.CONFIGURATION_CHANGE_SET_TARIFF_MOMENT, "Set tariff moment") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetTariffMomentAttributeName, true, new StringFactory()));
        }
    },
    SetCommOffset(DeviceMessageId.CONFIGURATION_CHANGE_SET_COMMUNICATION_OFFSET, "Set comm offset") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetCommOffsetAttributeName, true, new StringFactory()));
        }
    },
    SetAggIntv(DeviceMessageId.CONFIGURATION_CHANGE_SET_AGGREGATION_INTERVAL, "Set aggregation interval") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetAggIntvAttributeName, true, new StringFactory()));
        }
    },
    SetPulseTimeTrue(DeviceMessageId.CONFIGURATION_CHANGE_SET_PULSE_TIME_TRUE, "Set pulse time true") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetPulseTimeTrueAttributeName, true, new StringFactory()));
        }
    },

    SetDukePowerID(DeviceMessageId.CONFIGURATION_CHANGE_SET_DUKE_POWER_ID, "Set dukepower ID") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetDukePowerIDAttributeName, true, new StringFactory()));
        }
    },
    SetDukePowerPassword(DeviceMessageId.CONFIGURATION_CHANGE_SET_DUKE_POWER_PASSWORD, "Set dukepower password") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetDukePowerPasswordAttributeName, true, new StringFactory()));
        }
    },
    SetDukePowerIdleTime(DeviceMessageId.CONFIGURATION_CHANGE_SET_DUKE_POWER_IDLE_TIME, "Set dukepower idle time") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetDukePowerIdleTimeAttributeName, true, new StringFactory()));
        }
    },

    UploadMeterScheme(DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_METER_SCHEME, "Upload meter scheme") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(DeviceMessageConstants.MeterScheme, true, FactoryIds.USERFILE));
        }
    },
    UploadSwitchPointClockSettings(DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_SWITCH_POINT_CLOCK_SETTINGS, "Upload switch point clock settings") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(DeviceMessageConstants.SwitchPointClockSettings, true, FactoryIds.USERFILE));
        }
    },
    UploadSwitchPointClockUpdateSettings(DeviceMessageId.CONFIGURATION_CHANGE_UPLOAD_SWITCH_POINT_CLOCK_UPDATE_SETTINGS, "Upload switch point clock update settings") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(DeviceMessageConstants.SwitchPointClockUpdateSettings, true, FactoryIds.USERFILE));
        }
    },

    ProgramBatteryExpiryDate(DeviceMessageId.CONFIGURATION_CHANGE_PROGRAM_BATTERY_EXPIRY_DATE, "Program battery expiry date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ConfigurationChangeDate, true, new DateFactory()));
        }
    },

    ChangeOfSupplier(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_OF_SUPPLIER, "Change of supplier") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ChangeOfSupplierName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ChangeOfSupplierID, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate, true, new DateAndTimeFactory()));
        }
    },
    ChangeOfTenancy(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_OF_TENANCY, "Change of tenancy") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate, true, new DateAndTimeFactory()));
        }
    },
    SetCalorificValue(DeviceMessageId.CONFIGURATION_CHANGE_SET_CALORIFIC_VALUE, "Set calorific value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.CalorificValue, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate, true, new DateAndTimeFactory()));
        }
    },
    SetConversionFactor(DeviceMessageId.CONFIGURATION_CHANGE_SET_CONVERSION_FACTOR, "Set conversion factor") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ConversionFactor, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate, true, new DateAndTimeFactory()));
        }
    },
    SetAlarmFilter(DeviceMessageId.CONFIGURATION_CHANGE_SET_ALARM_FILTER, "Set alarm filter") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.AlarmFilterAttributeName, true, new HexStringFactory()));
        }
    },
    ChangeDefaultResetWindow(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_DEFAULT_RESET_WINDOW, "Change default reset window") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.DefaultResetWindowAttributeName, true, new BigDecimalFactory()));
        }
    },
    ChangeAdministrativeStatus(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_ADMINISTRATIVE_STATUS, "Change administrative status") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(
                    propertySpecService.bigDecimalPropertySpecWithValues(
                            DeviceMessageConstants.AdministrativeStatusAttributeName,
                            true,
                            BigDecimal.ZERO,
                            BigDecimal.ONE,
                            BigDecimals.TWO,
                            BigDecimals.THREE));
        }

    },
    EnableFW(DeviceMessageId.CONFIGURATION_CHANGE_ENABLE_FW, "Enable firmware"),
    DisableFW(DeviceMessageId.CONFIGURATION_CHANGE_DISABLE_FW, "Disable firmware"),
    EnableSSL(DeviceMessageId.CONFIGURATION_CHANGE_ENABLE_SSL, "Enable SSL"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.enableSSL, true, new BooleanFactory()));
        }
    }
    , SetDeviceName(DeviceMessageId.CONFIGURATION_CHANGE_SET_DEVICENAME, "Set device name"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.deviceName, true, ""));
        }
    }, SetNTPAddress(DeviceMessageId.CONFIGURATION_CHANGE_SET_NTPADDRESS, "Set NTP address" ){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.ntpAddress, true, ""));
        }
    }
    , SYNC_NTP_SERVER(DeviceMessageId.CONFIGURATION_CHANGE_SYNC_NTPSERVER, "Synchronize NTP server")
    , CLEAR_FAULTS_FLAGS(DeviceMessageId.CONFIGURATION_CHANGE_CLEAR_FAULTS_FLAGS, "Clear faults flags")
    , CLEAR_STATISTICAL_VALUES(DeviceMessageId.CONFIGURATION_CHANGE_CLEAR_STATISTICAL_VALUES, "Clear statistical values")
    , ENABLE_DISCOVERY_ON_POWER_UP(DeviceMessageId.CONFIGURATION_CHANGE_ENABLE_DISCOVERY_ON_POWER_UP, "Enable discovery on power up")
    , DISABLE_DISCOVERY_ON_POWER_UP(DeviceMessageId.CONFIGURATION_CHANGE_DISABLE_DISCOVERY_ON_POWER_UP, "Disable discovery on power up")
    , CONFIGURE_AUTOMATIC_DEMAND_RESET(DeviceMessageId.CONFIGURATION_CHANGE_CONFIGURE_AUTOMATIC_DEMAND_RESET, "Configure automatic demand reset"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.booleanPropertySpec(DeviceMessageConstants.enableAutomaticDemandResetAttributeName, true, false));
            propertySpecs.add(propertySpecService.boundedDecimalPropertySpec(DeviceMessageConstants.day, true, BigDecimal.valueOf(0), BigDecimal.valueOf(31)));
            propertySpecs.add(propertySpecService.boundedDecimalPropertySpec(DeviceMessageConstants.hour, true, BigDecimal.valueOf(0), BigDecimal.valueOf(23)));
        }
    }, CONFIGURE_MASTER_BOARD_PARAMETERS(DeviceMessageId.CONFIGURATION_CHANGE_CONFIGURE_MASTER_BOARD_PARAMETERS, "Configure master board parameters") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.localMacAddress, true, BigDecimal.valueOf(-1)));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.maxCredit, true, BigDecimal.valueOf(-1)));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.zeroCrossDelay, true, BigDecimal.valueOf(-1)));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.synchronisationBit, true, BigDecimal.valueOf(-1)));
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

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService);
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}