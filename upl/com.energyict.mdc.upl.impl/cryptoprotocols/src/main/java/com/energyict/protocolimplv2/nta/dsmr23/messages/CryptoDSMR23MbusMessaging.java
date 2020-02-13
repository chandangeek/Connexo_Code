package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.KeyAccessorType;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaMbusDevice;

import java.util.List;

public class CryptoDSMR23MbusMessaging extends Dsmr23MbusMessaging {

    public CryptoDSMR23MbusMessaging(AbstractNtaMbusDevice mbusProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(mbusProtocol, propertySpecService, nlsService, converter, loadProfileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> supportedMessages = super.getSupportedMessages();
        supportedMessages.add(this.get(MBusSetupDeviceMessage.SetEncryptionKeysUsingCryptoserver));
        return supportedMessages;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        if (DeviceMessageConstants.defaultKeyAttributeName.equals(propertySpec.getName())) {
            return getKeyAccessorTypeExtractor().actualValueForHsmKey((KeyAccessorType) messageAttribute).orElse("");
        }
        return super.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }
}
