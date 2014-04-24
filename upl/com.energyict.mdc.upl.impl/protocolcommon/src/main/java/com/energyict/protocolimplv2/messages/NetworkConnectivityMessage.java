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

    ACTIVATE_WAKEUP_MECHANISM(0),
    DEACTIVATE_SMS_WAKEUP(1),
    CHANGE_GPRS_USER_CREDENTIALS(2,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.usernameAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.passwordAttributeName)),
    CHANGE_GPRS_APN_CREDENTIALS(3,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.apnAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.usernameAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.passwordAttributeName)),
    // will be a semicolon separated string (maybe in the future this will be a StringListAspectEditor ...
    ADD_PHONENUMBERS_TO_WHITE_LIST(4, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.whiteListPhoneNumbersAttributeName)),
    ADD_MANAGED_PHONENUMBERS_TO_WHITE_LIST(5, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.managedWhiteListPhoneNumbersAttributeName)),
    CHANGE_SMS_CENTER_NUMBER(6, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.smsCenterPhoneNumberAttributeName)),
    CHANGE_DEVICE_PHONENUMBER(7, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.devicePhoneNumberAttributeName)),
    CHANGE_GPRS_IP_ADDRESS_AND_PORT(8,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.ipAddressAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.portNumberAttributeName)),
    CHANGE_WAKEUP_FREQUENCY(9,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.wakeupPeriodAttributeName, "1", "2", "4", "6", "8", "12")),
    CHANGE_INACTIVITY_TIMEOUT(10, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.inactivityTimeoutAttributeName)),

    //EIWeb messages
    SetProxyServer(11, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetProxyServerAttributeName)),
    SetProxyUsername(12, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetProxyUsernameAttributeName)),
    SetProxyPassword(13, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetProxyPasswordAttributeName)),
    SetDHCP(14, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDHCPAttributeName)),
    SetDHCPTimeout(15, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDHCPTimeoutAttributeName)),
    SetIPAddress(16, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetIPAddressAttributeName)),
    SetSubnetMask(17, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSubnetMaskAttributeName)),
    SetGateway(18, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetGatewayAttributeName)),
    SetNameServer(19, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetNameServerAttributeName)),
    SetHttpPort(20, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetHttpPortAttributeName)),
    ConfigureKeepAliveSettings(21,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.NetworkConnectivityIPAddressAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.NetworkConnectivityIntervalAttributeName)
    );

    private static final DeviceMessageCategory networkAndConnectivityCategory = DeviceMessageCategories.NETWORK_AND_CONNECTIVITY;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private NetworkConnectivityMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
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

    @Override
    public int getMessageId() {
        return id;
    }
}
