package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.KeyAccessorType;

import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.AM540;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link com.energyict.protocolimplv2.messages.convertor.Dsmr50MessageConverter} component.
 *
 * @author sva
 * @since 30/10/13 - 14:22
 */
public class Dsmr50MessageConverterTest extends AbstractV2MessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_DataTransportAuthenticationKey NewAuthenticationKey=\"key\"> </Change_DataTransportAuthenticationKey>", messageEntry.getContent());

    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new AM540(propertySpecService, calendarFinder, calendarExtractor, deviceMessageFileFinder, deviceMessageFileExtractor, numberLookupFinder, numberLookupExtractor);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new Dsmr50MessageConverter(propertySpecService, nlsService, converter, loadProfileExtractor, numberLookupExtractor, calendarExtractor, keyAccessorTypeExtractor);
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        if (propertySpec.getName().equals(DeviceMessageConstants.newAuthenticationKeyAttributeName)) {
            KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
            when(keyAccessorTypeExtractor.passiveValueContent(keyAccessorType)).thenReturn("key");
            return keyAccessorType;
        }
        return "key";
    }
}