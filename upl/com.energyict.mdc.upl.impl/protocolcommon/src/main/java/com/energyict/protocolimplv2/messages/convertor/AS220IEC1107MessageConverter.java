package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.ArmLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.ClearDisplayMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.ConnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.DemandResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.DisconnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.ErrorStatusResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.EventLogResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.LoadLogResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.PowerOutageResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.PowerQualityResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.RegistersResetMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107.SetDisplayMessageEntry;

import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DisplayMessageAttributeName;

/**
 * Represents a MessageConverter for the legacy WebRTUKP protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class AS220IEC1107MessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        // contactor related
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN, new DisconnectLoadMessageEntry());
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE, new ConnectLoadMessageEntry());
        registry.put(ContactorDeviceMessage.CONTACTOR_ARM, new ArmLoadMessageEntry());

        // display related
        registry.put(DisplayDeviceMessage.SET_DISPLAY_MESSAGE, new SetDisplayMessageEntry(DisplayMessageAttributeName));
        registry.put(DisplayDeviceMessage.CLEAR_DISPLAY_MESSAGE, new ClearDisplayMessageEntry());

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
    public AS220IEC1107MessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DisplayMessageAttributeName)) {
            return messageAttribute.toString();
        }
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}
