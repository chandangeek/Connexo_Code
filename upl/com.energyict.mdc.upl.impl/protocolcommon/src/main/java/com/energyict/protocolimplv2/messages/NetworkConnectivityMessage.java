package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;

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
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    DEACTIVATE_SMS_WAKEUP(4002, "Deactivate sms wakeup") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    CHANGE_GPRS_USER_CREDENTIALS(4003, "Change the GPRS user credentials") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.usernameAttributeName, DeviceMessageConstants.usernameAttributeDefaultTranslation),
                    this.passwordSpec(service, DeviceMessageConstants.passwordAttributeName, DeviceMessageConstants.passwordAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_GPRS_APN_CREDENTIALS(4004, "Change the GPRS apn credentials") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.apnAttributeName, DeviceMessageConstants.apnAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.usernameAttributeName, DeviceMessageConstants.usernameAttributeDefaultTranslation),
                    this.passwordSpec(service, DeviceMessageConstants.passwordAttributeName, DeviceMessageConstants.passwordAttributeDefaultTranslation)
            );
        }
    },
    // will be a semicolon separated string (maybe in the future this will be a StringListAspectEditor ...
    ADD_PHONENUMBERS_TO_WHITE_LIST(4005, "Add phonenumbers to white list") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.whiteListPhoneNumbersAttributeName, DeviceMessageConstants.whiteListPhoneNumbersAttributeDefaultTranslation));
        }
    },
    ADD_MANAGED_PHONENUMBERS_TO_WHITE_LIST(4006, "Add managed phonenumbers to white list") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.managedWhiteListPhoneNumbersAttributeName, DeviceMessageConstants.managedWhiteListPhoneNumbersAttributeDefaultTranslation));
        }
    },
    CHANGE_SMS_CENTER_NUMBER(4007, "Change the sms center phone number") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.smsCenterPhoneNumberAttributeName, DeviceMessageConstants.smsCenterPhoneNumberAttributeDefaultTranslation));
        }
    },
    CHANGE_DEVICE_PHONENUMBER(4008, "Change the device phone number") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.devicePhoneNumberAttributeName, DeviceMessageConstants.devicePhoneNumberAttributeDefaultTranslation));
        }
    },
    CHANGE_GPRS_IP_ADDRESS_AND_PORT(4009, "Change the IP address and port number") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.ipAddressAttributeName, DeviceMessageConstants.ipAddressAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.portNumberAttributeName, DeviceMessageConstants.portNumberAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_WAKEUP_FREQUENCY(4010, "Change the wakeup frequency") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.stringSpec(
                            service,
                            DeviceMessageConstants.wakeupPeriodAttributeName, DeviceMessageConstants.wakeupPeriodAttributeDefaultTranslation,
                            "1", "2", "4", "6", "8", "12"));
        }
    },
    CHANGE_INACTIVITY_TIMEOUT(4011, "Change inactivity timeout") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.inactivityTimeoutAttributeName, DeviceMessageConstants.inactivityTimeoutAttributeDefaultTranslation));
        }
    },

    //EIWeb messages
    SetProxyServer(4012, "Set proxy server") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetProxyServerAttributeName, DeviceMessageConstants.SetProxyServerAttributeDefaultTranslation));
        }
    },
    SetProxyUsername(4013, "Set proxy user name") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetProxyUsernameAttributeName, DeviceMessageConstants.SetProxyUsernameAttributeDefaultTranslation));
        }
    },
    SetProxyPassword(4014, "Set proxy password") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetProxyPasswordAttributeName, DeviceMessageConstants.SetProxyPasswordAttributeDefaultTranslation));
        }
    },
    SetDHCP(4015, "Set DHCP") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetDHCPAttributeName, DeviceMessageConstants.SetDHCPAttributeDefaultTranslation));
        }
    },
    SetDHCPTimeout(4016, "Set DHCP timeout") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetDHCPTimeoutAttributeName, DeviceMessageConstants.SetDHCPTimeoutAttributeDefaultTranslation));
        }
    },
    SetIPAddress(4017, "Set IP address") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetIPAddressAttributeName, DeviceMessageConstants.SetIPAddressAttributeDefaultTranslation));
        }
    },
    SetSubnetMask(4018, "Set subnet mask") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetSubnetMaskAttributeName, DeviceMessageConstants.SetSubnetMaskAttributeDefaultTranslation));
        }
    },
    SetGateway(4019, "Set gateway") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetGatewayAttributeName, DeviceMessageConstants.SetGatewayAttributeDefaultTranslation));
        }
    },
    SetNameServer(4020, "Set name server") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetNameServerAttributeName, DeviceMessageConstants.SetNameServerAttributeDefaultTranslation));
        }
    },
    SetHttpPort(4021, "Set HTTP port") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetHttpPortAttributeName, DeviceMessageConstants.SetHttpPortAttributeDefaultTranslation));
        }
    },
    ConfigureKeepAliveSettings(4022, "Configure keepalive settings") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.NetworkConnectivityIPAddressAttributeName, DeviceMessageConstants.NetworkConnectivityIPAddressAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.NetworkConnectivityIntervalAttributeName, DeviceMessageConstants.NetworkConnectivityIntervalAttributeDefaultTranslation)
            );
        }
    },
    PreferGPRSUpstreamCommunication(4023, "Prefer GPRS upstream communication") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.preferGPRSUpstreamCommunication, DeviceMessageConstants.preferGPRSUpstreamCommunicationDefaultTranslation));
        }
    },
    EnableModemWatchdog(4024, "Enable modem watchdog") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.enableModemWatchdog, DeviceMessageConstants.enableModemWatchdogDefaultTranslation));
        }
    },
    SetModemWatchdogParameters(4025, "Write modem watchdog parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
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
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    EnableWhiteList(4027, "Enable usage of white list") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    DisableWhiteList(4028, "Disable usage of white list") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    EnableOperatingWindow(4029, "Enable usage of operating window") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    DisableOperatingWindow(4030, "Disable usage of operating window") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    SetOperatingWindowStartTime(4031, "Set operating window start time") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.timeSpec(service, DeviceMessageConstants.startTime, DeviceMessageConstants.startTimeDefaultTranslation));
        }
    },
    SetOperatingWindowEndTime(4032, "Set operating window end time") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.timeSpec(service, DeviceMessageConstants.endTime, DeviceMessageConstants.endTimeDefaultTranslation));
        }
    },
    RunMeterDiscovery(4033, "Run meter discovery") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    RunAlarmMeterDiscovery(4034, "Run alarm meter discovery") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    RunRepeaterCall(4035, "Run repeater call") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    SetNetworkManagementParameters(4036, "Set network management parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
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
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.SetDHCPAttributeName, DeviceMessageConstants.SetDHCPAttributeDefaultTranslation));
        }
    },
    SetPrimaryDNSAddress(4038, "Set primary DNS address") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.primaryDNSAddressAttributeName, DeviceMessageConstants.primaryDNSAddressAttributeDefaultTranslation));
        }
    },
    SetSecondaryDNSAddress(4039, "Set secondary DNS address") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.secondaryDNSAddressAttributeName, DeviceMessageConstants.secondaryDNSAddressAttributeDefaultTranslation));
        }
    },
    SetAutoConnectMode(4040, "Set GPRS connect mode") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.gprsModeAttributeName, DeviceMessageConstants.gprsModeAttributeDefaultTranslation));
        }
    },
    ChangeSessionTimeout(4041, "Change session timeout") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.sessionTimeoutAttributeName, DeviceMessageConstants.sessionTimeoutAttributeDefaultTranslation));
        }
    },
    SetCyclicMode(4042, "Set cyclic mode") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
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
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
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
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.Destination1IPAddressAttributeName, DeviceMessageConstants.Destination1IPAddressAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.Destination2IPAddressAttributeName, DeviceMessageConstants.Destination2IPAddressAttributeDefaultTranslation)
            );
        }
    },
    WakeupParameters(4045, "Set wakeup parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.wakeupPeriodAttributeName, DeviceMessageConstants.wakeupPeriodAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.inactivityTimeoutAttributeName, DeviceMessageConstants.inactivityTimeoutAttributeDefaultTranslation)
            );
        }
    },
    PreferredNetworkOperatorList(4046, "Set preferred network operator") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return IntStream
                        .range(1, 10)
                        .mapToObj(number -> this.networkOperatorSpec(service, number))
                        .collect(Collectors.toList());
        }
    },
    ConfigureAutoAnswer(4047, "Configure auto answer") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.windowAttributeName, DeviceMessageConstants.windowAttributeDefaultTranslation, getPossibleValues(1, 6)),
                    this.stringSpec(service, DeviceMessageConstants.autoAnswerStartTime, DeviceMessageConstants.autoAnswerStartTimeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.autoAnswerEndTime, DeviceMessageConstants.autoAnswerEndTimeDefaultTranslation)
            );
        }
    },
    DisableAutoAnswer(4048, "Disable auto answer") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.windowAttributeName, DeviceMessageConstants.windowAttributeDefaultTranslation, getPossibleValues(1, 6)));
        }
    },
    ConfigureAutoConnect(4049, "Configure auto connect") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.windowAttributeName, DeviceMessageConstants.windowAttributeDefaultTranslation, getPossibleValues(1, 2)),
                    this.stringSpec(service, DeviceMessageConstants.autoConnectMode, DeviceMessageConstants.autoConnectModeDefaultTranslation, AutoConnectMode.SpecifiedTime.description, AutoConnectMode.InsideWindow.description),
                    this.stringSpec(service, DeviceMessageConstants.autoConnectStartTime, DeviceMessageConstants.autoConnectStartTimeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.autoConnectEndTime, DeviceMessageConstants.autoConnectEndTimeDefaultTranslation, "N/A"),
                    this.stringSpec(service, DeviceMessageConstants.autoConnectDestionation1, DeviceMessageConstants.autoConnectDestionation1DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.autoConnectDestionation2, DeviceMessageConstants.autoConnectDestionation2DefaultTranslation, "N/A")
            );
        }
    },
    DisableAutoConnect(4050, "Disable auto connect") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.windowAttributeName, DeviceMessageConstants.windowAttributeDefaultTranslation, getPossibleValues(1, 2)));
        }
    },
    SetModemWatchdogParameters2(4051, "Write modem watchdog parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
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
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
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
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetHttpsPortAttributeName, DeviceMessageConstants.SetHttpsPortAttributeDefaultTranslation));
        }
    };

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

    private final long id;
    private final String defaultNameTranslation;

    NetworkConnectivityMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    private static BigDecimal[] getPossibleValues(int lowerLimit, int upperLimit) {
        return IntStream
                .range(lowerLimit, upperLimit)
                .mapToObj(BigDecimal::valueOf)
                .toArray(BigDecimal[]::new);
    }

    protected PropertySpecBuilder<String> stringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String... exhaustiveValues) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                        .addValues(exhaustiveValues)
                        .markExhaustive()
                        .finish();
    }

    protected PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal... possibleValues) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).addValues(possibleValues).markExhaustive().finish();
    }

    protected PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .booleanSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
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

    protected PropertySpec durationSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .durationSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec passwordSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .passwordSpec()
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

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.NETWORK_AND_CONNECTIVITY,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

}