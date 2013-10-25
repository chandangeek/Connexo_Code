package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 4/06/13
 * Time: 9:45
 * Author: khe
 */
public enum ConfigurationChangeDeviceMessage implements DeviceMessageSpec {

    IEC1107LimitPowerQuality(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.powerQualityThresholdAttributeName)),
    WriteExchangeStatus(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.WriteExchangeStatus)),
    WriteRadioAcknowledge(PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.WriteRadioAcknowledge)),
    WriteRadioUserTimeout(PropertySpecFactory.timeDurationPropertySpec(DeviceMessageConstants.WriteRadioUserTimeout)),
    EnableOrDisableDST(PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.enableDSTAttributeName)),
    SetEndOfDST(
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.month),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.dayOfMonth),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.dayOfWeek),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.hour)),
    SetStartOfDST(
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.month),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.dayOfMonth),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.dayOfWeek),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.hour)),
    WriteNewPDRNumber(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.newPDRAttributeName)),
    ConfigureConverterMasterData(
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.converterTypeAttributeName, "VOL1", "VOL2", "VEN1", "VEN2"),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.converterSerialNumberAttributeName)),
    ConfigureGasMeterMasterData(
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.meterTypeAttributeName, "MASS", "USON", "CORI", "VENT", "MEMB", "TURB", "ROTO", "Axxx"),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.meterCaliberAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.meterSerialNumberAttributeName)),
    ConfigureGasParameters(
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.gasDensityAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.airDensityAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.relativeDensityAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.molecularNitrogenAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.carbonDioxideAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.molecularHydrogenAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.higherCalorificValueAttributeName)),

    //EIWeb general messages
    SetDescription(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDescriptionAttributeName)),
    SetIntervalInSeconds(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetIntervalInSecondsAttributeName)),
    SetUpgradeUrl(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetUpgradeUrlAttributeName)),
    SetUpgradeOptions(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetUpgradeOptionsAttributeName)),
    SetDebounceTreshold(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDebounceTresholdAttributeName)),
    SetTariffMoment(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetTariffMomentAttributeName)),
    SetCommOffset(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetCommOffsetAttributeName)),
    SetAggIntv(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetAggIntvAttributeName)),
    SetPulseTimeTrue(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPulseTimeTrueAttributeName)),

    SetDukePowerID(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDukePowerIDAttributeName)),
    SetDukePowerPassword(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDukePowerPasswordAttributeName)),
    SetDukePowerIdleTime(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDukePowerIdleTimeAttributeName)),

    UploadMeterScheme(PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.MeterScheme)),
    UploadSwitchPointClockSettings(PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.SwitchPointClockSettings)),
    UploadSwitchPointClockUpdateSettings(PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.SwitchPointClockUpdateSettings)),

    ProgramBatteryExpiryDate(PropertySpecFactory.datePropertySpec(DeviceMessageConstants.ConfigurationChangeDate));

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