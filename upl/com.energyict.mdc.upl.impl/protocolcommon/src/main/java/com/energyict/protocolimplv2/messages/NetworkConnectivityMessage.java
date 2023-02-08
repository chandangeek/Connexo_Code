package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HexString;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.KeyAccessorType;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Provides a summary of all messages related to <i>Network</i> and <i>Connectivity</i>.
 * <p/>
 * Copyrights EnergyICT
 * Date: 2/04/13
 * Time: 10:11
 */
public enum NetworkConnectivityMessage implements DeviceMessageSpecSupplier {

    ACTIVATE_WAKEUP_MECHANISM(4001, "Activate wakeup mechanism") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    DEACTIVATE_SMS_WAKEUP(4002, "Deactivate sms wakeup") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    CHANGE_GPRS_USER_CREDENTIALS(4003, "Change the GPRS user credentials") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.usernameAttributeName, DeviceMessageConstants.usernameAttributeDefaultTranslation),
                    this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.passwordAttributeName, DeviceMessageConstants.passwordAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_GPRS_APN_CREDENTIALS(4004, "Change the GPRS APN credentials") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.apnAttributeName, DeviceMessageConstants.apnAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.usernameAttributeName, DeviceMessageConstants.usernameAttributeDefaultTranslation),
                    this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.passwordAttributeName, DeviceMessageConstants.passwordAttributeDefaultTranslation)
            );
        }
    },
    // will be a semicolon separated string (maybe in the future this will be a StringListAspectEditor ...
    ADD_PHONENUMBERS_TO_WHITE_LIST(4005, "Add phonenumbers to white list") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.whiteListPhoneNumbersAttributeName, DeviceMessageConstants.whiteListPhoneNumbersAttributeDefaultTranslation));
        }
    },
    ADD_MANAGED_PHONENUMBERS_TO_WHITE_LIST(4006, "Add managed phonenumbers to white list") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.managedWhiteListPhoneNumbersAttributeName, DeviceMessageConstants.managedWhiteListPhoneNumbersAttributeDefaultTranslation));
        }
    },
    CHANGE_SMS_CENTER_NUMBER(4007, "Change the sms center phone number") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.smsCenterPhoneNumberAttributeName, DeviceMessageConstants.smsCenterPhoneNumberAttributeDefaultTranslation));
        }
    },
    CHANGE_DEVICE_PHONENUMBER(4008, "Change the device phone number") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.devicePhoneNumberAttributeName, DeviceMessageConstants.devicePhoneNumberAttributeDefaultTranslation));
        }
    },
    CHANGE_GPRS_IP_ADDRESS_AND_PORT(4009, "Change the IP address and port number") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.ipAddressAttributeName, DeviceMessageConstants.ipAddressAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.portNumberAttributeName, DeviceMessageConstants.portNumberAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_WAKEUP_FREQUENCY(4010, "Change the wakeup frequency") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(
                    this.stringSpec(
                            service,
                            DeviceMessageConstants.wakeupPeriodAttributeName, DeviceMessageConstants.wakeupPeriodAttributeDefaultTranslation,
                            "1", "2", "4", "6", "8", "12"));
        }
    },
    CHANGE_INACTIVITY_TIMEOUT(4011, "Change inactivity timeout") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.inactivityTimeoutAttributeName, DeviceMessageConstants.inactivityTimeoutAttributeDefaultTranslation));
        }
    },

    //EIWeb messages
    SetProxyServer(4012, "Set proxy server") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetProxyServerAttributeName, DeviceMessageConstants.SetProxyServerAttributeDefaultTranslation));
        }
    },
    SetProxyUsername(4013, "Set proxy user name") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetProxyUsernameAttributeName, DeviceMessageConstants.SetProxyUsernameAttributeDefaultTranslation));
        }
    },
    SetProxyPassword(4014, "Set proxy password") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetProxyPasswordAttributeName, DeviceMessageConstants.SetProxyPasswordAttributeDefaultTranslation));
        }
    },
    SetDHCP(4015, "Set DHCP") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetDHCPAttributeName, DeviceMessageConstants.SetDHCPAttributeDefaultTranslation));
        }
    },
    SetDHCPTimeout(4016, "Set DHCP timeout") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetDHCPTimeoutAttributeName, DeviceMessageConstants.SetDHCPTimeoutAttributeDefaultTranslation));
        }
    },
    SetIPAddress(4017, "Set IP address") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetIPAddressAttributeName, DeviceMessageConstants.SetIPAddressAttributeDefaultTranslation));
        }
    },
    SetSubnetMask(4018, "Set subnet mask") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetSubnetMaskAttributeName, DeviceMessageConstants.SetSubnetMaskAttributeDefaultTranslation));
        }
    },
    SetGateway(4019, "Set gateway") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetGatewayAttributeName, DeviceMessageConstants.SetGatewayAttributeDefaultTranslation));
        }
    },
    SetNameServer(4020, "Set name server") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetNameServerAttributeName, DeviceMessageConstants.SetNameServerAttributeDefaultTranslation));
        }
    },
    SetHttpPort(4021, "Set HTTP port") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetHttpPortAttributeName, DeviceMessageConstants.SetHttpPortAttributeDefaultTranslation));
        }
    },
    ConfigureKeepAliveSettings(4022, "Configure keepalive settings") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.NetworkConnectivityIPAddressAttributeName, DeviceMessageConstants.NetworkConnectivityIPAddressAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.NetworkConnectivityIntervalAttributeName, DeviceMessageConstants.NetworkConnectivityIntervalAttributeDefaultTranslation)
            );
        }
    },
    PreferGPRSUpstreamCommunication(4023, "Prefer GPRS upstream communication") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.preferGPRSUpstreamCommunication, DeviceMessageConstants.preferGPRSUpstreamCommunicationDefaultTranslation));
        }
    },
    EnableModemWatchdog(4024, "Enable modem watchdog") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.enableModemWatchdog, DeviceMessageConstants.enableModemWatchdogDefaultTranslation));
        }
    },
    SetModemWatchdogParameters(4025, "Write modem watchdog parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.modemWatchdogInterval, DeviceMessageConstants.modemWatchdogIntervalDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.PPPDaemonResetThreshold, DeviceMessageConstants.PPPDaemonResetThresholdDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.modemResetThreshold, DeviceMessageConstants.modemResetThresholdDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.systemRebootThreshold, DeviceMessageConstants.systemRebootThresholdDefaultTranslation)
            );
        }
    },
    ClearWhiteList(4026, "Clear white list") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    EnableWhiteList(4027, "Enable usage of white list") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    DisableWhiteList(4028, "Disable usage of white list") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    EnableOperatingWindow(4029, "Enable usage of operating window") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    DisableOperatingWindow(4030, "Disable usage of operating window") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    SetOperatingWindowStartTime(4031, "Set operating window start time") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.timeSpec(service, DeviceMessageConstants.startTime, DeviceMessageConstants.startTimeDefaultTranslation));
        }
    },
    SetOperatingWindowEndTime(4032, "Set operating window end time") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.timeSpec(service, DeviceMessageConstants.endTime, DeviceMessageConstants.endTimeDefaultTranslation));
        }
    },
    RunMeterDiscovery(4033, "Run meter discovery") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    RunAlarmMeterDiscovery(4034, "Run alarm meter discovery") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    RunRepeaterCall(4035, "Run repeater call") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    SetNetworkManagementParameters(4036, "Set network management parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.discoverDuration, DeviceMessageConstants.discoverDurationDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.discoverInterval, DeviceMessageConstants.discoverIntervalDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.repeaterCallInterval, DeviceMessageConstants.repeaterCallIntervalDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.repeaterCallThreshold, DeviceMessageConstants.repeaterCallThresholdDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.repeaterCallTimeslots, DeviceMessageConstants.repeaterCallTimeslotsDefaultTranslation)
            );
        }
    },
    SetUseDHCPFlag(4037, "Use DHCP") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.SetDHCPAttributeName, DeviceMessageConstants.SetDHCPAttributeDefaultTranslation));
        }
    },
    SetPrimaryDNSAddress(4038, "Set primary DNS address") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.primaryDNSAddressAttributeName, DeviceMessageConstants.primaryDNSAddressAttributeDefaultTranslation));
        }
    },
    SetSecondaryDNSAddress(4039, "Set secondary DNS address") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.secondaryDNSAddressAttributeName, DeviceMessageConstants.secondaryDNSAddressAttributeDefaultTranslation));
        }
    },
    SetAutoConnectMode(4040, "Set GPRS connect mode") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.gprsModeAttributeName, DeviceMessageConstants.gprsModeAttributeDefaultTranslation));
        }
    },
    ChangeSessionTimeout(4041, "Change session timeout") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.sessionTimeoutAttributeName, DeviceMessageConstants.sessionTimeoutAttributeDefaultTranslation));
        }
    },
    SetCyclicMode(4042, "Set cyclic mode") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.bigDecimalSpecBuilder(service, DeviceMessageConstants.day, DeviceMessageConstants.dayDefaultTranslation).setDefaultValue(BigDecimal.ZERO).finish(),
                    this.bigDecimalSpec(service, DeviceMessageConstants.hour, DeviceMessageConstants.hourDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.minute, DeviceMessageConstants.minuteDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.second, DeviceMessageConstants.secondDefaultTranslation)
            );
        }
    },
    SetPreferredDateMode(4043, "Set preferred date mode") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.bigDecimalSpecBuilder(service, DeviceMessageConstants.day, DeviceMessageConstants.dayDefaultTranslation).setDefaultValue(BigDecimal.ZERO).finish(),
                    this.bigDecimalSpec(service, DeviceMessageConstants.hour, DeviceMessageConstants.hourDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.minute, DeviceMessageConstants.minuteDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.second, DeviceMessageConstants.secondDefaultTranslation)
            );
        }
    },
    SetWANConfiguration(4044, "Set WAN configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.Destination1IPAddressAttributeName, DeviceMessageConstants.Destination1IPAddressAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.Destination2IPAddressAttributeName, DeviceMessageConstants.Destination2IPAddressAttributeDefaultTranslation)
            );
        }
    },
    WakeupParameters(4045, "Set wakeup parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.wakeupPeriodAttributeName, DeviceMessageConstants.wakeupPeriodAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.inactivityTimeoutAttributeName, DeviceMessageConstants.inactivityTimeoutAttributeDefaultTranslation)
            );
        }
    },
    PreferredNetworkOperatorList(4046, "Set preferred network operator") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return IntStream
                    .range(1, 11)
                    .mapToObj(number -> this.networkOperatorSpec(service, number))
                    .collect(Collectors.toList());
        }
    },
    ConfigureAutoAnswer(4047, "Configure auto answer") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.windowAttributeName, DeviceMessageConstants.windowAttributeDefaultTranslation, getPossibleValues(1, 6)),
                    this.stringSpec(service, DeviceMessageConstants.autoAnswerStartTime, DeviceMessageConstants.autoAnswerStartTimeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.autoAnswerEndTime, DeviceMessageConstants.autoAnswerEndTimeDefaultTranslation)
            );
        }
    },
    DisableAutoAnswer(4048, "Disable auto answer") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.windowAttributeName, DeviceMessageConstants.windowAttributeDefaultTranslation, getPossibleValues(1, 6)));
        }
    },
    ConfigureAutoConnect(4049, "Configure auto connect") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.windowAttributeName, DeviceMessageConstants.windowAttributeDefaultTranslation, getPossibleValues(1, 2)),
                    this.stringSpec(service, DeviceMessageConstants.autoConnectMode, DeviceMessageConstants.autoConnectModeDefaultTranslation, AutoConnectMode.SpecifiedTime.description, AutoConnectMode.InsideWindow.description),
                    this.stringSpec(service, DeviceMessageConstants.autoConnectStartTime, DeviceMessageConstants.autoConnectStartTimeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.autoConnectEndTime, DeviceMessageConstants.autoConnectEndTimeDefaultTranslation, "N/A"),
                    this.stringSpec(service, DeviceMessageConstants.autoConnectDestination1, DeviceMessageConstants.autoConnectDestination1DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.autoConnectDestination2, DeviceMessageConstants.autoConnectDestination2DefaultTranslation, "N/A")
            );
        }
    },
    DisableAutoConnect(4050, "Disable auto connect") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.windowAttributeName, DeviceMessageConstants.windowAttributeDefaultTranslation, getPossibleValues(1, 2)));
        }
    },
    SetModemWatchdogParameters2(4051, "Write modem watchdog parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.durationSpec(service, DeviceMessageConstants.modemWatchdogInterval, DeviceMessageConstants.modemWatchdogIntervalDefaultTranslation),
                    this.durationSpec(service, DeviceMessageConstants.modemWatchdogInitialDelay, DeviceMessageConstants.modemWatchdogInitialDelayDefaultTranslation),
                    this.durationSpec(service, DeviceMessageConstants.PPPDaemonResetThreshold, DeviceMessageConstants.PPPDaemonResetThresholdDefaultTranslation),
                    this.durationSpec(service, DeviceMessageConstants.modemResetThreshold, DeviceMessageConstants.modemResetThresholdDefaultTranslation),
                    this.durationSpec(service, DeviceMessageConstants.systemRebootThreshold, DeviceMessageConstants.systemRebootThresholdDefaultTranslation)
            );
        }
    },
    EnableNetworkInterfaces(4052, "Enable network interfaces") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.booleanSpec(service, DeviceMessageConstants.ETHERNET_WAN, DeviceMessageConstants.ETHERNET_WANDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.ETHERNET_LAN, DeviceMessageConstants.ETHERNET_LANDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.WIRELESS_WAN, DeviceMessageConstants.WIRELESS_WANDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.IP6_TUNNEL, DeviceMessageConstants.IP6_TUNNELDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.PLC_NETWORK, DeviceMessageConstants.PLC_NETWORKDefaultTranslation)
            );
        }
    },
    SetHttpsPort(4053, "Set HTTPS Port") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetHttpsPortAttributeName, DeviceMessageConstants.SetHttpsPortAttributeDefaultTranslation));
        }
    },
    EnableNetworkInterfacesForSetupObject(4054, "Enable network interfaces for setup object") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.setupObjectAttributeName, DeviceMessageConstants.setupObjectAttributeDefaultTranslation)
                            .addValues(BeaconSetupObject.getSetupObjectValues())
                            .finish(),
                    this.booleanSpec(service, DeviceMessageConstants.ETHERNET_WAN, DeviceMessageConstants.ETHERNET_LANDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.ETHERNET_LAN, DeviceMessageConstants.ETHERNET_LANDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.WIRELESS_WAN, DeviceMessageConstants.WIRELESS_WANDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.IP6_TUNNEL, DeviceMessageConstants.IP6_TUNNELDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.PLC_NETWORK, DeviceMessageConstants.PLC_NETWORKDefaultTranslation)
            );
        }
    },
    ADD_ROUTING_ENTRY(4055, "Add a new routing entry to the routing setup") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.routingEntryType, DeviceMessageConstants.routingEntryTypeDefaultTranslation)
                            .addValues(RoutingEntryType.getDescriptionValues())
                            .finish(),
                    this.bigDecimalSpec(service, DeviceMessageConstants.routingEntryId, DeviceMessageConstants.routingEntryIdDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.routingDestination, DeviceMessageConstants.routingDestinationDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.routingDestinationLength, DeviceMessageConstants.routingDestinationLengthDefaultTranslation, new BigDecimal(64)),
                    this.booleanSpec(service, DeviceMessageConstants.compressionContextMulticast, DeviceMessageConstants.compressionContextMulticastDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.compressionContextAllowed, DeviceMessageConstants.compressionContextAllowedDefaultTranslation, true)
            );
        }
    },
    REMOVE_ROUTING_ENTRY(4056, "Remove routing entry") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.routingEntryId, DeviceMessageConstants.routingEntryIdDefaultTranslation)
            );
        }
    },
    RESET_ROUTER(4057, "Reset router") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },
    SET_VPN_ENABLED_OR_DISABLED(4058, "Set VPN enabled or disabled") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.booleanSpec(service, DeviceMessageConstants.vpnEnabled, DeviceMessageConstants.vpnEnabledDefaultTranslation)
            );
        }
    },
    SET_VPN_TYPE(4059, "Set VPN type") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.vpnType, DeviceMessageConstants.vpnTypeDefaultTranslation)
                            .addValues(VPNType.getDescriptionValues())
                            .finish()
            );
        }
    },
    SET_VPN_GATEWAY_ADDRESS(4060, "Set gateway address") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.vpnGatewayAddress, DeviceMessageConstants.vpnGatewayAddressDefaultTranslation)
            );
        }
    },
    SET_VPN_AUTHENTICATION_TYPE(4061, "Set authentication type") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.vpnAuthenticationType, DeviceMessageConstants.vpnAuthenticationTypeDefaultTranslation)
                            .addValues(VPNAuthenticationType.getDescriptionValues())
                            .finish()
            );
        }
    },
    SET_VPN_LOCAL_IDENTIFIER(4062, "Set local identifier used during IKE SA") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.vpnLocalIdentifier, DeviceMessageConstants.vpnLocalIdentifierDefaultTranslation)
            );
        }
    },
    SET_VPN_REMOTE_IDENTIFIER(4063, "Set remote identifier expected during IKE SA") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.vpnRemoteIdentifier, DeviceMessageConstants.vpnRemoteIdentifierDefaultTranslation)
            );
        }
    },
    SET_VPN_REMOTE_CERTIFICATE(4065, "Set remote certificate expected during IKE") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.vpnRemoteCertificate, DeviceMessageConstants.vpnRemoteCertificateDefaultTranslation)
            );
        }
    },
    SET_VPN_SHARED_SECRET(4066, "Set shared secret used during IKE") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.vpnSharedSecret, DeviceMessageConstants.vpnSharedSecretDefaultTranslation)
            );
        }
    },
    SET_VPN_VIRTUAL_IP_ENABLED_OR_DISABLED(4067, "Set virtual IP, enabled or disabled") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.booleanSpec(service, DeviceMessageConstants.vpnVirtualIPEnabled, DeviceMessageConstants.vpnVirtualIPEnabledDefaultTranslation)
            );
        }
    },
    SET_VPN_IP_COMPRESSION_ENABLED_OR_DISABLED(4068, "Set IP compression, enabled or disabled") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.booleanSpec(service, DeviceMessageConstants.vpnIPCompressionEnabled, DeviceMessageConstants.vpnIPCompressionEnabledDefaultTranslation)
            );
        }
    },
    REFRESH_VPN_CONFIG(4069, "Refresh VPN config") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    },

    ADD_ROUTING_ENTRY_USING_CONFIGURED_IPV6_IN_GENERAL_PROPERTIES(4070, "Add a new routing entry using IPv6AddressAndPrefixLength property") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.booleanSpec(service, DeviceMessageConstants.cleanUpExistingEntry, DeviceMessageConstants.cleanUpExistingEntryDefaultTranslation, true)
            );
        }
    },

    CHANGE_SNMP_AGENT_CONFIGURATION(4071, "Change SNMP agent configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.snmpSystemContact, DeviceMessageConstants.snmpSystemContactDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.snmpSystemLocation, DeviceMessageConstants.snmpSystemLocationDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.snmpLocalEngineId, DeviceMessageConstants.snmpLocalEngineIdDefaultTranslation),
                    this.stringSpecBuilder(service, DeviceMessageConstants.snmpNotificationType, DeviceMessageConstants.snmpNotificationTypeDefaultTranslation)
                            .addValues(SNMPNotificationType.getDescriptionValues())
                            .finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.snmpNotificationUserProfile, DeviceMessageConstants.snmpNotificationUserProfileDefaultTranslation)
                            .addValues(SNMPUserProfileType.getDescriptionValues())
                            .finish(),
                    this.stringSpec(service, DeviceMessageConstants.snmpNotificationHost, DeviceMessageConstants.snmpNotificationHostDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.snmpNotificationPort, DeviceMessageConstants.snmpNotificationPortDefaultTranslation)
            );
        }
    },

    CHANGE_SNMP_AGENT_USER_NAME(4072, "Change SNMP agent user name") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.snmpUserProfile, DeviceMessageConstants.snmpUserProfileDefaultTranslation)
                        .addValues(SNMPUserProfileType.getDescriptionValues())
                        .finish(),
                    this.stringSpec(service, DeviceMessageConstants.snmpNewUserName, DeviceMessageConstants.snmpNewUserNameDefaultTranslation)
            );
        }
    },

    CHANGE_SNMP_AGENT_USER_PASSPHRASES(4073, "Change SNMP agent user passphrases") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.snmpUserProfile, DeviceMessageConstants.snmpUserProfileDefaultTranslation)
                            // The public user profile (0) does not hold any credentials, and therefore cannot be used in this method.
                            .addValues(Arrays.copyOfRange(SNMPUserProfileType.getDescriptionValues(), 1, SNMPUserProfileType.getDescriptionValues().length))
                            .finish(),
                    this.stringSpec(service, DeviceMessageConstants.snmpPrivPassphrase, DeviceMessageConstants.snmpPrivPassphraseDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.snmpAuthPassphrase, DeviceMessageConstants.snmpAuthPassphraseDefaultTranslation)
            );
        }
    },

    ENABLE_SNMP_USER_PROFILE(4074, "Enable/disable a SNMP user profile") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.snmpUserProfile, DeviceMessageConstants.snmpUserProfileDefaultTranslation)
                            .addValues(SNMPUserProfileType.getDescriptionValues())
                            .finish(),
                    this.booleanSpec(service, DeviceMessageConstants.snmpUserState, DeviceMessageConstants.snmpUserStateDefaultTranslation)
            );
        }
    },

    CHANGE_LTE_APN_NAME(4075, "Change the LTE APN name") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.apnAttributeName, DeviceMessageConstants.apnAttributeDefaultTranslation)
            );
        }
    },

    CHANGE_LTE_PING_ADDRESS(4076, "Change the LTE ping address") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.uplinkPingDestinationAddress, DeviceMessageConstants.enableUplinkPingDefaultTranslation)
            );
        }
    },

    CONFIGURE_INTERFACE_LOCKOUT_PARAMETERS(4077, "Configure interface lockout parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.remoteShellMaxLoginAttempts, DeviceMessageConstants.remoteShellMaxLoginAttemptsDefaultTranslation),
                    this.durationSpec(service, DeviceMessageConstants.remoteShellLockoutDuration, DeviceMessageConstants.remoteShellLockoutDurationDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.webPortalMaxLoginAttempts, DeviceMessageConstants.webPortalMaxLoginAttemptsDefaultTranslation),
                    this.durationSpec(service, DeviceMessageConstants.webPortalLockoutDuration, DeviceMessageConstants.webPortalLockoutDurationDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.snmpMaxLoginAttempts, DeviceMessageConstants.snmpMaxLoginAttemptsDefaultTranslation),
                    this.durationSpec(service, DeviceMessageConstants.snmpLockoutDuration, DeviceMessageConstants.snmpLockoutDurationDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.dlmsLanAllowedFailedAttempts, DeviceMessageConstants.dlmsLanAllowedFailedAttemptsDefaultTranslation),
                    this.durationSpec(service, DeviceMessageConstants.dlmsLanInitialLockoutTime, DeviceMessageConstants.dlmsLanInitialLockoutTimeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.dlmsWanAllowedFailedAttempts, DeviceMessageConstants.dlmsWanAllowedFailedAttemptsDefaultTranslation),
                    this.durationSpec(service, DeviceMessageConstants.dlmsWanInitialLockoutTime, DeviceMessageConstants.dlmsWanInitialLockoutTimeDefaultTranslation)
            );
        }
    },
    CONFIGURE_AUTO_CONNECT_A2(4078, "Configure auto connect for A2 protocol") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.autoConnectMode, DeviceMessageConstants.autoConnectModeDefaultTranslation,
                            AutoConnectModeA2.Inactive.description,
                            AutoConnectModeA2.Active.description,
                            AutoConnectModeA2.DailyActive.description),
                    this.bigDecimalSpec(service, DeviceMessageConstants.autoConnectRepetitions, DeviceMessageConstants.autoConnectRepetitionsDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.autoConnectRepetitionsDelay, DeviceMessageConstants.autoConnectRepetitionsDelayDefaultTranslation),
                    this.optDateTimeSpec(service, DeviceMessageConstants.communicationWindowStartTime1, DeviceMessageConstants.communicationWindowStartTime1DefaultTranslation),
                    this.optDateTimeSpec(service, DeviceMessageConstants.communicationWindowStopTime1, DeviceMessageConstants.communicationWindowStopTime1DefaultTranslation),
                    this.optDateTimeSpec(service, DeviceMessageConstants.communicationWindowStartTime2, DeviceMessageConstants.communicationWindowStartTime2DefaultTranslation),
                    this.optDateTimeSpec(service, DeviceMessageConstants.communicationWindowStopTime2, DeviceMessageConstants.communicationWindowStopTime2DefaultTranslation),
                    this.optDateTimeSpec(service, DeviceMessageConstants.communicationWindowStartTime3, DeviceMessageConstants.communicationWindowStartTime3DefaultTranslation),
                    this.optDateTimeSpec(service, DeviceMessageConstants.communicationWindowStopTime3, DeviceMessageConstants.communicationWindowStopTime3DefaultTranslation),
                    this.optDateTimeSpec(service, DeviceMessageConstants.communicationWindowStartTime4, DeviceMessageConstants.communicationWindowStartTime4DefaultTranslation),
                    this.optDateTimeSpec(service, DeviceMessageConstants.communicationWindowStopTime4, DeviceMessageConstants.communicationWindowStopTime4DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.autoConnectDestination, DeviceMessageConstants.autoConnectDestinationDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.portNumberAttributeName, DeviceMessageConstants.portNumberAttributeDefaultTranslation),
                    this.hexStringSpecOfExactLengthWithDefault(service, DeviceMessageConstants.autoConnectDayMap, DeviceMessageConstants.autoConnectDayMapDefaultTranslation, 8, converter.hexFromString("FFFFFFFF")),
                    this.stringSpec(service, DeviceMessageConstants.autoConnectGSMRegistrationTimeout, DeviceMessageConstants.autoConnectGSMRegistrationTimeoutDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.autoConnectCosemSessionRegistrationTimeout, DeviceMessageConstants.autoConnectCosemSessionRegistrationTimeoutDefaultTranslation)
            );
        }
    },
    CHANGE_SIM_PIN(4079, "Change the sim PIN code") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.simPincode, DeviceMessageConstants.simPincodeDefaultTranslation)
            );
        }
    },
    CHANGE_PUSH_SCHEDULER(4080, "Change push scheduler") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.schedulerNumber, DeviceMessageConstants.schedulerNumberDefaultTranslation, getPossibleValues(1, 4)),
                    this.dateTimeSpec(service, DeviceMessageConstants.executionTime, DeviceMessageConstants.executionTimeDefaultTranslation)
            );
        }
    },
    CHANGE_PUSH_SETUP(4081, "Change push setup parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.pushNumber, DeviceMessageConstants.pushNumberDefaultTranslation, getPushSetupNumbers()),
                    this.stringSpec(service, DeviceMessageConstants.pushObjectList, DeviceMessageConstants.pushObjectListDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.transportTypeAttributeName, DeviceMessageConstants.transportTypeAttributeDefaultTranslation, NetworkConnectivityMessage.TransportType.getTypes()),
                    this.stringSpec(service, DeviceMessageConstants.destinationAddressAttributeName, DeviceMessageConstants.destinationAddressAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.messageTypeAttributeName, DeviceMessageConstants.messageTypeAttributeDefaultTranslation, NetworkConnectivityMessage.MessageType.getTypes()),
                    this.optDateTimeSpec(service, DeviceMessageConstants.communicationWindowStartTime, DeviceMessageConstants.communicationWindowStartTimeDefaultTranslation),
                    this.optDateTimeSpec(service, DeviceMessageConstants.communicationWindowStopTime, DeviceMessageConstants.communicationWindowStopTimeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.randomizationStartInterval, DeviceMessageConstants.randomizationStartIntervalDefaultTranslation, new BigDecimal(7200)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.numberOfRetries, DeviceMessageConstants.numberOfRetriesDefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.repetitionDelay, DeviceMessageConstants.repetitionDelayDefaultTranslation, new BigDecimal(0))
            );
        }
    },
    CHANGE_ORPHAN_STATE_THRESHOLD(4082, "Change orphan state threshold") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.threshold, DeviceMessageConstants.thresholdDefaultTranslation, new BigDecimal(5))
            );
        }
    },

    CHANGE_NETWORK_TIMEOUT(4083, "Change network timeout") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.timeoutObject, DeviceMessageConstants.timeoutObjectDefaultTranslation, NetworkConnectivityMessage.TimeoutType.getTypes()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.sessionMaxDuration, DeviceMessageConstants.sessionMaxDurationDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.inactivityTimeoutAttributeName, DeviceMessageConstants.inactivityTimeoutAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.networkAttachTimeout, DeviceMessageConstants.networkAttachTimeoutDefaultTranslation)
            );
        }
    },
    CHANGE_NBIOT_APN_CREDENTIALS(4084, "Change the NB-IOT APN credentials") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.apnAttributeName, DeviceMessageConstants.apnAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.usernameAttributeName, DeviceMessageConstants.usernameAttributeDefaultTranslation),
                    this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.passwordAttributeName, DeviceMessageConstants.passwordAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_PPP_AUTHENTICATION_PAP(4085, "Change PPP Authentication PAP") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.usernameAttributeName, DeviceMessageConstants.usernameAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.passwordAttributeName, DeviceMessageConstants.passwordAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_PPP_AUTHENTICATION_PAP_TO_NULL(4086, "Reset PPP Authentication PAP") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter) {
            return Collections.emptyList();
        }
    };

    private static BigDecimal[] getPushSetupNumbers() {
        int[] pushSetupNumbers = new int[] { 1, 2, 3, 4, 11, 12, 13, 14};
        return Arrays.stream(pushSetupNumbers).mapToObj(BigDecimal::valueOf).toArray(BigDecimal[]::new);
    }

    public enum TimeoutType {
        GPRS(254),
        NBIOT(255);

        private final int id;

        TimeoutType(int id) {
            this.id = id;
        }

        public static String[] getTypes() {
            return Stream.of(values()).map(NetworkConnectivityMessage.TimeoutType::name).toArray(String[]::new);
        }

        public static String getStringValue(int id) {
            return Stream
                    .of(values())
                    .filter(each -> each.getId() == id)
                    .findFirst()
                    .map(NetworkConnectivityMessage.TimeoutType::name)
                    .orElse("Unknown transport type");
        }

        public int getId() {
            return id;
        }
    }

    public enum TransportType {
        TCP(254),
        UDP(255);

        private final int id;

        TransportType(int id) {
            this.id = id;
        }

        public static String[] getTypes() {
            return Stream.of(values()).map(NetworkConnectivityMessage.TransportType::name).toArray(String[]::new);
        }

        public static String getStringValue(int id) {
            return Stream
                    .of(values())
                    .filter(each -> each.getId() == id)
                    .findFirst()
                    .map(NetworkConnectivityMessage.TransportType::name)
                    .orElse("Unknown transport type");
        }

        public int getId() {
            return id;
        }
    }

    public enum MessageType {
        AXDR(0),
        XML(1);

        private final int id;

        MessageType(int id) {
            this.id = id;
        }

        public static String[] getTypes() {
            return Stream.of(values()).map(NetworkConnectivityMessage.MessageType::name).toArray(String[]::new);
        }

        public static String getStringValue(int id) {
            return Stream
                    .of(values())
                    .filter(each -> each.getId() == id)
                    .findFirst()
                    .map(NetworkConnectivityMessage.MessageType::name)
                    .orElse("Unknown message type");
        }

        public int getId() {
            return id;
        }
    }

    public enum VPNAuthenticationType {
        IKEv2_With_PSK(0, "IKEv2 with PSK"),
        IKEv2_With_Certificates(1, "IKEv2 with certificates"),
        IKEv2_Using_EAP_TLS(2, "IKEv2 using EAP-TLS"),
        ;

        private final int id;
        private final String description;

        VPNAuthenticationType(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static VPNAuthenticationType entryForDescription(String description) {
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .get();
        }

        public static String[] getDescriptionValues() {
            VPNAuthenticationType[] allObjects = values();
            String[] result = new String[allObjects.length];
            for (int index = 0; index < allObjects.length; index++) {
                result[index] = allObjects[index].getDescription();
            }
            return result;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum SNMPNotificationType {
        None(0, "None"),
        Trap(1, "Trap"),
        Inform(2, "Inform");

        private final int id;
        private final String description;

        SNMPNotificationType(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static SNMPNotificationType entryForDescription(String description) {
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .get();
        }

        public static String[] getDescriptionValues() {
            SNMPNotificationType[] allObjects = values();
            String[] result = new String[allObjects.length];
            for (int index = 0; index < allObjects.length; index++) {
                result[index] = allObjects[index].getDescription();
            }
            return result;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum SNMPUserProfileType {
        Public(0, "Public"),
        Read_Only(1, "Read_Only"),
        Read_Write(2, "Read_Write"),
        Management(3, "Management");

        private final int id;
        private final String description;

        SNMPUserProfileType(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static SNMPUserProfileType entryForDescription(String description) {
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .get();
        }

        public static String[] getDescriptionValues() {
            SNMPUserProfileType[] allObjects = values();
            String[] result = new String[allObjects.length];
            for (int index = 0; index < allObjects.length; index++) {
                result[index] = allObjects[index].getDescription();
            }
            return result;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum VPNType {
        IPSec_IKEv2(0, "IPSec/IKEv2"),
        PPTP(1, "PPTP"),
        OpenVPN(2, "OpenVPN"),
        OpenVPN_NL(3, "OpenVPN-NL"),

        ;

        private final int id;
        private final String description;

        VPNType(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static VPNType entryForDescription(String description) {
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .get();
        }

        public static String[] getDescriptionValues() {
            VPNType[] allObjects = values();
            String[] result = new String[allObjects.length];
            for (int index = 0; index < allObjects.length; index++) {
                result[index] = allObjects[index].getDescription();
            }
            return result;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum AlgorithmId {

        CHAP_with_MD5((short) 5),
        SHA_1((short) 6),
        MS_CHAP((short) 128),
        MS_CHAP_2((short) 129);
        private short value;

        AlgorithmId(short value) {
            this.value = value;
        }

        public static String[] valuesAsStringArray() {
            return Arrays.stream(values()).map(Enum::name).toArray(String[]::new);
        }

        public short getValue() {
            return this.value;
        }

    }

    public enum RoutingEntryType {
        G3_PLC(1, "G3 PLC");

        private final int id;
        private final String description;

        RoutingEntryType(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static RoutingEntryType entryForDescription(String description) {
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .get();
        }

        public static String[] getDescriptionValues() {
            RoutingEntryType[] allObjects = values();
            String[] result = new String[allObjects.length];
            for (int index = 0; index < allObjects.length; index++) {
                result[index] = allObjects[index].getDescription();
            }
            return result;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum AutoConnectMode {
        SpecifiedTime(1, "Auto connect at specified time"),
        InsideWindow(2, "Auto connect inside time window"),
        Invalid(-1, "Invalid mode");

        private final int mode;
        private final String description;

        AutoConnectMode(int mode, String description) {
            this.mode = mode;
            this.description = description;
        }

        public static AutoConnectMode modeForDescription(String description) {
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .orElse(Invalid);
        }

        public int getMode() {
            return mode;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum AutoConnectModeA2 {
        Inactive(0,"Scheduler is not active"),
        Active(104,"Scheduler is active"),
        DailyActive(200,"Scheduler is active every day"),
        Invalid(-1, "Invalid mode");

        private final int mode;
        private final String description;

        AutoConnectModeA2(int mode, String description) {
            this.mode = mode;
            this.description = description;
        }

        public static AutoConnectModeA2 modeForDescription(String description) {
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .orElse(Invalid);
        }

        public int getMode() {
            return mode;
        }

        public String getDescription() {
            return description;
        }
    }

    private final long id;
    private final String defaultNameTranslation;

    NetworkConnectivityMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    public enum BeaconSetupObject {
        Remote_Shell_Old_ObisCode("0.16.128.0.0.255"),
        Remote_Shell_New_ObisCode("0.128.96.193.0.255"),
        SNMP_Old_ObisCode("0.17.128.0.0.255"),
        SNMP_New_ObisCode("0.128.96.194.0.255"),
        RTU_Discovery_Old_ObisCode("0.18.128.0.0.255"),
        RTU_Discovery_New_ObisCode("0.18.128.0.0.255 "),
        Web_Portal_Config_Old_ObisCode("0.0.128.0.13.255"),
        Web_Portal_Config_New_ObisCode("0.128.96.197.0.255");

        private final String obis;

        private BeaconSetupObject(String obis) {
            this.obis = obis;
        }

        public static String[] getSetupObjectValues() {
            BeaconSetupObject[] allObjects = values();
            String[] result = new String[allObjects.length];
            for (int index = 0; index < allObjects.length; index++) {
                result[index] = allObjects[index].name();
            }
            return result;
        }

        public ObisCode getObisCode() {
            return ObisCode.fromString(obis);
        }
    }

    @Override
    public long id() {
        return this.id;
    }

    private static BigDecimal[] getPossibleValues(int lowerLimit, int upperLimit) {
        return IntStream
                .range(lowerLimit, upperLimit + 1)
                .mapToObj(BigDecimal::valueOf)
                .toArray(BigDecimal[]::new);
    }

    protected PropertySpec timeSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .timeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    public PropertySpec hexStringSpecOfExactLengthWithDefault(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, int length, HexString defaultValue) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .hexStringSpecOfExactLength(length)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .setDefaultValue(defaultValue)
                .markRequired()
                .finish();
    }

    protected PropertySpec keyAccessorTypeReferenceSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(KeyAccessorType.class.getName())
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec networkOperatorSpec(PropertySpecService service, int index) {
        String defaultTranslation = MessageFormat.format(DeviceMessageConstants.networkOperatorTranslationMessageFormatPattern, index);
        return this.stringSpec(service, DeviceMessageConstants.networkOperator + "_" + index, defaultTranslation);
    }

    private String getNameResourceKey() {
        return NetworkConnectivityMessage.class.getSimpleName() + "." + this.toString();
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service, Converter converter);

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.NETWORK_AND_CONNECTIVITY,
                this.getPropertySpecs(propertySpecService, converter),
                propertySpecService, nlsService, converter);
    }

}