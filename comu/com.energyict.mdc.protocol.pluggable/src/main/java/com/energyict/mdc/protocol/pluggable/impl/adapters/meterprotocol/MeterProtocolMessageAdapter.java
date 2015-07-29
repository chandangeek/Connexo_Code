package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AbstractDeviceMessageConverterAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.NonExistingMessageConverter;
import com.energyict.mdc.protocol.api.messaging.LegacyMessageConverter;

/**
 * Adapter between a {@link MessageProtocol}
 * and the {@link com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport} interface.
 *
 * @author gna
 * @since 5/04/12 - 11:23
 */
public class MeterProtocolMessageAdapter extends AbstractDeviceMessageConverterAdapter {

    public MeterProtocolMessageAdapter(MeterProtocol meterProtocol, DataModel dataModel, MessageAdapterMappingFactory messageAdapterMappingFactory, ProtocolPluggableService protocolPluggableService, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        super(dataModel, messageAdapterMappingFactory, protocolPluggableService, issueService, collectedDataFactory);
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
