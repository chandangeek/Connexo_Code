package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Provides a summary of all messages related to general Device Actions
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 11:59
 */
public enum DeviceActionMessage implements DeviceMessageSpec {

    BILLING_RESET,
    GLOBAL_METER_RESET,
    DEMAND_RESET,
    POWER_OUTAGE_RESET,
    POWER_QUALITY_RESET,
    ERROR_STATUS_RESET,
    REGISTERS_RESET,
    LOAD_LOG_RESET,
    EVENT_LOG_RESET,
    SetFTIONReboot(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.FTIONReboot)),
    SetFTIONInitialize(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.FTIONInitialize)),
    SetFTIONMailLog(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.FTIONMailLog)),
    SetFTIONSaveConfig(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.FTIONSaveConfig)),
    SetFTIONUpgrade(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.FTIONUpgrade)),
    SetFTIONClearMem(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.FTIONClearMem)),
    SetFTIONMailConfig(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.FTIONMailConfig)),
    SetFTIONModemReset(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.FTIONModemReset)),
    SetChangeAdminPassword(
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.AdminOld),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.AdminNew)
    ),
    SetOutputOn(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.OutputOn)),
    SetOutputOff(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.OutputOff)),
    SetOutputToggle(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.OutputToggle)),
    SetOutputPulse(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.OutputPulse)),
    SetAnalogOut(
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, getPossibleValues()),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.AnalogOutValue)
    );

    private static BigDecimal[] getPossibleValues() {
        BigDecimal[] result = new BigDecimal[16];
        for (int index = 17; index <= 32; index++) {
            result[index - 17] = BigDecimal.valueOf(index);
        }
        return result;
    }

    private static final DeviceMessageCategory category = DeviceMessageCategories.DEVICE_ACTIONS;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private DeviceActionMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    private static String translate(final String key) {
        return UserEnvironment.getDefault().getTranslation(key);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return translate(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return DeviceActionMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.deviceMessagePropertySpecs;
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
