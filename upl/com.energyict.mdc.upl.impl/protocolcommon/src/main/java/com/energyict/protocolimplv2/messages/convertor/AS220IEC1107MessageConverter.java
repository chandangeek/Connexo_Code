package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

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
import com.google.common.collect.ImmutableMap;

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

    public AS220IEC1107MessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, extractor, deviceExtractor, registerExtractor, loadProfileExtractor, numberLookupExtractor, deviceMessageFileExtractor, tariffCalendarExtractor);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DisplayMessageAttributeName)) {
            return messageAttribute.toString();
        }
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                // contactor related
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN), new DisconnectLoadMessageEntry())
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE), new ConnectLoadMessageEntry())
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_ARM), new ArmLoadMessageEntry())

                // display related
                .put(messageSpec(DisplayDeviceMessage.SET_DISPLAY_MESSAGE), new SetDisplayMessageEntry(DisplayMessageAttributeName))
                .put(messageSpec(DisplayDeviceMessage.CLEAR_DISPLAY_MESSAGE), new ClearDisplayMessageEntry())

                // reset messages
                .put(messageSpec(DeviceActionMessage.DEMAND_RESET), new DemandResetMessageEntry())
                .put(messageSpec(DeviceActionMessage.POWER_OUTAGE_RESET), new PowerOutageResetMessageEntry())
                .put(messageSpec(DeviceActionMessage.POWER_QUALITY_RESET), new PowerQualityResetMessageEntry())
                .put(messageSpec(DeviceActionMessage.ERROR_STATUS_RESET), new ErrorStatusResetMessageEntry())
                .put(messageSpec(DeviceActionMessage.REGISTERS_RESET), new RegistersResetMessageEntry())
                .put(messageSpec(DeviceActionMessage.LOAD_LOG_RESET), new LoadLogResetMessageEntry())
                .put(messageSpec(DeviceActionMessage.EVENT_LOG_RESET), new EventLogResetMessageEntry())
                .build();
    }
}
