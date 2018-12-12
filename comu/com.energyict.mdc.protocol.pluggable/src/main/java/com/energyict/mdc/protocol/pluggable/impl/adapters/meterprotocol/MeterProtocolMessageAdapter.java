/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AbstractDeviceMessageConverterAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.NonExistingMessageConverter;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.protocol.MessageProtocol;

/**
 * Adapter between a {@link MessageProtocol}
 * and the {@link com.energyict.mdc.upl.tasks.support.DeviceMessageSupport} interface.
 *
 * @author gna
 * @since 5/04/12 - 11:23
 */
public class MeterProtocolMessageAdapter extends AbstractDeviceMessageConverterAdapter {

    public MeterProtocolMessageAdapter(MeterProtocol meterProtocol, MessageAdapterMappingFactory messageAdapterMappingFactory, ProtocolPluggableService protocolPluggableService, IssueService issueService, CollectedDataFactory collectedDataFactory, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        super(messageAdapterMappingFactory, protocolPluggableService, issueService, collectedDataFactory, deviceMessageSpecificationService);

        com.energyict.mdc.upl.MeterProtocol actualMeterProtocol = (meterProtocol instanceof UPLProtocolAdapter)
                      ? (com.energyict.mdc.upl.MeterProtocol) ((UPLProtocolAdapter) meterProtocol).getActual()
                      : meterProtocol;

        Class clazz = actualMeterProtocol.getClass();
        if (MessageProtocol.class.isAssignableFrom(clazz)) {
            if (actualMeterProtocol instanceof MessageProtocol) {
                setMessageProtocol((MessageProtocol) actualMeterProtocol);
            }
            Object messageConverter = createNewMessageConverterInstance(getDeviceMessageConverterMappingFor(clazz.getName()));
            if (LegacyMessageConverter.class.isAssignableFrom(messageConverter.getClass())) {
                final LegacyMessageConverter legacyMessageConverter = (LegacyMessageConverter) messageConverter;
                if (actualMeterProtocol instanceof MessageProtocol) {
                    legacyMessageConverter.setMessagingProtocol((MessageProtocol) actualMeterProtocol);
                }
                setLegacyMessageConverter(legacyMessageConverter);
            } else {
                setLegacyMessageConverter(new NonExistingMessageConverter());
            }
        } else {
            noSupportForMessagingRequired();
            setLegacyMessageConverter(new NonExistingMessageConverter());
        }
    }
}
