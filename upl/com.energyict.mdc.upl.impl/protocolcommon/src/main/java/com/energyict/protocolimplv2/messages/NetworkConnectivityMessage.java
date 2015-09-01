package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    ),
    PreferGPRSUpstreamCommunication(22, PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.preferGPRSUpstreamCommunication)),
    EnableModemWatchdog(23, PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.enableModemWatchdog)),
    SetModemWatchdogParameters(24,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.modemWatchdogInterval),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.PPPDaemonResetThreshold),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.modemResetThreshold),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.systemRebootThreshold)
    ),
    ClearWhiteList(25),
    EnableWhiteList(26),
    DisableWhiteList(27),
    EnableOperatingWindow(28),
    DisableOperatingWindow(29),
    SetOperatingWindowStartTime(30, PropertySpecFactory.timeOfDayPropertySpec(DeviceMessageConstants.startTime)),
    SetOperatingWindowEndTime(31, PropertySpecFactory.timeOfDayPropertySpec(DeviceMessageConstants.endTime)),
    RunMeterDiscovery(32),
    RunAlarmMeterDiscovery(33),
    RunRepeaterCall(34),
    SetNetworkManagementParameters(35,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.discoverDuration),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.discoverInterval),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.repeaterCallInterval),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.repeaterCallThreshold),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.repeaterCallTimeslots)
    ),
    SetUseDHCPFlag(36, PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.SetDHCPAttributeName)),
    SetPrimaryDNSAddress(37, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.primaryDNSAddressAttributeName)),
    SetSecondaryDNSAddress(38, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.secondaryDNSAddressAttributeName)),
    SetAutoConnectMode(39, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.gprsModeAttributeName)),
    ChangeSessionTimeout(40, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.sessionTimeoutAttributeName)),
    SetCyclicMode(41,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.day, BigDecimal.ZERO),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.hour),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.minute),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.second)
    ),
    SetPreferredDateMode(42,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.day, BigDecimal.ZERO),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.hour),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.minute),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.second)
    ),
    SetWANConfiguration(43,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.Destination1IPAddressAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.Destination2IPAddressAttributeName)
    ),
    WakeupParameters(44,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.wakeupPeriodAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.inactivityTimeoutAttributeName)
    ),
    PreferredNetworkOperatorList(45,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.networkOperator + "_" + 1),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.networkOperator + "_" + 2),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.networkOperator + "_" + 3),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.networkOperator + "_" + 4),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.networkOperator + "_" + 5),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.networkOperator + "_" + 6),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.networkOperator + "_" + 7),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.networkOperator + "_" + 8),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.networkOperator + "_" + 9),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.networkOperator + "_" + 10)
    ),
    ConfigureAutoAnswer(46,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.windowAttributeName, getPossibleValues(1, 6)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.autoAnswerStartTime),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.autoAnswerEndTime)
    ),
    DisableAutoAnswer(47, PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.windowAttributeName, getPossibleValues(1, 6))),
    ConfigureAutoConnect(48,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.windowAttributeName, getPossibleValues(1, 2)),
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.autoConnectMode, AutoConnectMode.SpecifiedTime.description, AutoConnectMode.InsideWindow.description),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.autoConnectStartTime),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.autoConnectEndTime, "N/A"),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.autoConnectDestionation1),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.autoConnectDestionation2, "N/A")
    ),
    DisableAutoConnect(49, PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.windowAttributeName, getPossibleValues(1, 2))),
    SetModemWatchdogParameters2(50,
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.modemWatchdogInterval),
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.modemWatchdogInitialDelay),
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.PPPDaemonResetThreshold),
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.modemResetThreshold),
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.systemRebootThreshold)
    );

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private NetworkConnectivityMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    /**
     * Construct an array of all possible values within range [lowerLimit, upperLimit]
     *
     * @param lowerLimit the inclusive lower limit
     * @param upperLimit the inclusive upper limit
     * @return the array containing all possible values
     */
    private static BigDecimal[] getPossibleValues(int lowerLimit, int upperLimit) {
        List<BigDecimal> values = new ArrayList<>();
        for (int i = lowerLimit; i <= upperLimit; i++) {
            values.add(BigDecimal.valueOf(i));
        }

        return values.toArray(new BigDecimal[values.size()]);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return DeviceMessageCategories.NETWORK_AND_CONNECTIVITY;
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
            for (AutoConnectMode autoConnectMode : values()) {
                if (autoConnectMode.getDescription().equals(description)) {
                    return autoConnectMode;
                }
            }
            return AutoConnectMode.Invalid;
        }

        public int getMode() {
            return mode;
        }

        public String getDescription() {
            return description;
        }
    }
}
