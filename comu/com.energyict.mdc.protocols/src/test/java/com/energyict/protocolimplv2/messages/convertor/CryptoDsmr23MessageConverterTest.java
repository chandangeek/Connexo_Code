package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.common.HexString;
import com.energyict.mdc.protocol.dynamic.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdc.protocol.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.device.data.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link CryptoDsmr23MessageConverter} component.
 *
 * @author sva
 * @since 30/10/13 - 14:22
 */
public class CryptoDsmr23MessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ServiceKeyAK PreparedData=\"0102030405060708\" Signature=\"0102030405060708\" VerificationKey=\"1\"> </ServiceKeyAK>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ServiceKeyEK PreparedData=\"0102030405060708\" Signature=\"0102030405060708\" VerificationKey=\"1\"> </ServiceKeyEK>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_HLS_SECRET_USING_SERVICE_KEY);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ServiceKeyHLSSecret PreparedData=\"0102030405060708\" Signature=\"0102030405060708\" VerificationKey=\"1\"> </ServiceKeyHLSSecret>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new WebRTUKP();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new CryptoDsmr23MessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.preparedDataAttributeName:
            case DeviceMessageConstants.signatureAttributeName:
                return new HexString("0102030405060708");
            default:
                return "1";
        }
    }
}