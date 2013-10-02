package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.*;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum SMSConfigurationDeviceMessage implements DeviceMessageSpec {

    SetSmsDataNbr(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSmsDataNbrAttributeName)),
    SetSmsAlarmNbr(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSmsAlarmNbrAttributeName)),
    SetSmsEvery(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSmsEveryAttributeName)),
    SetSmsNbr(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSmsNbrAttributeName)),
    SetSmsCorrection(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSmsCorrectionAttributeName)),
    SetSmsConfig(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSmsConfigAttributeName));

    private static final DeviceMessageCategory category = DeviceMessageCategories.SMS_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private SMSConfigurationDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
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
        return SMSConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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