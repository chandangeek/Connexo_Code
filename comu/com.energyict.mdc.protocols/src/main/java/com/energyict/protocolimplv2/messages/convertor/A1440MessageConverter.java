package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
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

import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.powerQualityThresholdAttributeName;

/**
 * Represents a MessageConverter for the legacy WebRTUKP protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
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
        }
        return EMPTY_FORMAT;
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
        return registry;
    }

}