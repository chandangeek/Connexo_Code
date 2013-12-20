package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.RequiredPropertySpecFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Provides a summary of all messages related to a <i>Display</i>
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 8:38
 */
public enum DisplayDeviceMessage implements DeviceMessageSpec {

    CONSUMER_MESSAGE_CODE_TO_PORT_P1(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.p1InformationAttributeName)),
    CONSUMER_MESSAGE_TEXT_TO_PORT_P1(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.p1InformationAttributeName)),
    SET_DISPLAY_MESSAGE(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.DisplayMessageAttributeName)),
    SET_DISPLAY_MESSAGE_WITH_OPTIONS(
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.DisplayMessageAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.DisplayMessageTimeDurationAttributeName),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(DeviceMessageConstants.DisplayMessageActivationDate)
    ),
    SET_DISPLAY_MESSAGE_ON_IHD_WITH_OPTIONS(
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.DisplayMessageAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.DisplayMessageTimeDurationAttributeName),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(DeviceMessageConstants.DisplayMessageActivationDate)
    ),
    CLEAR_DISPLAY_MESSAGE;

    private static final DeviceMessageCategory displayCategory = DeviceMessageCategories.DISPLAY;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private DisplayDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return displayCategory;
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
        return DisplayDeviceMessage.class.getSimpleName() + "." + this.toString();
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
