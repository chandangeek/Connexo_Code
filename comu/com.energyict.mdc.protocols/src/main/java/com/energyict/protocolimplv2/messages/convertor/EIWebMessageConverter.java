/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.AnalogOutMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.ChangeAdminPasswordMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.ChannelMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.EIWebConfigurationMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.SetLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.SetSetpointMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.SetSwitchTimeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.SimpleEIWebMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.SimplePeakShaverMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.TotalizerEIWebMessageEntry;

import java.util.HashMap;
import java.util.Map;

public class EIWebMessageConverter extends AbstractMessageConverter {

    /**
     * Default constructor for at-runtime instantiation
     */
    public EIWebMessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        // General Parameters
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_SET_DESCRIPTION, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_SET_INTERVAL_IN_SECONDS, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_SET_UPGRADE_URL, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_SET_UPGRADE_OPTIONS, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_SET_DEBOUNCE_TRESHOLD, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_SET_TARIFF_MOMENT, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_SET_COMMUNICATION_OFFSET, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_SET_AGGREGATION_INTERVAL, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_SET_PULSE_TIME_TRUE, new SimpleEIWebMessageEntry());

        // Network Parameters
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_SET_PROXY_SERVER, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_SET_PROXY_USERNAME, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_SET_PROXY_PASSWORD, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_SET_DHCP, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_SET_DHCP_TIMEOUT, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_SET_IP_ADDRESS, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_SET_SUBNET_MASK, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_SET_GATEWAY, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_SET_NAME_SERVER, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_SET_HTTP_PORT, new SimpleEIWebMessageEntry());

        // Time Parameters
        registry.put(DeviceMessageId.CLOCK_SET_DST, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.CLOCK_SET_TIMEZONE, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.CLOCK_SET_TIME_ADJUSTMENT, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.CLOCK_SET_NTP_SERVER, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.CLOCK_SET_REFRESH_CLOCK_EVERY, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.CLOCK_SET_NTP_OPTIONS, new SimpleEIWebMessageEntry());

        // EIWeb Parameters
        registry.put(DeviceMessageId.EIWEB_SET_PASSWORD, new EIWebConfigurationMessageEntry());
        registry.put(DeviceMessageId.EIWEB_SET_PAGE, new EIWebConfigurationMessageEntry());
        registry.put(DeviceMessageId.EIWEB_SET_FALLBACK_PAGE, new EIWebConfigurationMessageEntry());
        registry.put(DeviceMessageId.EIWEB_SET_SEND_EVERY, new EIWebConfigurationMessageEntry());
        registry.put(DeviceMessageId.EIWEB_SET_CURRENT_INTERVAL, new EIWebConfigurationMessageEntry());
        registry.put(DeviceMessageId.EIWEB_SET_DATABASE_ID, new EIWebConfigurationMessageEntry());
        registry.put(DeviceMessageId.EIWEB_SET_OPTIONS, new EIWebConfigurationMessageEntry());

        // Read Mail (POP3) Parameters
        registry.put(DeviceMessageId.MAIL_CONFIGURATION_SET_POP_USERNAME, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MAIL_CONFIGURATION_SET_POP_PASSWORD, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MAIL_CONFIGURATION_SET_POP_HOST, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MAIL_CONFIGURATION_SET_POP_READ_MAIL_EVERY, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MAIL_CONFIGURATION_SET_POP3_OPTIONS, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_FROM, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_TO, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_CONFIGURATION_TO, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_SERVER, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_DOMAIN, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_SEND_MAIL_EVERY, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_CURRENT_INTERVAL, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_DATABASE_ID, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MAIL_CONFIGURATION_SET_SMTP_OPTIONS, new SimpleEIWebMessageEntry());

        // SMS
        registry.put(DeviceMessageId.SMS_CONFIGURATION_SET_DATA_NUMBER, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.SMS_CONFIGURATION_SET_ALARM_NUMBER, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.SMS_CONFIGURATION_SET_EVERY, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.SMS_CONFIGURATION_SET_NUMBER, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.SMS_CONFIGURATION_SET_CORRECTION, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.SMS_CONFIGURATION_SET_CONFIG, new SimpleEIWebMessageEntry());

        //DLMS
        registry.put(DeviceMessageId.DLMS_CONFIGURATION_SET_DEVICE_ID, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.DLMS_CONFIGURATION_SET_METER_ID, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.DLMS_CONFIGURATION_SET_PASSWORD, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.DLMS_CONFIGURATION_SET_IDLE_TIME, new SimpleEIWebMessageEntry());

        // Duke Power Protocol
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_SET_DUKE_POWER_ID, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_SET_DUKE_POWER_PASSWORD, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_SET_DUKE_POWER_IDLE_TIME, new SimpleEIWebMessageEntry());

        registry.put(DeviceMessageId.MODEM_CONFIGURATION_SET_DIAL_COMMAND, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MODEM_CONFIGURATION_SET_MODEM_INIT_1, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MODEM_CONFIGURATION_SET_MODEM_INIT_2, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MODEM_CONFIGURATION_SET_MODEM_INIT_3, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MODEM_CONFIGURATION_SET_PPP_BAUD_RATE, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MODEM_CONFIGURATION_SET_MODEMTYPE, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MODEM_CONFIGURATION_SET_RESET_CYCLE, new SimpleEIWebMessageEntry());

        // PPP
        registry.put(DeviceMessageId.PPP_CONFIGURATION_SET_ISP1_PHONE, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.PPP_CONFIGURATION_SET_ISP1_USERNAME, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.PPP_CONFIGURATION_SET_ISP1_PASSWORD, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.PPP_CONFIGURATION_SET_ISP1_TRIES, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.PPP_CONFIGURATION_SET_ISP2_PHONE, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.PPP_CONFIGURATION_SET_ISP2_USERNAME, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.PPP_CONFIGURATION_SET_ISP2_PASSWORD, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.PPP_CONFIGURATION_SET_ISP2_TRIES, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.PPP_CONFIGURATION_SET_IDLE_TIMEOUT, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.PPP_CONFIGURATION_SET_RETRY_INTERVAL, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.PPP_CONFIGURATION_SET_OPTIONS, new SimpleEIWebMessageEntry());

        // Channels
        registry.put(DeviceMessageId.CHANNEL_CONFIGURATION_SET_FUNCTION, new ChannelMessageEntry());
        registry.put(DeviceMessageId.CHANNEL_CONFIGURATION_SET_PARAMETERS, new ChannelMessageEntry());
        registry.put(DeviceMessageId.CHANNEL_CONFIGURATION_SET_NAME, new ChannelMessageEntry());
        registry.put(DeviceMessageId.CHANNEL_CONFIGURATION_SET_UNIT, new ChannelMessageEntry());

        // Totalizers
        registry.put(DeviceMessageId.TOTALIZER_CONFIGURATION_SET_SUM_MASK, new TotalizerEIWebMessageEntry());
        registry.put(DeviceMessageId.TOTALIZER_CONFIGURATION_SET_SUBSTRACT_MASK, new TotalizerEIWebMessageEntry());

        // Peak Shavers
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_ACTIVE_CHANNEL, new SimplePeakShaverMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_REACTIVE_CHANNEL, new SimplePeakShaverMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_TIME_BASE, new SimplePeakShaverMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_P_OUT, new SimplePeakShaverMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_P_IN, new SimplePeakShaverMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_DEAD_TIME, new SimplePeakShaverMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_AUTOMATIC, new SimplePeakShaverMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_CYCLIC, new SimplePeakShaverMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_INVERT, new SimplePeakShaverMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_ADAPT_SETPOINT, new SimplePeakShaverMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_INSTANT_ANALOG_OUT, new SimplePeakShaverMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_PREDICTED_ANALOG_OUT, new SimplePeakShaverMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SETPOINT_ANALOG_OUT, new SimplePeakShaverMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_DIFFERENCE_ANALOG_OUT, new SimplePeakShaverMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_TARIFF, new SimplePeakShaverMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_RESET_LOADS, new SimplePeakShaverMessageEntry());

        registry.put(DeviceMessageId.PEAK_SHAVING_SET_SETPOINT, new SetSetpointMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_SWITCH_TIME, new SetSwitchTimeMessageEntry());
        registry.put(DeviceMessageId.PEAK_SHAVING_SET_LOAD, new SetLoadMessageEntry());

        // Events
        registry.put(DeviceMessageId.LOG_BOOK_SET_INPUT_CHANNEL, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.LOG_BOOK_SET_CONDITION, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.LOG_BOOK_SET_CONDITION_VALUE, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.LOG_BOOK_SET_TIME_TRUE, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.LOG_BOOK_SET_TIME_FALSE, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.LOG_BOOK_SET_OUTPUT_CHANNEL, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.LOG_BOOK_SET_ALARM, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.LOG_BOOK_SET_TAG, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.LOG_BOOK_SET_INVERSE, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.LOG_BOOK_SET_IMMEDIATE, new SimpleEIWebMessageEntry());

        // Opus
        registry.put(DeviceMessageId.OPUS_CONFIGURATION_SET_OS_NUMBER, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.OPUS_CONFIGURATION_SET_PASSWORD, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.OPUS_CONFIGURATION_SET_TIMEOUT, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.OPUS_CONFIGURATION_SET_CONFIG, new SimpleEIWebMessageEntry());

        // Modbus Master
        registry.put(DeviceMessageId.MODBUS_CONFIGURATION_SET_MM_EVERY, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MODBUS_CONFIGURATION_SET_MM_TIMEOUT, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MODBUS_CONFIGURATION_SET_MM_INSTANT, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MODBUS_CONFIGURATION_SET_MM_OVERFLOW, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MODBUS_CONFIGURATION_SET_MM_CONFIG, new SimpleEIWebMessageEntry());

        // MBus Master
        registry.put(DeviceMessageId.MBUS_CONFIGURATION_SET_EVERY, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MBUS_CONFIGURATION_SET_INTER_FRAME_TIME, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.MBUS_CONFIGURATION_SET_CONFIG, new SimpleEIWebMessageEntry());

        // General Commands
        registry.put(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_REBOOT, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_INITIALIZE, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_MAIL_LOG, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_MAIL_CONFIG, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_SAVE_CONFIG, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_UPGRADE, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_CLEAR_MEM, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_MODEM_RESET, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_CHANGE_ADMIN_PASSWORD, new ChangeAdminPasswordMessageEntry());

        registry.put(DeviceMessageId.DEVICE_ACTIONS_SET_OUTPUT_ON, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_SET_OUTPUT_OFF, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_SET_OUTPUT_TOGGLE, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_SET_OUTPUT_PULSE, new SimpleEIWebMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_SET_ANALOG_OUT, new AnalogOutMessageEntry());
        return registry;
    }

}