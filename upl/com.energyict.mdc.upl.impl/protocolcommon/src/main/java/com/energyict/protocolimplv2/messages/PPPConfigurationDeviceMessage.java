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
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PPPConfigurationDeviceMessage implements DeviceMessageSpec {

    SetISP1Phone(0, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetISP1PhoneAttributeName)),
    SetISP1Username(1, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetISP1UsernameAttributeName)),
    SetISP1Password(2, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetISP1PasswordAttributeName)),
    SetISP1Tries(3, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetISP1TriesAttributeName)),
    SetISP2Phone(4, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetISP2PhoneAttributeName)),
    SetISP2Username(5, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetISP2UsernameAttributeName)),
    SetISP2Password(6, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetISP2PasswordAttributeName)),
    SetISP2Tries(7, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetISP2TriesAttributeName)),
    SetPPPIdleTimeout(8, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPPPIdleTimeoutAttributeName)),
    SetPPPRetryInterval(9, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPPPRetryIntervalAttributeName)),
    SetPPPOptions(10, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPPPOptionsAttributeName)),
    SetPPPIdleTime(11, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.SetPPPIdleTime)),
    PPPSetOption(12, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.singleOptionAttributeName)),
    PPPClrOption(13, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.singleOptionAttributeName));

    private static final DeviceMessageCategory category = DeviceMessageCategories.PPP_PARAMETERS;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private PPPConfigurationDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
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

    @Override
    public int getMessageId() {
        return id;
    }
}