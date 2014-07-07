package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Provides a summary of all DeviceMessages related to configuration/readout of LogBooks
 *
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum LogBookDeviceMessage implements DeviceMessageSpec {

    SetInputChannel(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetInputChannelAttributeName)),
    SetCondition(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetConditionAttributeName)),
    SetConditionValue(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetConditionValueAttributeName)),
    SetTimeTrue(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetTimeTrueAttributeName)),
    SetTimeFalse(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetTimeFalseAttributeName)),
    SetOutputChannel(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetOutputChannelAttributeName)),
    SetAlarm(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetAlarmAttributeName)),
    SetTag(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetTagAttributeName)),
    SetInverse(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetInverseAttributeName)),
    SetImmediate(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetImmediateAttributeName)),
    ReadDebugLogBook(
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(DeviceMessageConstants.fromDateAttributeName),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(DeviceMessageConstants.toDateAttributeName)
    ),
    ReadManufacturerSpecificLogBook(
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(DeviceMessageConstants.fromDateAttributeName),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(DeviceMessageConstants.toDateAttributeName)
    ),
    ResetMainLogbook(),
    ResetCoverLogbook(),
    ResetBreakerLogbook(),
    ResetCommunicationLogbook(),
    ResetLQILogbook(),
    ResetVoltageCutLogbook();

    private static final DeviceMessageCategory category = DeviceMessageCategories.LOG_BOOKS;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private LogBookDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
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
        return LogBookDeviceMessage.class.getSimpleName() + "." + this.toString();
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