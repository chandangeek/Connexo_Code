/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.BillingResetMessageEntry;

import java.util.HashMap;
import java.util.Map;

public class ABBA1140MessageConverter extends AbstractMessageConverter {

    public ABBA1140MessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        registry.put(DeviceMessageId.DEVICE_ACTIONS_BILLING_RESET, new BillingResetMessageEntry());
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

}