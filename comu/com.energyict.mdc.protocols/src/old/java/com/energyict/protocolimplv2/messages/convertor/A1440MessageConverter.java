/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.ArmLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.ConnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.DemandResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.DisconnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.ErrorStatusResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.EventLogResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.LoadLogResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.PowerOutageResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.PowerQualityLimitMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.PowerQualityResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.RegistersResetMessageEntry;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.powerQualityThresholdAttributeName;

public class A1440MessageConverter extends AbstractMessageConverter {

    /**
     * Default constructor for at-runtime instantiation
     */
    public A1440MessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(powerQualityThresholdAttributeName)) {
            return String.valueOf(((BigDecimal) messageAttribute).intValue());
        } else if (propertySpec.getName().equals(overThresholdDurationAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        }
        return String.valueOf(messageAttribute);
    }

    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        // contactor related
        registry.put(DeviceMessageId.CONTACTOR_OPEN, new DisconnectLoadMessageEntry());
        registry.put(DeviceMessageId.CONTACTOR_CLOSE, new ConnectLoadMessageEntry());
        registry.put(DeviceMessageId.CONTACTOR_ARM, new ArmLoadMessageEntry());

        // power quality limit related
        registry.put(DeviceMessageId.POWER_CONFIGURATION_IEC1107_LIMIT_POWER_QUALITY, new PowerQualityLimitMessageEntry(powerQualityThresholdAttributeName));

        // reset messages
        registry.put(DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET, new DemandResetMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_POWER_OUTAGE_RESET, new PowerOutageResetMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_POWER_QUALITY_RESET, new PowerQualityResetMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_ERROR_STATUS_RESET, new ErrorStatusResetMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_REGISTERS_RESET, new RegistersResetMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_EVENT_LOG_RESET, new LoadLogResetMessageEntry());
        registry.put(DeviceMessageId.DEVICE_ACTIONS_EVENT_LOG_RESET, new EventLogResetMessageEntry());

        registry.put(DeviceMessageId.LOAD_BALANCING_DISABLE_LOAD_LIMITING, new MultipleAttributeMessageEntry("DISABLE_LOAD_LIMIT"));
        registry.put(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD, new MultipleAttributeMessageEntry("SET_LOAD_LIMIT_TRESHOLD", "Threshold", "Unit"));
        registry.put(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION, new MultipleAttributeMessageEntry("CONFIGURE_LOAD_LIMIT", "Threshold", "Unit", "Duration"));

        return registry;
    }

}