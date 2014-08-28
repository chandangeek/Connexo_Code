package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;

import com.elster.jupiter.properties.PropertySpec;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PPPConfigurationDeviceMessage implements DeviceMessageSpec {

    SetISP1Phone(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetISP1PhoneAttributeName)),
    SetISP1Username(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetISP1UsernameAttributeName)),
    SetISP1Password(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetISP1PasswordAttributeName)),
    SetISP1Tries(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetISP1TriesAttributeName)),
    SetISP2Phone(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetISP2PhoneAttributeName)),
    SetISP2Username(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetISP2UsernameAttributeName)),
    SetISP2Password(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetISP2PasswordAttributeName)),
    SetISP2Tries(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetISP2TriesAttributeName)),
    SetPPPIdleTimeout(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetPPPIdleTimeoutAttributeName)),
    SetPPPRetryInterval(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetPPPRetryIntervalAttributeName)),
    SetPPPOptions(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetPPPOptionsAttributeName));

    private static final DeviceMessageCategory category = DeviceMessageCategories.PPP_PARAMETERS;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private PPPConfigurationDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
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
        return PPPConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
    public
    DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }
}