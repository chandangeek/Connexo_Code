package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;

import java.util.Arrays;
import java.util.List;

/**
 * Provides a summary of all messages related to general Device Actions
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 11:59
 */
public enum FirewallConfigurationMessage implements DeviceMessageSpec {

    ActivateFirewall(0),
    DeactivateFirewall(1),
    ConfigureFWWAN(2,
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.EnableDLMS),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.EnableHTTP),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.EnableSSH)
    ),
    ConfigureFWLAN(3,
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.EnableDLMS),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.EnableHTTP),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.EnableSSH)
    ),
    ConfigureFWGPRS(4,
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.EnableDLMS),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.EnableHTTP),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.EnableSSH)
    ),
    SetFWDefaultState(5, PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.defaultEnabled)),;

    private final int id;

    public int getMessageId() {
        return id;
    }

    private static final DeviceMessageCategory category = DeviceMessageCategories.FIREWALL_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private FirewallConfigurationMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    private static String translate(final String key) {
        return UserEnvironment.getDefault().getTranslation(key);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return translate(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return FirewallConfigurationMessage.class.getSimpleName() + "." + this.toString();
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
        return new EnumBasedDeviceMessageSpecPrimaryKey(this, name());
    }
}
