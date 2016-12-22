package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.AM540;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link com.energyict.protocolimplv2.messages.convertor.Dsmr50MessageConverter} component.
 *
 * @author sva
 * @since 30/10/13 - 14:22
 */
public class Dsmr50MessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_DataTransportAuthenticationKey PlainNewAuthenticationKey=\"key\" NewAuthenticationKey=\"key\"> </Change_DataTransportAuthenticationKey>", messageEntry.getContent());

    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new AM540();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new Dsmr50MessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        return new Password("key");
    }
}