package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.KeyAccessorType;

import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link com.energyict.protocolimplv2.messages.convertor.CryptoDsmr23MessageConverter} component.
 *
 * @author khe
 * @since 30/10/13 - 14:22
 */
public class CryptoDsmr23MBusMessageConverterTest extends AbstractV2MessageConverterTest {

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
        return new MbusDevice(propertySpecService, calendarFinder, calendarExtractor, deviceMessageFileExtractor, deviceMessageFileFinder, numberLookupFinder, numberLookupExtractor);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new CryptoDsmr23MBusMessageConverter(propertySpecService, nlsService, converter, loadProfileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.defaultKeyAttributeName:
                KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
                when(keyAccessorTypeExtractor.passiveValueContent(keyAccessorType)).thenReturn("0102030405060708090A0B0C0D0E0F");
                return keyAccessorType;
            default:
                return "1";
        }
    }
}