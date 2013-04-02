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
 * Provides a summary of all messages related to <i>Network</i> and <i>Connectivity</i>
 * <p/>
 * Copyrights EnergyICT
 * Date: 2/04/13
 * Time: 10:11
 */
public enum NetworkConnectivityMessage implements DeviceMessageSpec {

    ACTIVATE_SMS_WAKEUP,
    DEACTIVATE_SMS_WAKEUP,
    CHANGE_GPRS_USER_CREDENTIALS(
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.username),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.password)),
    CHANGE_GPRS_APN_CREDENTIALS(
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.apn),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.username),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.password));

    private static final DeviceMessageCategory networkAndConnectivityCategory = DeviceMessageCategories.NETWORK_AND_CONNECTIVITY;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private NetworkConnectivityMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return networkAndConnectivityCategory;
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
        return SecurityMessage.class.getSimpleName() + "." + this.toString();
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
