package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.common.HexString;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;

import javax.inject.Inject;
import java.util.Map;

/**
 * Represents a MessageConverter for the legacy Crypto DSM2.3 MBusDevice protocols.
 * This is the normal DSMR2.3 MBusDevice message converter, but adds the extra crypto message.
 *
 * @author khe
 * @since 30/10/13 - 8:33
 */
public class CryptoDsmr23MBusMessageConverter extends Dsmr23MBusDeviceMessageConverter {

    @Inject
    public CryptoDsmr23MBusMessageConverter(TopologyService topologyService) {
        super(topologyService);
    }

    @Override
    protected void initializeRegistry(Map<DeviceMessageId, MessageEntryCreator> registry) {
        super.initializeRegistry(registry);
        registry.put(DeviceMessageId.MBUS_SETUP_SET_ENCRYPTION_KEYS, new MultipleAttributeMessageEntry(RtuMessageConstant.CRYPTOSERVER_MBUS_ENCRYPTION_KEYS, RtuMessageConstant.MBUS_DEFAULT_KEY));
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.defaultKeyAttributeName:
                return ((HexString) messageAttribute).getContent();
            default:
                return super.format(propertySpec, messageAttribute);
        }
    }

}