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

        registry.put(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_DURATION, new MultipleAttributeMessageEntry("SET_LOAD_LIMIT_DURATION", "Duration"));
        registry.put(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD, new MultipleAttributeMessageEntry("SET_LOAD_LIMIT_TRESHOLD", "Threshold"));
        registry.put(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD_WITH_TARIFFS, new MultipleAttributeMessageEntry("SET_LOAD_LIMIT_TRESHOLD", "Threshold", "Tariff(s)"));
        registry.put(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION, new MultipleAttributeMessageEntry("CONFIGURE_LOAD_LIMIT", "Threshold", "Duration"));
        registry.put(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION_WITH_TARIFFS, new MultipleAttributeMessageEntry("CONFIGURE_LOAD_LIMIT", "Threshold", "Tariff(s)", "Duration"));
        registry.put(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_MEASUREMENT_READING_TYPE, new MultipleAttributeMessageEntry("SET_LOAD_LIMIT_MEASUREMENT_VALUE", "MeasurementCode"));

        return registry;
    }

}