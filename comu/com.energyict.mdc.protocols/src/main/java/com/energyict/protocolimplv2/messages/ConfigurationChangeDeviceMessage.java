package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.protocol.dynamic.RequiredPropertySpecFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 4/06/13
 * Time: 9:45
 * Author: khe
 */
public enum ConfigurationChangeDeviceMessage implements DeviceMessageSpec {

    WriteExchangeStatus(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.WriteExchangeStatus)),
    WriteRadioAcknowledge(RequiredPropertySpecFactory.newInstance().notNullableBooleanPropertySpec(DeviceMessageConstants.WriteRadioAcknowledge)),
    WriteRadioUserTimeout(RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(DeviceMessageConstants.WriteRadioUserTimeout)),
    WriteNewPDRNumber(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.newPDRAttributeName)),
    ConfigureConverterMasterData(
            RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValues(DeviceMessageConstants.converterTypeAttributeName, "VOL1", "VOL2", "VEN1", "VEN2"),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.converterSerialNumberAttributeName)),
    ConfigureGasMeterMasterData(
            RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValues(DeviceMessageConstants.meterTypeAttributeName, "MASS", "USON", "CORI", "VENT", "MEMB", "TURB", "ROTO", "Axxx"),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.meterCaliberAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.meterSerialNumberAttributeName)),
    ConfigureGasParameters(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.gasDensityAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.airDensityAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.relativeDensityAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.molecularNitrogenAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.carbonDioxideAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.molecularHydrogenAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.higherCalorificValueAttributeName)),

    //EIWeb general messages
    SetDescription(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetDescriptionAttributeName)),
    SetIntervalInSeconds(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetIntervalInSecondsAttributeName)),
    SetUpgradeUrl(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetUpgradeUrlAttributeName)),
    SetUpgradeOptions(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetUpgradeOptionsAttributeName)),
    SetDebounceTreshold(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetDebounceTresholdAttributeName)),
    SetTariffMoment(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetTariffMomentAttributeName)),
    SetCommOffset(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetCommOffsetAttributeName)),
    SetAggIntv(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetAggIntvAttributeName)),
    SetPulseTimeTrue(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetPulseTimeTrueAttributeName)),

    SetDukePowerID(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetDukePowerIDAttributeName)),
    SetDukePowerPassword(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetDukePowerPasswordAttributeName)),
    SetDukePowerIdleTime(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetDukePowerIdleTimeAttributeName)),

    UploadMeterScheme(RequiredPropertySpecFactory.newInstance().userFileReferencePropertySpec(DeviceMessageConstants.MeterScheme)),
    UploadSwitchPointClockSettings(RequiredPropertySpecFactory.newInstance().userFileReferencePropertySpec(DeviceMessageConstants.SwitchPointClockSettings)),
    UploadSwitchPointClockUpdateSettings(RequiredPropertySpecFactory.newInstance().userFileReferencePropertySpec(DeviceMessageConstants.SwitchPointClockUpdateSettings)),

    ProgramBatteryExpiryDate(RequiredPropertySpecFactory.newInstance().datePropertySpec(DeviceMessageConstants.ConfigurationChangeDate)),

    ChangeOfSupplier(
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.ChangeOfSupplierName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.ChangeOfSupplierID),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate)
    ),
    ChangeOfTenancy(RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate)),
    SetCalorificValue(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CalorificValue),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate)
    ),
    SetConversionFactor(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.ConversionFactor),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate)
    ),
    SetAlarmFilter(RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.AlarmFilterAttributeName)),
    ChangeDefaultResetWindow(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.DefaultResetWindowAttributeName)),
    ChangeAdministrativeStatus(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpecWithValues(
                    DeviceMessageConstants.AdministrativeStatusAttributeName,
                    new BigDecimal(0),
                    new BigDecimal(1),
                    new BigDecimal(2),
                    new BigDecimal(3)
            )
    );

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private ConfigurationChangeDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

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
        return deviceMessagePropertySpecs;
    }

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