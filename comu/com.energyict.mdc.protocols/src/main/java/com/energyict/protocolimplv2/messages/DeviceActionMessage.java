package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.protocol.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.impl.RequiredPropertySpecFactory;

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
    ALARM_REGISTER_RESET,
    ERROR_REGISTER_RESET,
    REBOOT_DEVICE,
    DISABLE_WEBSERVER,
    ENABLE_WEBSERVER,
    RESTORE_FACTORY_SETTINGS,
    SetFTIONReboot(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.FTIONReboot)),
    SetFTIONInitialize(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.FTIONInitialize)),
    SetFTIONMailLog(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.FTIONMailLog)),
    SetFTIONSaveConfig(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.FTIONSaveConfig)),
    SetFTIONUpgrade(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.FTIONUpgrade)),
    SetFTIONClearMem(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.FTIONClearMem)),
    SetFTIONMailConfig(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.FTIONMailConfig)),
    SetFTIONModemReset(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.FTIONModemReset)),
    SetChangeAdminPassword(
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.AdminOld),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.AdminNew)
    ),
    SetOutputOn(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.OutputOn)),
    SetOutputOff(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.OutputOff)),
    SetOutputToggle(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.OutputToggle)),
    SetOutputPulse(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.OutputPulse)),
    SetAnalogOut(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, getPossibleValues()),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.AnalogOutValue)
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
