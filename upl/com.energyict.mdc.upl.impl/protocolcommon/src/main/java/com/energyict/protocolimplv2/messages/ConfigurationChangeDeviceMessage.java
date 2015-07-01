package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 4/06/13
 * Time: 9:45
 * Author: khe
 */
public enum ConfigurationChangeDeviceMessage implements DeviceMessageSpec {

    WriteExchangeStatus(0, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.WriteExchangeStatus)),
    WriteRadioAcknowledge(1, PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.WriteRadioAcknowledge)),
    WriteRadioUserTimeout(2, PropertySpecFactory.timeDurationPropertySpec(DeviceMessageConstants.WriteRadioUserTimeout)),
    WriteNewPDRNumber(3, PropertySpecFactory.fixedLengthStringPropertySpec(DeviceMessageConstants.newPDRAttributeName, 14)),
    ConfigureConverterMasterData(4,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.converterTypeAttributeName, "VOL1", "VOL2", "VEN1", "VEN2"),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.converterSerialNumberAttributeName)),
    ConfigureGasMeterMasterData(5,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.meterTypeAttributeName, "MASS", "USON", "CORI", "VENT", "MEMB", "TURB", "ROTO", "Axxx"),
            PropertySpecFactory.boundedDecimalPropertySpec(
                    DeviceMessageConstants.meterCaliberAttributeName,
                    new BigDecimal(0),
                    new BigDecimal(999999)
            ),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.meterSerialNumberAttributeName)),
    ConfigureGasParameters(6,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.gasDensityAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.airDensityAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.relativeDensityAttributeName),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.molecularNitrogenPercentageAttributeName, new BigDecimal(0), new BigDecimal(100)),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.carbonDioxidePercentageAttributeName, new BigDecimal(0), new BigDecimal(100)),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.molecularHydrogenPercentageAttributeName, new BigDecimal(0), new BigDecimal(100)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.higherCalorificValueAttributeName)),

    //EIWeb general messages
    SetDescription(7, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDescriptionAttributeName)),
    SetIntervalInSeconds(8, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetIntervalInSecondsAttributeName)),
    SetUpgradeUrl(9, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetUpgradeUrlAttributeName)),
    SetUpgradeOptions(10, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetUpgradeOptionsAttributeName)),
    SetDebounceTreshold(11, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDebounceTresholdAttributeName)),
    SetTariffMoment(12, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetTariffMomentAttributeName)),
    SetCommOffset(13, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetCommOffsetAttributeName)),
    SetAggIntv(14, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetAggIntvAttributeName)),
    SetPulseTimeTrue(15, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPulseTimeTrueAttributeName)),

    SetDukePowerID(16, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDukePowerIDAttributeName)),
    SetDukePowerPassword(17, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDukePowerPasswordAttributeName)),
    SetDukePowerIdleTime(18, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDukePowerIdleTimeAttributeName)),

    UploadMeterScheme(19, PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.MeterScheme)),
    UploadSwitchPointClockSettings(20, PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.SwitchPointClockSettings)),
    UploadSwitchPointClockUpdateSettings(21, PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.SwitchPointClockUpdateSettings)),

    ProgramBatteryExpiryDate(22, PropertySpecFactory.datePropertySpec(DeviceMessageConstants.ConfigurationChangeDate)),

    ChangeOfSupplier(23,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.ChangeOfSupplierName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ChangeOfSupplierID),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate)
    ),
    ChangeOfTenancy(24, PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate)),
    SetCalorificValue(25,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CalorificValue),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate)
    ),
    SetConversionFactor(26,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ConversionFactor),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate)
    ),
    SetAlarmFilter(27, PropertySpecFactory.fixedLengthHexStringPropertySpec(DeviceMessageConstants.AlarmFilterAttributeName, 4)),
    ChangeDefaultResetWindow(28, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.DefaultResetWindowAttributeName)),
    ChangeAdministrativeStatus(29,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(
                    DeviceMessageConstants.AdministrativeStatusAttributeName,
                    new BigDecimal(0),
                    new BigDecimal(1),
                    new BigDecimal(2),
                    new BigDecimal(3)
            )
    ),
    BootSyncEnable(30, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.enableBootSync)),
    WhitelistedPhoneNumbers(31, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.whiteListPhoneNumbersAttributeName)),
    EnableFW(32),
    DisableFW(33),
    EnableSSL(35, PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.enableSSL)),
    SetDeviceName(36, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.deviceName)),
    SetNTPAddress(37, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.ntpAddress)),
    Clear_Faults_Flags(38),
    Clear_Statistical_Values(39),
    SyncNTPServer(40),
    ConfigureAutomaticDemandReset(41,
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.enableAutomaticDemandResetAttributeName),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.day, BigDecimal.valueOf(0), BigDecimal.valueOf(31)),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.hour, BigDecimal.valueOf(0), BigDecimal.valueOf(23))
    ),
    ENABLE_DISCOVERY_ON_POWER_UP(43),
    DISABLE_DISCOVERY_ON_POWER_UP(44),
    ConfigureMasterBoardParameters(45,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.localMacAddress, BigDecimal.valueOf(-1)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.maxCredit, BigDecimal.valueOf(-1)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.zeroCrossDelay, BigDecimal.valueOf(-1)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.synchronisationBit, BigDecimal.valueOf(-1))
    ),
    ;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private ConfigurationChangeDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    public int getMessageId() {
        return id;
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

    private static class Constants {

        private static final Date DEFAULT_DATE = new Date(978307200000l);   // 01/01/2001
        private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");
    }
}