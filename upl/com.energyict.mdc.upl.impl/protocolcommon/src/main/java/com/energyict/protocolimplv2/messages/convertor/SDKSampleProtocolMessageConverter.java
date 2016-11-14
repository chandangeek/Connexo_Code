package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.ConnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.DemandResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.DisconnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.ErrorStatusResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.PowerOutageResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.PowerQualityResetMessageEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a MessageConverter for the SDKSampleProtocol protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class SDKSampleProtocolMessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        // contactor related
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN, new DisconnectLoadMessageEntry());
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE, new ConnectLoadMessageEntry());

        // reset messages
        registry.put(DeviceActionMessage.DEMAND_RESET, new DemandResetMessageEntry());
        registry.put(DeviceActionMessage.POWER_OUTAGE_RESET, new PowerOutageResetMessageEntry());
        registry.put(DeviceActionMessage.POWER_QUALITY_RESET, new PowerQualityResetMessageEntry());
        registry.put(DeviceActionMessage.ERROR_STATUS_RESET, new ErrorStatusResetMessageEntry());
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public SDKSampleProtocolMessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}
