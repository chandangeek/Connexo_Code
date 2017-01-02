package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.common.HexString;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.messaging.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

import com.energyict.mdc.protocol.api.messaging.Messaging;

import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link com.energyict.protocolimplv2.messages.convertor.CryptoDsmr23MessageConverter} component.
 *
 * @author khe
 * @since 30/10/13 - 14:22
 */
public class CryptoDsmr23MBusMessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.SetEncryptionKeysUsingCryptoserver);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Set_Encryption_keys_using_Cryptoserver MBus_Default_Key=\"0102030405060708090A0B0C0D0E0F\"> </Set_Encryption_keys_using_Cryptoserver>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new MbusDevice();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new CryptoDsmr23MBusMessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.defaultKeyAttributeName:
                return new HexString("0102030405060708090A0B0C0D0E0F");
            default:
                return "1";
        }
    }
}