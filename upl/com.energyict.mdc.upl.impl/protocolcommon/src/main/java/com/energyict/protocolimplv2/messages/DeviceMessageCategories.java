package com.energyict.protocolimplv2.messages;

import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageCategoryPrimaryKey;
import com.energyict.mdc.messages.DeviceMessageSpec;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Serves as a implementation to summarize <b>all</b> the supported standard
 * {@link DeviceMessageCategory DeviceMessageCategories}
 * <p/>
 * When adding new categories, keep in mind to add the proper name translation key (DeviceMessageCategories.enumName)
 * and the description translation key (DeviceMessageCategories.enumName.description) in the NLS database.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (16:12)
 */
public enum DeviceMessageCategories implements DeviceMessageCategory {

    /**
     * The category for all messages that relate to the Activity Calendar
     */
    ACTIVITY_CALENDAR(0) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ActivityCalendarDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to the contactor/breaker/valve of a Device
     */
    CONTACTOR(1) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ContactorDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to configuring the public lighting objects
     */
    PUBLIC_LIGHTING(2) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(PublicLightingDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to configuring alarms
     */
    ALARM_CONFIGURATION(3) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(AlarmConfigurationMessage.values());
        }
    },
    /**
     * The category for all messages that relate configuring PLC
     */
    PLC_CONFIGURATION(4) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(PLCConfigurationDeviceMessage.values());
        }
    },
    /**
     * The category for all messages related to resetting values/registers/states/flags
     */
    RESET(5) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Collections.emptyList();
        }
    },
    /**
     * The category for all messages that will change the connectivity setup of a device.
     */
    NETWORK_AND_CONNECTIVITY(6) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(NetworkConnectivityMessage.values());
        }
    },
    /**
     * The category for all messages that relate to ZigBee configuration
     */
    ZIGBEE_CONFIGURATION(7) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ZigBeeConfigurationDeviceMessage.values());
        }
    },

    /**
     * The category for all messages that relate to authentication, authorisation and encryption.
     */
    SECURITY(8) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(SecurityMessage.values());
        }
    },

    /**
     * The category for all message that relate to the device's firmware.
     */
    FIRMWARE(9) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(FirmwareDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to a device action
     */
    DEVICE_ACTIONS(10) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(DeviceActionMessage.values());
        }
    },
    /**
     * The category for all messages that relate to a pricing information
     */
    PRICING_INFORMATION(11) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(PricingInformationMessage.values());
        }
    },
    /**
     * The category for all messages that relate to <i>a</i> display (InHomeDisplay, Display of E-meter, ...)
     */
    DISPLAY(12) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(DisplayDeviceMessage.values());
        }
    },
    /**
     * The category for all general messages, that don't have one unique goal
     */
    GENERAL(13) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(GeneralDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to load limiting
     */
    LOAD_BALANCE(14) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(LoadBalanceDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all advance test messages
     */
    ADVANCED_TEST(15) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(AdvancedTestMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to a LoadProfile and their configuration
     */
    LOAD_PROFILES(16) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(LoadProfileMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to LogBooks and their configuration
     */
    LOG_BOOKS(17) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(LogBookDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to setting the meter time
     */
    CLOCK(18) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ClockDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring a peak shaver
     */
    PEAK_SHAVER_CONFIGURATION(19) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(PeakShaverConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to setting the mail configuration
     */
    MAIL_CONFIGURATION(20) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(MailConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring the EIWeb parameters
     */
    EIWEB_PARAMETERS(21) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(EIWebConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring PPP parameters
     */
    PPP_PARAMETERS(22) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(PPPConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring a modem
     */
    MODEM_CONFIGURATION(23) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ModemConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring SMS
     */
    SMS_CONFIGURATION(24) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(SMSConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring ModBus (e.g. on the RTU+Server)
     */
    MODBUS_CONFIGURATION(25) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ModbusConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring MBus (e.g. on the RTU+Server)
     */
    MBUS_CONFIGURATION(26) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(MBusConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to setting up an MBus device (in a master slave scenario)
     */
    MBUS_SETUP(27) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(MBusSetupDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring Opus in the RTU+Server
     */
    OPUS_CONFIGURATION(28) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(OpusConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring power failures and power quality
     */
    POWER_CONFIGURATION(29) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(PowerConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring prepaid
     */
    PREPAID_CONFIGURATION(30) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(PrepaidConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring DLMS (e.g. via EIWeb, on the RTU+Server)
     */
    DLMS_CONFIGURATION(31) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(DLMSConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring channels
     */
    CHANNEL_CONFIGURATION(32) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ChannelConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring the totalizers
     */
    TOTALIZER_CONFIGURATION(33) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(TotalizersConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to a configuration changes
     */
    CONFIGURATION_CHANGE(34) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ConfigurationChangeDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to configuring outputs
     */
    OUTPUT_CONFIGURATION(35) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(OutputConfigurationMessage.values());
        }
    },
    /**
     * The category for all messages that relate configuring Wavenis stuff on the RTU+Server
     */
    WAVENIS_CONFIGURATION(36) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(WavenisDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to configuring logging (e.g. on the RTU+Server)
     */
    LOGGING_CONFIGURATION(37) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(LoggingConfigurationDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to configuring the uplink pinging (e.g. on the RTU+Server)
     */
    UPLINK_CONFIGURATION(38) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(UplinkConfigurationDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to configuring the the firewall (e.g of the RTU+Server)
     */
    FIREWALL_CONFIGURATION(39) {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(FirewallConfigurationMessage.values());
        }
    };

    private final int id;
    private String name;

    private DeviceMessageCategories(int id) {
        this.id = id;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @Override
    @XmlAttribute
    public String getName() {
        if (name == null) {
            name = UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
        }
        return name;
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

    @Override
    public String getDescription() {
        return UserEnvironment.getDefault().getTranslation(this.getDescriptionResourceKey());
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
    public int getId() {
        return id;
    }

    @Override
    public abstract List<DeviceMessageSpec> getMessageSpecifications();

    @Override
    public DeviceMessageCategoryPrimaryKey getPrimaryKey() {
        return new DeviceMessageCategoryPrimaryKey(this, name());
    }
}