package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages;


import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40Messaging;

import java.util.HashSet;
import java.util.Set;

/**
 * Messaging class for the AM540 device, which contains a combination of G3 PLC messages and DSMR 4.0 messages
 *
 * @author sva
 * @since 6/01/2015 - 9:39
 */
public class AM540Messaging extends Dsmr40Messaging implements DeviceMessageSupport {

    private final static Set<DeviceMessageId> am540Messages;

    static {
        am540Messages = new HashSet<>();

        // PLC configuration - G3 PLC OFDM MAC setup
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_TMR_TTL);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_FRAME_RETRIES);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_NEIGHBOUR_TABLE_ENTRY_TTL);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_HIGH_PRIORITY_WINDOW_SIZE);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_CSMA_FAIRNESS_LIMIT);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_BEACON_RANDOMIZATION_WINDOW_LENGTH);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MAC_A);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MAC_K);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MINIMUM_CW_ATTEMPTS);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_BE);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_CSMA_BACK_OFF);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MIN_BE);

        // PLC configuration - G3 6LoWPAN layer setup
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_NUMBER_OF_HOPS_ATTRIBUTENAME);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_WEAK_LQI_VALUE_ATTRIBUTENAME);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_SECURITY_LEVEL);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_ROUTING_CONFIGURATION);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_BROAD_CAST_LOG_TABLE_ENTRY_TTL);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_JOIN_WAIT_TIME);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_PATH_DISCOVERY_TIME);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_METRIC_TYPE);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_COORD_SHORT_ADDRESS);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_DISABLE_DEFAULT_ROUTING);
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_SET_DEVICE_TYPE);

        // PLC configuration - Miscellaneous
        am540Messages.add(DeviceMessageId.PLC_CONFIGURATION_WRITE_PLC_G3_TIMEOUT);

        // Relay control
        am540Messages.add(DeviceMessageId.CONTACTOR_CLOSE_RELAY);
        am540Messages.add(DeviceMessageId.CONTACTOR_OPEN_RELAY);
    }


    public AM540Messaging(AbstractMessageExecutor messageExecutor, TopologyService topologyService) {
        super(messageExecutor, topologyService);
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {

        this.setSupportLimiter(true);
        this.setSupportMBus(false);
        this.setSupportGPRS(false);
        this.setSupportMeterReset(false);
        this.setSupportResetWindow(false);
        Set<DeviceMessageId> allMessages = super.getSupportedMessages();
        allMessages.addAll(am540Messages);

        // DSMR5.0 security related messages who have changed compared to DSM4.0
        allMessages.remove(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY);
        allMessages.remove(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY);
        allMessages.add(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS);
        allMessages.add(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS);

        return allMessages;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (messageAttribute instanceof TimeDuration) {
            return Integer.toString(((TimeDuration) messageAttribute).getSeconds());
        } else {
            return super.format(propertySpec, messageAttribute);
        }
    }
}