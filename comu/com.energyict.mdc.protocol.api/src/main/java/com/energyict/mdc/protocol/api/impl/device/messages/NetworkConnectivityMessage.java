/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.TimeOfDayFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

enum NetworkConnectivityMessage implements DeviceMessageSpecEnum {

    ACTIVATE_WAKEUP_MECHANISM(DeviceMessageId.NETWORK_CONNECTIVITY_ACTIVATE_WAKEUP_MECHANISM, "Activate wakeup mechanism"),
    DEACTIVATE_SMS_WAKEUP(DeviceMessageId.NETWORK_CONNECTIVITY_DEACTIVATE_SMS_WAKEUP, "Deactivate SMS wakeup"),
    CHANGE_GPRS_USER_CREDENTIALS(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_USER_CREDENTIALS, "Change the GPRS user credentials") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.usernameAttributeName, DeviceMessageAttributes.usernameAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .passwordSpec()
                            .named(DeviceMessageConstants.passwordAttributeName, DeviceMessageAttributes.passwordAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CHANGE_GPRS_APN_CREDENTIALS(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_APN_CREDENTIALS, "Change the GPRS APN credentials") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.apnAttributeName, propertySpecService, thesaurus));
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.usernameAttributeName, DeviceMessageAttributes.usernameAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .passwordSpec()
                            .named(DeviceMessageConstants.passwordAttributeName, DeviceMessageAttributes.passwordAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    // will be a semicolon separated string (maybe in the future this will be a StringListAspectEditor ...
    ADD_PHONENUMBERS_TO_WHITE_LIST(DeviceMessageId.NETWORK_CONNECTIVITY_ADD_PHONENUMBERS_TO_WHITE_LIST, "Add phone numbers to white list") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.whiteListPhoneNumbersAttributeName, propertySpecService, thesaurus));
        }
    },
    ADD_MANAGED_PHONENUMBERS_TO_WHITE_LIST(DeviceMessageId.NETWORK_CONNECTIVITY_ADD_MANAGED_PHONENUMBERS_TO_WHITE_LIST, "Add managed phone numbers to white list") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.managedWhiteListPhoneNumbersAttributeName, propertySpecService, thesaurus));
        }
    },
    CHANGE_SMS_CENTER_NUMBER(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_SMS_CENTER_NUMBER, "Change the SMS center phone number") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.smsCenterPhoneNumberAttributeName, propertySpecService, thesaurus));
        }
    },
    CHANGE_DEVICE_PHONENUMBER(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_DEVICE_PHONENUMBER, "Change the device phone number") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.devicePhoneNumberAttributeName, propertySpecService, thesaurus));
        }
    },
    CHANGE_GPRS_IP_ADDRESS_AND_PORT(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_IP_ADDRESS_AND_PORT, "Change the IP address and port number") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.ipAddressAttributeName, propertySpecService, thesaurus));
            propertySpecs.add(this.bigDecimalProperty(DeviceMessageAttributes.portNumberAttributeName, propertySpecService, thesaurus));
        }
    },
    CHANGE_WAKEUP_FREQUENCY(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_WAKEUP_FREQUENCY, "Change the wakeup frequency") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.wakeupPeriodAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
                            .markExhaustive()
                            .finish());
        }
    },
    CHANGE_INACTIVITY_TIMEOUT(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_INACTIVITY_TIMEOUT, "Change the inactivity timeout") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.bigDecimalProperty(DeviceMessageAttributes.inactivityTimeoutAttributeName, propertySpecService, thesaurus));
        }
    },

    //EIWeb messages
    SetProxyServer(DeviceMessageId.NETWORK_CONNECTIVITY_SET_PROXY_SERVER, "Set proxy server") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.SetProxyServerAttributeName, propertySpecService, thesaurus));
        }
    },
    SetProxyUsername(DeviceMessageId.NETWORK_CONNECTIVITY_SET_PROXY_USERNAME, "Set proxy user name") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.SetProxyUsernameAttributeName, propertySpecService, thesaurus));
        }
    },
    SetProxyPassword(DeviceMessageId.NETWORK_CONNECTIVITY_SET_PROXY_PASSWORD, "Set proxy password") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.SetProxyPasswordAttributeName, propertySpecService, thesaurus));
        }
    },
    SetDHCP(DeviceMessageId.NETWORK_CONNECTIVITY_SET_DHCP, "Set DHCP") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.SetDHCPAttributeName, propertySpecService, thesaurus));
        }
    },
    SetDHCPTimeout(DeviceMessageId.NETWORK_CONNECTIVITY_SET_DHCP_TIMEOUT, "Set DHCP timeout") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.SetDHCPTimeoutAttributeName, propertySpecService, thesaurus));
        }
    },
    SetIPAddress(DeviceMessageId.NETWORK_CONNECTIVITY_SET_IP_ADDRESS, "Set IP address") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.SetIPAddressAttributeName, propertySpecService, thesaurus));
        }
    },
    SetSubnetMask(DeviceMessageId.NETWORK_CONNECTIVITY_SET_SUBNET_MASK, "Set subnet mask") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.SetSubnetMaskAttributeName, propertySpecService, thesaurus));
        }
    },
    SetGateway(DeviceMessageId.NETWORK_CONNECTIVITY_SET_GATEWAY, "Set gateway") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.SetGatewayAttributeName, propertySpecService, thesaurus));
        }
    },
    SetNameServer(DeviceMessageId.NETWORK_CONNECTIVITY_SET_NAME_SERVER, "Set name server") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.SetNameServerAttributeName, propertySpecService, thesaurus));
        }
    },
    SetHttpPort(DeviceMessageId.NETWORK_CONNECTIVITY_SET_HTTP_PORT, "Set HTTP port") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.SetHttpPortAttributeName, propertySpecService, thesaurus));
        }
    },
    ConfigureKeepAliveSettings(DeviceMessageId.NETWORK_CONNECTIVITY_CONFIGURE_KEEP_ALIVE_SETTINGS, "Configure keepalive settings") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(DeviceMessageAttributes.NetworkConnectivityIPAddressAttributeName, propertySpecService, thesaurus));
            propertySpecs.add(this.bigDecimalProperty(DeviceMessageAttributes.NetworkConnectivityIntervalAttributeName, propertySpecService, thesaurus));
        }
    }, PreferGPRSUpstreamCommunication(DeviceMessageId.NETWORK_CONNECTIVITY_PREFER_GPRS_UPSTREAM_COMMUNICATION, "Prefer GPRS upstream communication"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.booleanProperty(DeviceMessageAttributes.preferGPRSUpstreamCommunication, propertySpecService, thesaurus));
        }
    }, EnableModemWatchdog(DeviceMessageId.NETWORK_CONNECTIVITY_ENABLE_MODEM_WATCHDOG, "Enable the modem watchdog"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.booleanProperty(DeviceMessageAttributes.enableModemWatchdog, propertySpecService, thesaurus));
        }
    }, SetModemWatchdogParameters(DeviceMessageId.NETWORK_CONNECTIVITY_SET_MODEM_WATCHDOG_PARAMETERS, "Set modem watchdog parameters"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(DeviceMessageAttributes.modemWatchdogInterval, DeviceMessageAttributes.PPPDaemonResetThreshold, DeviceMessageAttributes.modemResetThreshold, DeviceMessageAttributes.systemRebootThreshold)
                .map(name -> propertySpecService
                        .bigDecimalSpec()
                        .named(name)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .setDefaultValue(BigDecimal.ZERO).finish())
                .forEach(propertySpecs::add);
        }
    },
    CLEAR_WHITE_LIST(DeviceMessageId.NETWORK_CONNECTIVITY_CLEAR_WHITE_LIST, "Clear the white list"),
    ENABLE_WHITE_LIST(DeviceMessageId.NETWORK_CONNECTIVITY_ENABLE_WHITE_LIST, "Enable the white list"),
    DISABLE_WHITE_LIST(DeviceMessageId.NETWORK_CONNECTIVITY_DISABLE_WHITE_LIST, "Disable the white list"),
    ENABLE_OPERATING_WINDOW(DeviceMessageId.NETWORK_CONNECTIVITY_ENABLE_OPERATING_WINDOW, "Enable operating mode"),
    DISABLE_OPERATING_WINDOW(DeviceMessageId.NETWORK_CONNECTIVITY_DISABLE_OPERATING_WINDOW, "Disable operating mode"),
    SET_OPERATING_WINDOW_START_TIME(DeviceMessageId.NETWORK_CONNECTIVITY_SET_OPERATING_WINDOW_START_TIME, "Set operating window start time") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new TimeOfDayFactory())
                            .named(DeviceMessageConstants.startTime, DeviceMessageAttributes.startTime)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SET_OPERATING_WINDOW_END_TIME(DeviceMessageId.NETWORK_CONNECTIVITY_SET_OPERATING_WINDOW_END_TIME, "Set operating window end time") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new TimeOfDayFactory())
                            .named(DeviceMessageConstants.endTime, DeviceMessageAttributes.endTime)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    RUN_METER_DISCOVERY(DeviceMessageId.NETWORK_CONNECTIVITY_RUN_METER_DISCOVERY, "Run meter discovery"),
    RUN_ALARM_METER_DISCOVERY(DeviceMessageId.NETWORK_CONNECTIVITY_RUN_ALARM_METER_DISCOVERY, "Run alarm meter discovery"),
    RUN_REPEATER_CALL(DeviceMessageId.NETWORK_CONNECTIVITY_RUN_REPEATER_CALL, "Run repeater call"),
    SET_NETWORK_MANAGEMENT_PARAMETERS(DeviceMessageId.NETWORK_CONNECTIVITY_SET_NETWORK_MANAGEMENT_PARAMETERS, "Set the network management parameters") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(DeviceMessageAttributes.discoverInterval, DeviceMessageAttributes.discoverInterval, DeviceMessageAttributes.repeaterCallInterval, DeviceMessageAttributes.repeaterCallThreshold, DeviceMessageAttributes.repeaterCallTimeslots)
                .map(name -> propertySpecService
                        .bigDecimalSpec()
                        .named(name)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .setDefaultValue(BigDecimal.ZERO)
                        .finish())
                .forEach(propertySpecs::add);
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    NetworkConnectivityMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return NetworkConnectivityMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultTranslation;
    }

    @Override
    public DeviceMessageId getId() {
        return this.id;
    }

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
        return propertySpecs;
    }

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        // Default behavior is not to add anything
    };

    protected PropertySpec stringProperty(DeviceMessageAttributes name, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        return propertySpecService.stringSpec().named(name).fromThesaurus(thesaurus).markRequired().finish();
    }

    protected PropertySpec passwordProperty(DeviceMessageAttributes name, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        return propertySpecService.passwordSpec().named(name).fromThesaurus(thesaurus).markRequired().finish();
    }

    protected PropertySpec bigDecimalProperty(DeviceMessageAttributes name, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        return propertySpecService.bigDecimalSpec().named(name).fromThesaurus(thesaurus).markRequired().finish();
    }

    protected PropertySpec booleanProperty(DeviceMessageAttributes name, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        return propertySpecService.booleanSpec().named(name).fromThesaurus(thesaurus).markRequired().finish();
    }

}