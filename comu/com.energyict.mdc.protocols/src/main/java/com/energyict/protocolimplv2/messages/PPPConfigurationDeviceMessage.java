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
public enum PPPConfigurationDeviceMessage implements DeviceMessageSpec {

    SetISP1Phone(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetISP1PhoneAttributeName)),
    SetISP1Username(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetISP1UsernameAttributeName)),
    SetISP1Password(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetISP1PasswordAttributeName)),
    SetISP1Tries(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetISP1TriesAttributeName)),
    SetISP2Phone(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetISP2PhoneAttributeName)),
    SetISP2Username(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetISP2UsernameAttributeName)),
    SetISP2Password(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetISP2PasswordAttributeName)),
    SetISP2Tries(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetISP2TriesAttributeName)),
    SetPPPIdleTimeout(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPPPIdleTimeoutAttributeName)),
    SetPPPRetryInterval(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPPPRetryIntervalAttributeName)),
    SetPPPOptions(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPPPOptionsAttributeName));

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
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
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
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }
}