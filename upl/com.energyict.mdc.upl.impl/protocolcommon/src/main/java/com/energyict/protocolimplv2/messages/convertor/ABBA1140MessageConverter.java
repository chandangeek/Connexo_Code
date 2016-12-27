package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.BillingResetMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Represents a MessageConverter for the legacy ABBA1140 protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 12:03
 */
public class ABBA1140MessageConverter extends AbstractMessageConverter {

    public ABBA1140MessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, extractor);
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap.of(messageSpec(DeviceActionMessage.BILLING_RESET), new BillingResetMessageEntry());
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

}
