package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;

import com.elster.jupiter.properties.PropertySpec;

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

    ACTIVATE_WAKEUP_MECHANISM,
    DEACTIVATE_SMS_WAKEUP,
    CHANGE_GPRS_USER_CREDENTIALS(
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.usernameAttributeName),
            RequiredPropertySpecFactory.newInstance().passwordPropertySpec(DeviceMessageConstants.passwordAttributeName)),
    CHANGE_GPRS_APN_CREDENTIALS(
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.apnAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.usernameAttributeName),
            RequiredPropertySpecFactory.newInstance().passwordPropertySpec(DeviceMessageConstants.passwordAttributeName)),
    // will be a semicolon separated string (maybe in the future this will be a StringListAspectEditor ...
    ADD_PHONENUMBERS_TO_WHITE_LIST(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.whiteListPhoneNumbersAttributeName)),
    ADD_MANAGED_PHONENUMBERS_TO_WHITE_LIST(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.managedWhiteListPhoneNumbersAttributeName)),
    CHANGE_SMS_CENTER_NUMBER(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.smsCenterPhoneNumberAttributeName)),
    CHANGE_DEVICE_PHONENUMBER(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.devicePhoneNumberAttributeName)),
    CHANGE_GPRS_IP_ADDRESS_AND_PORT(
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.ipAddressAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.portNumberAttributeName)),
    CHANGE_WAKEUP_FREQUENCY(
            RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValues(DeviceMessageConstants.wakeupPeriodAttributeName, "0", "1", "2", "3", "4", "5", "6", "7", "8", "9")),
    CHANGE_INACTIVITY_TIMEOUT(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.inactivityTimeoutAttributeName)),

    //EIWeb messages
    SetProxyServer(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetProxyServerAttributeName)),
    SetProxyUsername(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetProxyUsernameAttributeName)),
    SetProxyPassword(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetProxyPasswordAttributeName)),
    SetDHCP(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetDHCPAttributeName)),
    SetDHCPTimeout(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetDHCPTimeoutAttributeName)),
    SetIPAddress(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetIPAddressAttributeName)),
    SetSubnetMask(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetSubnetMaskAttributeName)),
    SetGateway(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetGatewayAttributeName)),
    SetNameServer(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetNameServerAttributeName)),
    SetHttpPort(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetHttpPortAttributeName)),
    ConfigureKeepAliveSettings(
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.NetworkConnectivityIPAddressAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.NetworkConnectivityIntervalAttributeName)
    );

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
        return this.getNameResourceKey();
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return NetworkConnectivityMessage.class.getSimpleName() + "." + this.toString();
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
