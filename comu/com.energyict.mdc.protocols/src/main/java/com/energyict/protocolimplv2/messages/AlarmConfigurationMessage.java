package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;

import com.elster.jupiter.properties.PropertySpec;

import java.util.Arrays;
import java.util.List;

/**
 * Provides a summary of all messages related to configuring alarms
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 8:38
 */
public enum AlarmConfigurationMessage implements DeviceMessageSpec {

    RESET_ALL_ALARM_BITS,
    WRITE_ALARM_FILTER(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.alarmFilterAttributeName));

    private static final DeviceMessageCategory displayCategory = DeviceMessageCategories.ALARM_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private AlarmConfigurationMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return displayCategory;
    }

    @Override
    public String getName() {
        return this.getNameResourceKey();
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return AlarmConfigurationMessage.class.getSimpleName() + "." + this.toString();
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
