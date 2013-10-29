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
 * Provides a summary of all DeviceMessages related to configuration/readout of LogBooks
 *
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum LogBookDeviceMessage implements DeviceMessageSpec {

    SetInputChannel(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetInputChannelAttributeName)),
    SetCondition(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetConditionAttributeName)),
    SetConditionValue(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetConditionValueAttributeName)),
    SetTimeTrue(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetTimeTrueAttributeName)),
    SetTimeFalse(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetTimeFalseAttributeName)),
    SetOutputChannel(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetOutputChannelAttributeName)),
    SetAlarm(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetAlarmAttributeName)),
    SetTag(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetTagAttributeName)),
    SetInverse(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetInverseAttributeName)),
    SetImmediate(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetImmediateAttributeName)),
    ReadDebugLogBook(
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.fromDateAttributeName),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.toDateAttributeName)
    ),
    ReadManufacturerSpecificLogBook(
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.fromDateAttributeName),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.toDateAttributeName)
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