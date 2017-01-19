package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.Formatter;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cbo.HexString;
import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link com.energyict.protocolimplv2.messages.convertor.CryptoDsmr40MessageConverter} component.
 *
 * @author sva
 * @since 30/10/13 - 14:22
 */
@RunWith(MockitoJUnitRunner.class)
public class CryptoDsmr40MessageConverterTest extends AbstractMessageConverterTest {

    @Mock
    private TariffCalendarFinder calendarFinder;
    @Mock
    private Extractor extractor;
    @Mock
    private DeviceMessageFileFinder messageFileFinder;
    @Mock
    private Formatter dateFormatter;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Converter converter;

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ServiceKeyAK PreparedData=\"0102030405060708\" Signature=\"0102030405060708\" VerificationKey=\"1\"> </ServiceKeyAK>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ServiceKeyEK PreparedData=\"0102030405060708\" Signature=\"0102030405060708\" VerificationKey=\"1\"> </ServiceKeyEK>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_HLS_SECRET_USING_SERVICE_KEY.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ServiceKeyHLSSecret PreparedData=\"0102030405060708\" Signature=\"0102030405060708\" VerificationKey=\"1\"> </ServiceKeyHLSSecret>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new E350(this.calendarFinder, this.messageFileFinder, this.extractor);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new CryptoDsmr40MessageConverter(null, this.propertySpecService, this.nlsService, this.converter, this.extractor);
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