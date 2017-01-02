package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cbo.HexString;
import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link CryptoDsmr23MessageConverter} component.
 *
 * @author sva
 * @since 30/10/13 - 14:22
 */
@RunWith(MockitoJUnitRunner.class)
public class CryptoDsmr23MessageConverterTest extends AbstractMessageConverterTest {

    @Mock
    private TariffCalendarFinder tariffCalendarFinder;
    @Mock
    private TariffCalendarExtractor tariffCalendarExtractor;
    @Mock
    private DeviceMessageFileExtractor deviceMessageFileExtractor;
    @Mock
    private NumberLookupExtractor numberLookupExtractor;
    @Mock
    private LoadProfileExtractor loadProfileExtractor;
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

        offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY.get(propertySpecService, nlsService, converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ServiceKeyAK PreparedData=\"0102030405060708\" Signature=\"0102030405060708\" VerificationKey=\"1\"> </ServiceKeyAK>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY.get(propertySpecService, nlsService, converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ServiceKeyEK PreparedData=\"0102030405060708\" Signature=\"0102030405060708\" VerificationKey=\"1\"> </ServiceKeyEK>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_HLS_SECRET_USING_SERVICE_KEY.get(propertySpecService, nlsService, converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ServiceKeyHLSSecret PreparedData=\"0102030405060708\" Signature=\"0102030405060708\" VerificationKey=\"1\"> </ServiceKeyHLSSecret>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new WebRTUKP(tariffCalendarFinder, tariffCalendarExtractor, deviceMessageFileExtractor);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new CryptoDsmr23MessageConverter(null, propertySpecService, nlsService, converter, loadProfileExtractor, numberLookupExtractor, deviceMessageFileExtractor, tariffCalendarExtractor);
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