/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.Collections;
import java.util.Map;

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