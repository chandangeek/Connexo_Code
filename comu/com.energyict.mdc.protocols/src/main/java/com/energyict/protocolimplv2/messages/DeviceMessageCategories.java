package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategoryPrimaryKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Serves as a implementation to summarize <b>all</b> the supported standard
 * {@link DeviceMessageCategory DeviceMessageCategories}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (16:12)
 */
public enum DeviceMessageCategories implements DeviceMessageCategory {

    /**
     * The category for all messages that relate to the Activity Calendar
     */
    ACTIVITY_CALENDAR {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ActivityCalendarDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to the contactor/breaker/valve of a Device
     */
    CONTACTOR {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ContactorDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to configuring alarms
     */
    ALARM_CONFIGURATION {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(AlarmConfigurationMessage.values());
        }
    },
    /**
     * The category for all messages that relate configuring PLC
     */
    PLC_CONFIGURATION {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(PLCConfigurationDeviceMessage.values());
        }
    },
    /**
     * The category for all messages related to resetting values/registers/states/flags
     */
    RESET {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Collections.emptyList();
        }
    },
    /**
     * The category for all messages that will change the connectivity setup of a device.
     */
    NETWORK_AND_CONNECTIVITY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(NetworkConnectivityMessage.values());
        }
    },
    /**
     * The category for all messages that relate to ZigBee configuration
     */
    ZIGBEE_CONFIGURATION {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ZigBeeConfigurationDeviceMessage.values());
        }
    },

    /**
     * The category for all messages that relate to authentication, authorisation and encryption.
     */
    SECURITY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(SecurityMessage.values());
        }
    },

    /**
     * The category for all message that relate to the device's firmware.
     */
    FIRMWARE {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(FirmwareDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to a device action
     */
    DEVICE_ACTIONS {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(DeviceActionMessage.values());
        }
    },
    /**
     * The category for all messages that relate to a pricing information
     */
    PRICING_INFORMATION {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(PricingInformationMessage.values());
        }
    },
    /**
     * The category for all messages that relate to <i>a</i> display (InHomeDisplay, Display of E-meter, ...)
     */
    DISPLAY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(DisplayDeviceMessage.values());
        }
    },
    /**
     * The category for all general messages, that don't have one unique goal
     */
    GENERAL {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(GeneralDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to load limiting
     */
    LOAD_BALANCE {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(LoadBalanceDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all advance test messages
     */
    ADVANCED_TEST {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(AdvancedTestMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to a LoadProfile and their configuration
     */
    LOAD_PROFILES {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(LoadProfileMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to LogBooks and their configuration
     */
    LOG_BOOKS {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(LogBookDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to setting the meter time
     */
    CLOCK {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ClockDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring a peak shaver
     */
    PEAK_SHAVER_CONFIGURATION {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(PeakShaverConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to setting the mail configuration
     */
    MAIL_CONFIGURATION {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(MailConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring the EIWeb parameters
     */
    EIWEB_PARAMETERS {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(EIWebConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring PPP parameters
     */
    PPP_PARAMETERS {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(PPPConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring a modem
     */
    MODEM_CONFIGURATION {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ModemConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring SMS
     */
    SMS_CONFIGURATION {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(SMSConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring ModBus (e.g. on the RTU+Server)
     */
    MODBUS_CONFIGURATION {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ModbusConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring MBus (e.g. on the RTU+Server)
     */
    MBUS_CONFIGURATION {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(MBusConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to setting up an MBus device (in a master slave scenario)
     */
    MBUS_SETUP {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(MBusSetupDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring Opus in the RTU+Server
     */
    OPUS_CONFIGURATION {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(OpusConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring power failures and power quality
     */
    POWER_CONFIGURATION {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(PowerConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring Opus in the RTU+Server
     */
    PREPAID_CONFIGURATION {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(PrepaidConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring DLMS (e.g. via EIWeb, on the RTU+Server)
     */
    DLMS_CONFIGURATION {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(DLMSConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring channels
     */
    CHANNEL_CONFIGURATION {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ChannelConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to configuring the totalizers
     */
    TOTALIZER_CONFIGURATION {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(TotalizersConfigurationDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to a configuration changes
     */
    CONFIGURATION_CHANGE {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ConfigurationChangeDeviceMessage.values());
        }
    };


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
        return this.ordinal();
    }

    @Override
    public abstract List<DeviceMessageSpec> getMessageSpecifications();

    @Override
    public DeviceMessageCategoryPrimaryKey getPrimaryKey() {
        return new DeviceMessageCategoryPrimaryKey(this, name());
    }
}