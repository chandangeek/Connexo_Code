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
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Represents a MessageConverter for the legacy ABBA1700 protocol.
 *
 *  @author sva
  * @since 24/10/13 - 10:10
 */
public class ABBA1700MessageConverter extends AbstractMessageConverter {

    private static final String DEMAND_RESET = "DemandReset";

    public ABBA1700MessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, extractor, deviceExtractor, registerExtractor, loadProfileExtractor, numberLookupExtractor, deviceMessageFileExtractor, tariffCalendarExtractor);
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap.of(messageSpec(DeviceActionMessage.BILLING_RESET), new OneTagMessageEntry(DEMAND_RESET));
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

}