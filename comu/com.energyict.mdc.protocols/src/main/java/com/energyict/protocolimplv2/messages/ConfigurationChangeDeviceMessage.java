package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.dynamic.BigDecimalFactory;
import com.energyict.mdc.dynamic.BooleanFactory;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.StringFactory;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;

import com.energyict.protocols.mdc.services.impl.Bus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 4/06/13
 * Time: 9:45
 * Author: khe
 */
public enum ConfigurationChangeDeviceMessage implements DeviceMessageSpec {

    WriteExchangeStatus {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.WriteExchangeStatus, true, new BigDecimalFactory()));
        }
    },
    WriteRadioAcknowledge {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.WriteRadioAcknowledge, true, new BooleanFactory()));
        }
    },
    WriteRadioUserTimeout {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.WriteRadioUserTimeout, true, new TimeDurationValueFactory()));
        }
    },
    WriteNewPDRNumber {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.WriteRadioUserTimeout, true, new StringFactory()));
        }
    },
    ConfigureConverterMasterData {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(DeviceMessageConstants.converterTypeAttributeName, true, "VOL1", "VOL2", "VEN1", "VEN2"));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.converterSerialNumberAttributeName, true, new StringFactory()));
        }
    },
    ConfigureGasMeterMasterData {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(DeviceMessageConstants.meterTypeAttributeName, true, "MASS", "USON", "CORI", "VENT", "MEMB", "TURB", "ROTO", "Axxx"));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.meterCaliberAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.meterSerialNumberAttributeName, true, new StringFactory()));
        }
    },
    ConfigureGasParameters {
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
    SetDescription {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetDescriptionAttributeName, true, new StringFactory()));
        }
    },
    SetIntervalInSeconds {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetIntervalInSecondsAttributeName, true, new StringFactory()));
        }
    },
    SetUpgradeUrl {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetUpgradeUrlAttributeName, true, new StringFactory()));
        }
    },
    SetUpgradeOptions {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetUpgradeOptionsAttributeName, true, new StringFactory()));
        }
    },
    SetDebounceTreshold {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetDebounceTresholdAttributeName, true, new StringFactory()));
        }
    },
    SetTariffMoment {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetTariffMomentAttributeName, true, new StringFactory()));
        }
    },
    SetCommOffset {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetCommOffsetAttributeName, true, new StringFactory()));
        }
    },
    SetAggIntv {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetAggIntvAttributeName, true, new StringFactory()));
        }
    },
    SetPulseTimeTrue {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetPulseTimeTrueAttributeName, true, new StringFactory()));
        }
    },

    SetDukePowerID {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetDukePowerIDAttributeName, true, new StringFactory()));
        }
    },
    SetDukePowerPassword {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetDukePowerPasswordAttributeName, true, new StringFactory()));
        }
    },
    SetDukePowerIdleTime {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SetDukePowerIdleTimeAttributeName, true, new StringFactory()));
        }
    },

    UploadMeterScheme {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(DeviceMessageConstants.MeterScheme, true, FactoryIds.USERFILE));
        }
    },
    UploadSwitchPointClockSettings {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(DeviceMessageConstants.SwitchPointClockSettings, true, FactoryIds.USERFILE));
        }
    },
    UploadSwitchPointClockUpdateSettings {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(DeviceMessageConstants.SwitchPointClockUpdateSettings, true, FactoryIds.USERFILE));
        }
    },

    ProgramBatteryExpiryDate {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ConfigurationChangeDate, true, new DateFactory()));
        }
    },

    ChangeOfSupplier {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ChangeOfSupplierName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ChangeOfSupplierID, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate, true, new DateAndTimeFactory()));
        }
    },
    ChangeOfTenancy {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate, true, new DateAndTimeFactory()));
        }
    },
    SetCalorificValue {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.CalorificValue, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate, true, new DateAndTimeFactory()));
        }
    },
    SetConversionFactor {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ConversionFactor, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate, true, new DateAndTimeFactory()));
        }
    },
    SetAlarmFilter {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.AlarmFilterAttributeName, true, new HexStringFactory()));
        }
    },
    ChangeDefaultResetWindow {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.DefaultResetWindowAttributeName, true, new BigDecimalFactory()));
        }
    },
    ChangeAdministrativeStatus {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(
                    propertySpecService.bigDecimalPropertySpecWithValues(
                            DeviceMessageConstants.AdministrativeStatusAttributeName,
                            true,
                            BigDecimal.ZERO,
                            BigDecimal.ONE,
                            new BigDecimal(2),
                            new BigDecimal(3)));
        }
    };

    @Override
    public DeviceMessageCategory getCategory() {
        return DeviceMessageCategories.CONFIGURATION_CHANGE;
    }

    @Override
    public String getName() {
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return ConfigurationChangeDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, Bus.getPropertySpecService());
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }
}