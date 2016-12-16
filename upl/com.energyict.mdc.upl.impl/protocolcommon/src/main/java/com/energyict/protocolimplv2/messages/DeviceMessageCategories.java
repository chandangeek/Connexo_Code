package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import javax.xml.bind.annotation.XmlElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Serves as a implementation to summarize <b>all</b> the supported standard
 * {@link DeviceMessageCategory DeviceMessageCategories}.
 * <p/>
 * When adding new categories, keep in mind to add the proper name translation key (DeviceMessageCategories.enumName)
 * and the description translation key (DeviceMessageCategories.enumName.description) in the NLS database.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (16:12)
 */
public enum DeviceMessageCategories implements DeviceMessageCategorySupplier {

    /**
     * The category for all messages that relate to the Activity Calendar
     */
    ACTIVITY_CALENDAR(0, "Configuration Messages", "The category for all messages that relate to writing an activity calendar, a special days calendar or any other kind of tariff information.") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(ActivityCalendarDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to the contactor/breaker/valve of a Device
     */
    CONTACTOR(1, "Contactor", "The category for all messages that relate to connecting or disconnecting the contactor/breaker/valve of a device") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(ContactorDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to configuring the public lighting objects
     */
    PUBLIC_LIGHTING(2, "Public lighting configuration", "This category summarizes all messages related to configuring public lighting objects (e.g. time switching table, operating mode,...)") {
        @Override
        public List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(PublicLightingDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to configuring alarms
     */
    ALARM_CONFIGURATION(3, "Alarm configuration", "The category for all messages that relate to configuring alarms (e.g. write an alarm filter, reset alarm,…)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(AlarmConfigurationMessage.values());
        }
    },
    /**
     * The category for all messages that relate configuring PLC
     */
    PLC_CONFIGURATION(4, "PLC configuration", "The category for all messages that relate to setting up a complete PLC network") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(PLCConfigurationDeviceMessage.values());
        }
    },
    /**
     * The category for all messages related to resetting values/registers/states/flags
     */
    RESET(5, "Reset", "The category for all messages related to resetting values/registers/states/flags") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Collections.emptyList();
        }
    },
    /**
     * The category for all messages that will change the connectivity setup of a device.
     */
    NETWORK_AND_CONNECTIVITY(6, "Network and connectivity", "The category for all messages that will change the connectivity setup of a device (e.g. GPRS settings)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(NetworkConnectivityMessage.values());
        }
    },
    /**
     * The category for all messages that relate to ZigBee configuration
     */
    ZIGBEE_CONFIGURATION(7, "ZigBee configuration", "The category for all messages that relate to setting up and configuring a ZigBee network (e.g. configure HAN, add slave,...)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(ZigBeeConfigurationDeviceMessage.values());
        }
    },

    /**
     * The category for all messages that relate to authentication, authorisation and encryption.
     */
    SECURITY(8, "Security", "The category for all messages that relate to authentication, authorization and encryption.") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(SecurityMessage.values());
        }
    },

    /**
     * The category for all message that relate to the device's firmware.
     */
    FIRMWARE(9, "Firmware", "The category for all message that relate to upgrading the device's firmware.") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(FirmwareDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to a device action
     */
    DEVICE_ACTIONS(10, "Device actions", "The category for all messages that trigger a specific action of the device (e.g. billing reset, restore factory settings,…)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(DeviceActionMessage.values());
        }
    },
    /**
     * The category for all messages that relate to a pricing information
     */
    PRICING_INFORMATION(11, "Pricing information", "The category for all messages that relate to a pricing information") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(PricingInformationMessage.values());
        }
    },
    /**
     * The category for all messages that relate to <i>a</i> display (InHomeDisplay, Display of E-meter, ...)
     */
    DISPLAY(12, "Display", "The category for all messages that can put messages or codes on a display") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(DisplayDeviceMessage.values());
        }
    },
    /**
     * The category for all general messages, that don't have one unique goal
     */
    GENERAL(13, "General", "The category for all general messages, that don't have one unique goal (e.g. writing a full configuration to a device)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(GeneralDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to load limiting
     */
    LOAD_BALANCE(14, "Load balance", "The category for all messages that relate to load limiting, energy balance and grid stability.") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(LoadBalanceDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all advance test messages
     */
    ADVANCED_TEST(15, "Advanced test messages", "This category summarizes all advanced test messages. These should not be used in normal use cases.") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(AdvancedTestMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to a LoadProfile and their configuration
     */
    LOAD_PROFILES(16, "LoadProfile messages", "This category summarizes all messages related to load profiles and their configuration (e.g. reset the load profile, change the interval,…)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(LoadProfileMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to LogBooks and their configuration
     */
    LOG_BOOKS(17, "Logbooks", "This category summarizes all messages related to logbooks and their configuration (e.g. reset a logbook)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(LogBookDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to setting the meter time
     */
    CLOCK(18, "Clock", "This category summarizes all messages related to configuring the device clock. This can be e.g. setting the current meter time, changing the DST rules or adjusting the time zone offset.") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(ClockDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring a peak shaver
     */
    PEAK_SHAVER_CONFIGURATION(19, "Peak shaver configuration", "This category summarizes all messages related to configuring a peak shaver (e.g. set the active and reactive channel,…)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(PeakShaverConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to setting the mail configuration
     */
    MAIL_CONFIGURATION(20, "Mail configuration", "This category summarizes all messages related to setting the mail configuration (e.g. POP parameters, SMTP parameters,…)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(MailConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring the EIWeb parameters
     */
    EIWEB_PARAMETERS(21, "EIWeb parameters", "This category summarizes all messages related to configuring the EIWeb parameters (e.g. password, web page,…)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(EIWebConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring PPP parameters
     */
    PPP_PARAMETERS(22, "PPP parameters", "This category summarizes all messages related to configuring PPP parameters (e.g. ISP parameters, idle timeout,…)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(PPPConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring a modem
     */
    MODEM_CONFIGURATION(23, "Modem configuration", "This category summarizes all messages related to configuring a modem (e.g. PPP baud rate, dial command,…)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(ModemConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring SMS
     */
    SMS_CONFIGURATION(24, "SMS configuration", "This category summarizes all messages related to configuring SMS settings (e.g. interval, sms number,…)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(SMSConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring ModBus (e.g. on the RTU+Server)
     */
    MODBUS_CONFIGURATION(25, "Modbus configuration", "This category summarizes all messages related to configuring ModBus and writing registers (e.g. on the RTU+Server)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(ModbusConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring MBus (e.g. on the RTU+Server)
     */
    MBUS_CONFIGURATION(26, "MBus configuration", "This category summarizes all messages related to configuring MBus (e.g. on the RTU+Server: write VIF, set inter frame timeout,...)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(MBusConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to setting up an MBus device (in a master slave scenario)
     */
    MBUS_SETUP(27, "MBus setup", "This category summarizes all messages related to setting up an MBus device in a master slave scenario (e.g. set encryption key, decommission,…)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(MBusSetupDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring Opus in the RTU+Server
     */
    OPUS_CONFIGURATION(28, "Opus configuration", "This category summarizes all messages related to configuring Opus on the RTU+Server") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(OpusConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring power failures and power quality
     */
    POWER_CONFIGURATION(29, "Power configuration", "This category summarizes all messages related to configuring power failures and power quality (e.g. sag time threshold, swell threshold,…)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(PowerConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring prepaid
     */
    PREPAID_CONFIGURATION(30, "Prepaid configuration", "This category summarizes all messages related to enabling, disabling and configuring prepaid credit") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(PrepaidConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring DLMS (e.g. via EIWeb, on the RTU+Server)
     */
    DLMS_CONFIGURATION(31, "DLMS configuration", "This category summarizes all messages related to configuring DLMS (e.g. via EIWeb, on the RTU+Server)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(DLMSConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring channels
     */
    CHANNEL_CONFIGURATION(32, "Channel configuration", "This category summarizes all messages related to configuring channels (e.g. name, unit, …)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(ChannelConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring the totalizers
     */
    TOTALIZER_CONFIGURATION(33, "Totalizer configuration", "This category summarizes all messages related to configuring the totalizers (e.g. sum mask, subtract mask, …)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(TotalizersConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to a configuration changes
     */
    CONFIGURATION_CHANGE(34, "Configuration change", "This category summarizes all messages related to configuring parameters that do not fit in any other category.") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(ConfigurationChangeDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to configuring outputs
     */
    OUTPUT_CONFIGURATION(35, "Output configuration", "This category summarizes all messages related to configuring the outputs of a device") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(OutputConfigurationMessage.values());
        }
    },
    /**
     * The category for all messages that relate configuring Wavenis stuff on the RTU+Server
     */
    WAVENIS_CONFIGURATION(36, "Wavenis configuration", "This category summarizes all messages related to configuring the Wavenis functionality (e.g. on the RTU+Server that has a Wavecard)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(WavenisDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to configuring logging (e.g. on the RTU+Server)
     */
    LOGGING_CONFIGURATION(37, "Logging configuration", "This category summarizes all messages related to configuring the logging functionality of a device (e.g. of a concentrator)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(LoggingConfigurationDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to configuring the uplink pinging (e.g. on the RTU+Server)
     */
    UPLINK_CONFIGURATION(38, "Uplink configuration", "The category for all messages that relate to configuring uplink pinging (e.g. write destination address, write ping interval,...)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(UplinkConfigurationDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to configuring the the firewall (e.g of the RTU+Server)
     */
    FIREWALL_CONFIGURATION(39, "Firewall configuration", "The category for all messages that relate to configuring the firewall settings (e.g. activate firewall, enable ssh,...)") {
        @Override
        protected List<DeviceMessageSpecSupplier> factories() {
            return Arrays.asList(FirewallConfigurationMessage.values());
        }
    };

    private final int id;
    private final String defaultNameTranslation;
    private final String defaultDescriptionTranslation;

    private DeviceMessageCategories(int id, String defaultNameTranslation, String defaultDescriptionTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
        this.defaultDescriptionTranslation = defaultDescriptionTranslation;
    }

    protected abstract List<DeviceMessageSpecSupplier> factories();


    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return DeviceMessageCategories.class.getSimpleName() + "." + this.toString();
    }

    /**
     * Gets the resource key that determines the description
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getDescriptionResourceKey() {
        return this.getNameResourceKey() + ".description";
    }

    @Override
    public DeviceMessageCategory get(PropertySpecService propertySpecService, NlsService nlsService) {
        return new DeviceMessageCategoryImpl(
                this.id,
                new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                new TranslationKeyImpl(this.getDescriptionResourceKey(), this.defaultDescriptionTranslation),
                this.factories(),
                propertySpecService,
                nlsService, converter);
    }

}