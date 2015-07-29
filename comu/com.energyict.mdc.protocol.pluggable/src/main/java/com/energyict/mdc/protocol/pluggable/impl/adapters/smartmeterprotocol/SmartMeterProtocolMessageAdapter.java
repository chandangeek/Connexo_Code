package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.messaging.LegacyMessageConverter;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AbstractDeviceMessageConverterAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.NonExistingMessageConverter;

import com.elster.jupiter.orm.DataModel;

/**
 * Adapter between a {@link MessageProtocol}
 * and the {@link com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport}
 *
 * @author gna
 * @since 10/04/12 - 14:53
 */
public class SmartMeterProtocolMessageAdapter extends AbstractDeviceMessageConverterAdapter {

    public SmartMeterProtocolMessageAdapter(SmartMeterProtocol smartMeterProtocol, DataModel dataModel, MessageAdapterMappingFactory messageAdapterMappingFactory, ProtocolPluggableService protocolPluggableService, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        super(dataModel, messageAdapterMappingFactory, protocolPluggableService, issueService, collectedDataFactory);
        if (MessageProtocol.class.isAssignableFrom(smartMeterProtocol.getClass())) {
            setMessageProtocol((MessageProtocol) smartMeterProtocol);
            Object messageConverter = createNewMessageConverterInstance(getDeviceMessageConverterMappingFor(smartMeterProtocol.getClass().getName()));
            if (LegacyMessageConverter.class.isAssignableFrom(messageConverter.getClass())) {
                final LegacyMessageConverter legacyMessageConverter = (LegacyMessageConverter) messageConverter;
                legacyMessageConverter.setMessagingProtocol((MessageProtocol) smartMeterProtocol);
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