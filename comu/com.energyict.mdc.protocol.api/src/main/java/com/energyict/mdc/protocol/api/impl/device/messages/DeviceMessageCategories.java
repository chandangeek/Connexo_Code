package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Serves as a implementation to summarize <b>all</b> the supported standard
 * {@link DeviceMessageCategory DeviceMessageCategories}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (16:12)
 */
public enum DeviceMessageCategories implements TranslationKey {

    /**
     * The category for all messages that relate to the Activity Calendar.
     */
    ACTIVITY_CALENDAR("Activity calendar") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, ActivityCalendarDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to the contactor/breaker/valve of a Device.
     */
    CONTACTOR("Contactor") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, ContactorDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to configuring alarms.
     */
    ALARM_CONFIGURATION("Alarm configuration") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, AlarmConfigurationMessage.values());
        }
    },
    /**
     * The category for all messages that relate configuring PLC.
     */
    PLC_CONFIGURATION("PLC configuration") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, PLCConfigurationDeviceMessage.values());
        }
    },
    /**
     * The category for all messages related to resetting values/registers/states/flags.
     */
    RESET("Reset") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return Collections.emptyList();
        }
    },
    /**
     * The category for all messages that will change the connectivity setup of a device.
     */
    NETWORK_AND_CONNECTIVITY("Network and connectivity") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, NetworkConnectivityMessage.values());
        }
    },
    /**
     * The category for all messages that relate to ZigBee configuration.
     */
    ZIGBEE_CONFIGURATION("ZigBee configuration") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, ZigBeeConfigurationDeviceMessage.values());
        }
    },

    /**
     * The category for all messages that relate to authentication, authorisation and encryption.
     */
    SECURITY("Security") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, SecurityMessage.values());
        }
    },

    /**
     * The category for all message that relate to the device's firmware.
     */
    FIRMWARE("Firmware") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, FirmwareDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to a device action.
     */
    DEVICE_ACTIONS("Device actions") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, DeviceActionMessage.values());
        }
    },
    /**
     * The category for all messages that relate to a pricing information.
     */
    PRICING_INFORMATION("Pricing information") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, PricingInformationMessage.values());
        }
    },
    /**
     * The category for all messages that relate to <i>a</i> display (InHomeDisplay, Display of E-meter, ...).
     */
    DISPLAY("Display") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, DisplayDeviceMessage.values());
        }
    },
    /**
     * The category for all general messages, that don't have one unique goal.
     */
    GENERAL("General") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, GeneralDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to load limiting.
     */
    LOAD_BALANCE("Load balance") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, LoadBalanceDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all advance test messages.
     */
    ADVANCED_TEST("Advanced test messages") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, AdvancedTestMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to a LoadProfile and their configuration.
     */
    LOAD_PROFILES("Load profiles") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, LoadProfileMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to LogBooks and their configuration.
     */
    LOG_BOOKS("Logbooks") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, LogBookDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to setting the meter time.
     */
    CLOCK("Clock") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, ClockDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring a peak shaver.
     */
    PEAK_SHAVER_CONFIGURATION("Peak shaver configuration") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, PeakShaverConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to setting the mail configuration.
     */
    MAIL_CONFIGURATION("Mail configuration") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, MailConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring the EIWeb parameters.
     */
    EIWEB_PARAMETERS("EIWeb parameters") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, EIWebConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring PPP parameters.
     */
    PPP_PARAMETERS("PPP parameters") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, PPPConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring a modem.
     */
    MODEM_CONFIGURATION("Modem configuration") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, ModemConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring SMS.
     */
    SMS_CONFIGURATION("SMS configuration") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, SMSConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring ModBus (e.g. on the RTU+Server).
     */
    MODBUS_CONFIGURATION("Modbus configuration") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, ModbusConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring MBus (e.g. on the RTU+Server).
     */
    MBUS_CONFIGURATION("MBus configuration") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, MBusConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to setting up an MBus device (in a master slave scenario).
     */
    MBUS_SETUP("MBus setup") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, MBusSetupDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring Opus in the RTU+Server.
     */
    OPUS_CONFIGURATION("Opus configuration") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, OpusConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring power failures and power quality.
     */
    POWER_CONFIGURATION("Power configuration") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, PowerConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring Opus in the RTU+Server.
     */
    PREPAID_CONFIGURATION("Prepaid configuration") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, PrepaidConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring DLMS (e.g. via EIWeb, on the RTU+Server).
     */
    DLMS_CONFIGURATION("DLMS configuration") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, DLMSConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring channels.
     */
    CHANNEL_CONFIGURATION("Channel configuration") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, ChannelConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring the totalizers.
     */
    TOTALIZER_CONFIGURATION("Totalizer configuration") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, TotalizersConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to a configuration changes.
     */
    CONFIGURATION_CHANGE("Configuration change") {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, ConfigurationChangeDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all message related to the firewall configuration
     */
    FIREWALL_CONFIGURATION("Firewall configuration"){
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, FirewallConfigurationMessage.values());
        }
    },
    /**
     * Summarizes all messages related to output configuration
     */
    OUTPUT_CONFIGURATION("Output configuration"){
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, OutputConfigurationMessage.values());
        }
    },
    /**
     * Summarizes all messages related to public lighting
     */
    PUBLIC_LIGHTING("Public lighting"){
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, PublicLightingDeviceMessage.values());
        }
    },
    UPLINK_CONFIGURATION("Uplink configuration"){
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.wrapAll(propertySpecService, thesaurus, category, UplinkConfigurationDeviceMessage.values());
        }
    }
    ;

    private String defaultTranslation;

    DeviceMessageCategories(String defaultTranslation) {
        this.defaultTranslation = defaultTranslation;
    }

    protected List<DeviceMessageSpec> wrapAll(PropertySpecService propertySpecService, Thesaurus thesaurus, DeviceMessageCategory category, DeviceMessageSpecEnum... values) {
        return Stream
                .of(values)
                .map(each -> new DeviceMessageSpecAdapter(each, category, propertySpecService, thesaurus))
                .collect(Collectors.toList());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    public String getNameResourceKey() {
        return DeviceMessageCategories.class.getSimpleName() + "." + this.toString();
    }

    /**
     * Gets the resource key that determines the description
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    public String getDescriptionResourceKey() {
        return this.getNameResourceKey() + ".description";
    }

    public abstract List<DeviceMessageSpec> getMessageSpecifications(DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus);

    @Override
    public String getKey() {
        return getNameResourceKey();
    }

    @Override
    public String getDefaultFormat() {
        return defaultTranslation;
    }

    private static class DeviceMessageSpecAdapter implements DeviceMessageSpec {
        private final DeviceMessageSpecEnum enumValue;
        private final DeviceMessageCategory category;
        private final PropertySpecService propertySpecService;
        private final Thesaurus thesaurus;

        private DeviceMessageSpecAdapter(DeviceMessageSpecEnum enumValue, DeviceMessageCategory category, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            this.enumValue = enumValue;
            this.category = category;
            this.propertySpecService = propertySpecService;
            this.thesaurus = thesaurus;
        }

        @Override
        public DeviceMessageCategory getCategory() {
            return this.category;
        }

        @Override
        public String getName() {
            return this.thesaurus.getFormat(this.enumValue).format();
        }

        @Override
        public DeviceMessageId getId() {
            return this.enumValue.getId();
        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            return this.enumValue.getPropertySpecs(this.propertySpecService, this.thesaurus);
        }
    }
}