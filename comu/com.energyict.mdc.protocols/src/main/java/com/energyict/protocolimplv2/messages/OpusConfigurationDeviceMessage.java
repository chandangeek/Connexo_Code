package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.messages.*;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum OpusConfigurationDeviceMessage implements DeviceMessageSpec {

    SetOpusOSNbr(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetOpusOSNbrAttributeName)),
    SetOpusPassword(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetOpusPasswordAttributeName)),
    SetOpusTimeout(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetOpusTimeoutAttributeName)),
    SetOpusConfig(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetOpusConfigAttributeName));

    private static final DeviceMessageCategory category = DeviceMessageCategories.OPUS_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private OpusConfigurationDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
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
        return OpusConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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