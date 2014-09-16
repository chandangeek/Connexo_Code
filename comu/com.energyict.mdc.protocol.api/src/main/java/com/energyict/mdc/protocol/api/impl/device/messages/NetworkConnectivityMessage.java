package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.PasswordFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.NetworkConnectivityIPAddressAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.NetworkConnectivityIntervalAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetDHCPAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetDHCPTimeoutAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetGatewayAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetHttpPortAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetIPAddressAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetNameServerAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetProxyPasswordAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetProxyServerAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetProxyUsernameAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetSubnetMaskAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.apnAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.devicePhoneNumberAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.inactivityTimeoutAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.ipAddressAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.managedWhiteListPhoneNumbersAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.passwordAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.portNumberAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.smsCenterPhoneNumberAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.usernameAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.wakeupPeriodAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.whiteListPhoneNumbersAttributeName;

/**
 * Provides a summary of all messages related to <i>Network</i> and <i>Connectivity</i>
 * <p/>
 * Copyrights EnergyICT
 * Date: 2/04/13
 * Time: 10:11
 */
public enum NetworkConnectivityMessage implements DeviceMessageSpecEnum {

    ACTIVATE_WAKEUP_MECHANISM(DeviceMessageId.NETWORK_CONNECTIVITY_ACTIVATE_WAKEUP_MECHANISM, "Activate wakeup mechanism"),
    DEACTIVATE_SMS_WAKEUP(DeviceMessageId.NETWORK_CONNECTIVITY_DEACTIVATE_SMS_WAKEUP, "Deactivate SMS wakeup"),
    CHANGE_GPRS_USER_CREDENTIALS(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_USER_CREDENTIALS, "Change the GPRS user credentials") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(usernameAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(passwordAttributeName, true, new PasswordFactory()));
        }
    },
    CHANGE_GPRS_APN_CREDENTIALS(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_APN_CREDENTIALS, "Change the GPRS APN credentials") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(apnAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(usernameAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(passwordAttributeName, true, new PasswordFactory()));
        }
    },
    // will be a semicolon separated string (maybe in the future this will be a StringListAspectEditor ...
    ADD_PHONENUMBERS_TO_WHITE_LIST(DeviceMessageId.NETWORK_CONNECTIVITY_ADD_PHONENUMBERS_TO_WHITE_LIST, "Add phonenumbers to white list") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(whiteListPhoneNumbersAttributeName, true, new StringFactory()));
        }
    },
    ADD_MANAGED_PHONENUMBERS_TO_WHITE_LIST(DeviceMessageId.NETWORK_CONNECTIVITY_ADD_MANAGED_PHONENUMBERS_TO_WHITE_LIST, "Add managed phonenumbers to white list") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(managedWhiteListPhoneNumbersAttributeName, true, new StringFactory()));
        }
    },
    CHANGE_SMS_CENTER_NUMBER(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_SMS_CENTER_NUMBER, "Change the SMS center phonenumber") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(smsCenterPhoneNumberAttributeName, true, new StringFactory()));
        }
    },
    CHANGE_DEVICE_PHONENUMBER(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_DEVICE_PHONENUMBER, "Change the device phonenumber") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(devicePhoneNumberAttributeName, true, new StringFactory()));
        }
    },
    CHANGE_GPRS_IP_ADDRESS_AND_PORT(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_IP_ADDRESS_AND_PORT, "Change the IP address and port number") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(ipAddressAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(portNumberAttributeName, true, new BigDecimalFactory()));
        }
    },
    CHANGE_WAKEUP_FREQUENCY(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_WAKEUP_FREQUENCY, "Change the wakeup frequency") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(wakeupPeriodAttributeName, true, "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
        }
    },
    CHANGE_INACTIVITY_TIMEOUT(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_INACTIVITY_TIMEOUT, "Change the inactivity timeout") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(inactivityTimeoutAttributeName, true, new BigDecimalFactory()));
        }
    },

    //EIWeb messages
    SetProxyServer(DeviceMessageId.NETWORK_CONNECTIVITY_SET_PROXY_SERVER, "Set proxy server") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetProxyServerAttributeName, true, new StringFactory()));
        }
    },
    SetProxyUsername(DeviceMessageId.NETWORK_CONNECTIVITY_SET_PROXY_USERNAME, "Set proxy user name") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetProxyUsernameAttributeName, true, new StringFactory()));
        }
    },
    SetProxyPassword(DeviceMessageId.NETWORK_CONNECTIVITY_SET_PROXY_PASSWORD, "Set proxy password") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetProxyPasswordAttributeName, true, new StringFactory()));
        }
    },
    SetDHCP(DeviceMessageId.NETWORK_CONNECTIVITY_SET_DHCP, "Set DHCP") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetDHCPAttributeName, true, new StringFactory()));
        }
    },
    SetDHCPTimeout(DeviceMessageId.NETWORK_CONNECTIVITY_SET_DHCP_TIMEOUT, "Set DHCP timeout") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetDHCPTimeoutAttributeName, true, new StringFactory()));
        }
    },
    SetIPAddress(DeviceMessageId.NETWORK_CONNECTIVITY_SET_IP_ADDRESS, "Set IP address") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetIPAddressAttributeName, true, new StringFactory()));
        }
    },
    SetSubnetMask(DeviceMessageId.NETWORK_CONNECTIVITY_SET_SUBNET_MASK, "Set subnet mask") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetSubnetMaskAttributeName, true, new StringFactory()));
        }
    },
    SetGateway(DeviceMessageId.NETWORK_CONNECTIVITY_SET_GATEWAY, "Set gateway") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetGatewayAttributeName, true, new StringFactory()));
        }
    },
    SetNameServer(DeviceMessageId.NETWORK_CONNECTIVITY_SET_NAME_SERVER, "Set name server") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetNameServerAttributeName, true, new StringFactory()));
        }
    },
    SetHttpPort(DeviceMessageId.NETWORK_CONNECTIVITY_SET_HTTP_PORT, "Set HTTP port") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetHttpPortAttributeName, true, new StringFactory()));
        }
    },
    ConfigureKeepAliveSettings(DeviceMessageId.NETWORK_CONNECTIVITY_CONFIGURE_KEEP_ALIVE_SETTINGS, "Configure keepalive settings") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(NetworkConnectivityIPAddressAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(NetworkConnectivityIntervalAttributeName, true, new BigDecimalFactory()));
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    NetworkConnectivityMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getNameResourceKey() {
        return NetworkConnectivityMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String defaultTranslation() {
        return this.defaultTranslation;
    }

    @Override
    public DeviceMessageId getId() {
        return this.id;
    }

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService);
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}