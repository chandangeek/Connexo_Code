package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.powerQualityThresholdAttributeName;

/**
 * Represents a MessageConverter for the legacy WebRTUKP protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class A1440MessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        // contactor related
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN, new DisconnectLoadMessageEntry());
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE, new ConnectLoadMessageEntry());
        registry.put(ContactorDeviceMessage.CONTACTOR_ARM, new ArmLoadMessageEntry());

        // power quality limit related
        registry.put(PowerConfigurationDeviceMessage.IEC1107LimitPowerQuality, new PowerQualityLimitMessageEntry(powerQualityThresholdAttributeName));

        // reset messages
        registry.put(DeviceActionMessage.DEMAND_RESET, new DemandResetMessageEntry());
        registry.put(DeviceActionMessage.POWER_OUTAGE_RESET, new PowerOutageResetMessageEntry());
        registry.put(DeviceActionMessage.POWER_QUALITY_RESET, new PowerQualityResetMessageEntry());
        registry.put(DeviceActionMessage.ERROR_STATUS_RESET, new ErrorStatusResetMessageEntry());
        registry.put(DeviceActionMessage.REGISTERS_RESET, new RegistersResetMessageEntry());
        registry.put(DeviceActionMessage.LOAD_LOG_RESET, new LoadLogResetMessageEntry());
        registry.put(DeviceActionMessage.EVENT_LOG_RESET, new EventLogResetMessageEntry());
    }

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

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}
