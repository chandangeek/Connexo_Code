package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a MessageConverter for protocols which don't have any messages.
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 14:07
 */
public class NoMessageSupportConverter extends AbstractMessageConverter {

    public NoMessageSupportConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        return Collections.emptyMap();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

}