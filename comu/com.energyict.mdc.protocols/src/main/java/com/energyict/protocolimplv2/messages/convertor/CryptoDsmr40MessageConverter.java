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
 * Represents a MessageConverter for the legacy Crypto DSMR4.0 protocols.
 * This is the normal DSMR4.0 message converter, but adds the extra crypto messages.
 *
 * @author khe
 * @since 30/10/13 - 8:33
 */
public class CryptoDsmr40MessageConverter extends Dsmr40MessageConverter {

    @Inject
    public CryptoDsmr40MessageConverter(TopologyService topologyService) {
        super(topologyService);
    }

    @Override
    protected void initializeRegistry(Map<DeviceMessageId, MessageEntryCreator> registry) {
        super.initializeRegistry(registry);
        registry.put(DeviceMessageId.SECURITY_CHANGE_HLS_SECRET_USING_SERVICE_KEY, new MultipleAttributeMessageEntry(RtuMessageConstant.SERVICEKEY_HLSSECRET, RtuMessageConstant.SERVICEKEY_PREPAREDDATA, RtuMessageConstant.SERVICEKEY_SIGNATURE, RtuMessageConstant.SERVICEKEY_VERIFYKEY));
        registry.put(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY, new MultipleAttributeMessageEntry(RtuMessageConstant.SERVICEKEY_AK, RtuMessageConstant.SERVICEKEY_PREPAREDDATA, RtuMessageConstant.SERVICEKEY_SIGNATURE, RtuMessageConstant.SERVICEKEY_VERIFYKEY));
        registry.put(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY, new MultipleAttributeMessageEntry(RtuMessageConstant.SERVICEKEY_EK, RtuMessageConstant.SERVICEKEY_PREPAREDDATA, RtuMessageConstant.SERVICEKEY_SIGNATURE, RtuMessageConstant.SERVICEKEY_VERIFYKEY));
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.preparedDataAttributeName:
            case DeviceMessageConstants.signatureAttributeName:
                return ((HexString) messageAttribute).getContent();
            default:
                return super.format(propertySpec, messageAttribute);
        }
    }

}