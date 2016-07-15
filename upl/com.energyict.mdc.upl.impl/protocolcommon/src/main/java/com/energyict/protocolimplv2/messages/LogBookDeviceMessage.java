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
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum LogBookDeviceMessage implements DeviceMessageSpec {

    SetInputChannel(0, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetInputChannelAttributeName)),
    SetCondition(1, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetConditionAttributeName)),
    SetConditionValue(2, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetConditionValueAttributeName)),
    SetTimeTrue(3, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetTimeTrueAttributeName)),
    SetTimeFalse(4, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetTimeFalseAttributeName)),
    SetOutputChannel(5, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetOutputChannelAttributeName)),
    SetAlarm(6, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetAlarmAttributeName)),
    SetTag(7, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetTagAttributeName)),
    SetInverse(8, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetInverseAttributeName)),
    SetImmediate(9, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetImmediateAttributeName)),
    ReadDebugLogBook(10,
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.fromDateAttributeName),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.toDateAttributeName)
    ),
    ReadManufacturerSpecificLogBook(11,
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.fromDateAttributeName),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.toDateAttributeName)
    ),
    ResetMainLogbook(12),
    ResetCoverLogbook(13),
    ResetBreakerLogbook(14),
    ResetCommunicationLogbook(15),
    ResetLQILogbook(16),
    ResetVoltageCutLogbook(17),
    ReadLogBook(18);

    private static final DeviceMessageCategory category = DeviceMessageCategories.LOG_BOOKS;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    LogBookDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
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

    @Override
    public int getMessageId() {
        return id;
    }
}