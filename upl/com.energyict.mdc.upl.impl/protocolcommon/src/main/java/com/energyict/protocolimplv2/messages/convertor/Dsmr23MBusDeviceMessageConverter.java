package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectControlModeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectLoadWithActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DisconnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DisconnectLoadWithActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SetMBusEncryptionKeysMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.LoadProfileRegisterRequestMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.PartialLoadProfileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.google.common.collect.ImmutableMap;

import java.util.Date;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.openKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.toDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.transferKeyAttributeName;

/**
 * Represents a MessageConverter for the legacy NTA DSM2.3 WebRTUKP MBusDevice protocol.
 *
 * @author sva
 * @since 30/10/13 - 8:33
 */
public class Dsmr23MBusDeviceMessageConverter extends AbstractMessageConverter {

    public Dsmr23MBusDeviceMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, extractor);
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                    .<DeviceMessageSpec, MessageEntryCreator>builder()
                    // Disconnect control
                    .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE), new ConnectLoadMessageEntry())
                    .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE), new ConnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName))
                    .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN), new DisconnectLoadMessageEntry())
                    .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE), new DisconnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName))
                    .put(messageSpec(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE), new ConnectControlModeMessageEntry(contactorModeAttributeName))


                    // MBus setup
                    .put(messageSpec(MBusSetupDeviceMessage.Decommission), new OneTagMessageEntry(RtuMessageConstant.MBUS_DECOMMISSION))
                    .put(messageSpec(MBusSetupDeviceMessage.SetEncryptionKeys), new SetMBusEncryptionKeysMessageEntry(openKeyAttributeName, transferKeyAttributeName))
                    .put(messageSpec(MBusSetupDeviceMessage.UseCorrectedValues), new OneTagMessageEntry(RtuMessageConstant.MBUS_CORRECTED_VALUES))
                    .put(messageSpec(MBusSetupDeviceMessage.UseUncorrectedValues), new OneTagMessageEntry(RtuMessageConstant.MBUS_UNCORRECTED_VALUES))

                    // LoadProfiles
                    .put(messageSpec(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST), new PartialLoadProfileMessageEntry(loadProfileAttributeName, fromDateAttributeName, toDateAttributeName))
                    .put(messageSpec(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST), new LoadProfileRegisterRequestMessageEntry(loadProfileAttributeName, fromDateAttributeName))
                    .build();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.contactorActivationDateAttributeName:
                return Long.toString(((Date) messageAttribute).getTime() / 1000);
            case DeviceMessageConstants.loadProfileAttributeName:
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute, this.getExtractor());
            case DeviceMessageConstants.fromDateAttributeName:
            case DeviceMessageConstants.toDateAttributeName:
                return dateTimeFormatWithTimeZone.format((Date) messageAttribute);
            case DeviceMessageConstants.openKeyAttributeName:
            case DeviceMessageConstants.transferKeyAttributeName:
                return ((Password) messageAttribute).getValue();
            default:
                return messageAttribute.toString();
        }
    }

}