package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.messaging.LegacyMessageConverter;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AbstractDeviceMessageConverterAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.NonExistingMessageConverter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLProtocolAdapter;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

/**
 * Adapter between a {@link MessageProtocol}
 * and the {@link com.energyict.mdc.upl.tasks.support.DeviceMessageSupport}
 *
 * @author gna
 * @since 10/04/12 - 14:53
 */
public class SmartMeterProtocolMessageAdapter extends AbstractDeviceMessageConverterAdapter {

    public SmartMeterProtocolMessageAdapter(SmartMeterProtocol smartMeterProtocol, MessageAdapterMappingFactory messageAdapterMappingFactory, ProtocolPluggableService protocolPluggableService, IssueService issueService, CollectedDataFactory collectedDataFactory, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        super(messageAdapterMappingFactory, protocolPluggableService, issueService, collectedDataFactory, deviceMessageSpecificationService);
        if (MessageProtocol.class.isAssignableFrom(smartMeterProtocol.getClass())) {
            setMessageProtocol((MessageProtocol) smartMeterProtocol);

            String javaClassName;
            if (smartMeterProtocol instanceof UPLProtocolAdapter) {
                javaClassName = ((UPLProtocolAdapter) smartMeterProtocol).getActualClass().getName();
            } else {
                javaClassName = smartMeterProtocol.getClass().getName();
            }

            Object messageConverter = createNewMessageConverterInstance(getDeviceMessageConverterMappingFor(javaClassName));
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