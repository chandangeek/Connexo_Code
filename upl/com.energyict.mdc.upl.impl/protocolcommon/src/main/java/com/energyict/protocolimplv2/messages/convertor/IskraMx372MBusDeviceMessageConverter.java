package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DisconnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleValueMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.LoadProfileRegisterRequestMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.PartialLoadProfileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.google.common.collect.ImmutableMap;

import java.util.Date;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.toDateAttributeName;

/**
 * Represents a MessageConverter for the legacy IskraMx372 Mbus protocol.
 *
 *  @author sva
  * @since 25/10/13 - 10:10
 */
public class IskraMx372MBusDeviceMessageConverter extends AbstractMessageConverter {

    private static final String MBUS_SET_VIF = "Mbus_Set_VIF";
    private final Extractor extractor;

    public IskraMx372MBusDeviceMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
        this.extractor = extractor;
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                    .<DeviceMessageSpec, MessageEntryCreator>builder()
                    .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN), new DisconnectLoadMessageEntry())
                    .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE), new ConnectLoadMessageEntry())
                    .put(messageSpec(MBusConfigurationDeviceMessage.SetMBusVIF), new SimpleValueMessageEntry(MBUS_SET_VIF))
                    .put(messageSpec(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST), new PartialLoadProfileMessageEntry(loadProfileAttributeName, fromDateAttributeName, toDateAttributeName))
                    .put(messageSpec(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST), new LoadProfileRegisterRequestMessageEntry(loadProfileAttributeName, fromDateAttributeName))
                    .build();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.loadProfileAttributeName: {
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute, this.extractor);
            }
            case DeviceMessageConstants.fromDateAttributeName:  // Intentional fall-through
            case DeviceMessageConstants.toDateAttributeName: {
                return dateTimeFormatWithTimeZone.format((Date) messageAttribute);
            }
            default: {
                return messageAttribute.toString();
            }
        }
    }

}
