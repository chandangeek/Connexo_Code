package com.energyict.protocolimplv2.nta.esmr50.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaMbusDevice;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.nta.esmr50.common.messages.ESMR50MbusMessaging;

import java.util.List;

public class CryptoESMR50MbusMessaging extends ESMR50MbusMessaging {

    public CryptoESMR50MbusMessaging(AbstractNtaMbusDevice mbusProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(mbusProtocol, propertySpecService, nlsService, converter, loadProfileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> supportedMessages = super.getSupportedMessages();
        supportedMessages.add(this.get(MBusSetupDeviceMessage.SetEncryptionKeysUsingCryptoserver));
        supportedMessages.add(this.get(MBusSetupDeviceMessage.MBUS_TRANSFER_FUAK));
        supportedMessages.add(this.get(MBusSetupDeviceMessage.MBUS_TRANSFER_P2KEY));
        supportedMessages.add(this.get(MBusSetupDeviceMessage.MBUS_ESMR5_FIRMWARE_UPGRADE));
        supportedMessages.add(this.get(MBusSetupDeviceMessage.MBUS_READ_DETAILED_VERSION_INFORMATION_TAG));
        return supportedMessages;
    }
}
