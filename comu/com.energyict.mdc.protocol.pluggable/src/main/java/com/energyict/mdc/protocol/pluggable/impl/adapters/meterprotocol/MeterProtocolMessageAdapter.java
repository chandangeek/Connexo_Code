package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.messaging.LegacyMessageConverter;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AbstractDeviceMessageConverterAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.NonExistingMessageConverter;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

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
        if (MessageProtocol.class.isAssignableFrom(meterProtocol.getClass())) {
            setMessageProtocol((MessageProtocol) meterProtocol);
            Object messageConverter = createNewMessageConverterInstance(getDeviceMessageConverterMappingFor(meterProtocol.getClass().getName()));
            if (LegacyMessageConverter.class.isAssignableFrom(messageConverter.getClass())) {
                final LegacyMessageConverter legacyMessageConverter = (LegacyMessageConverter) messageConverter;
                legacyMessageConverter.setMessagingProtocol((MessageProtocol) meterProtocol);
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
