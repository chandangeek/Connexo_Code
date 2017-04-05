package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm.Kaifa;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link com.energyict.protocolimplv2.messages.convertor.KaifaDsmr40MessageConverter} component.
 *
 * @author khe
 */
@RunWith(MockitoJUnitRunner.class)
public class KaifaDsmr40MessageConverterTest extends AbstractV2MessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.Reset_MBus_Client.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Reset_MBus_client Mbus_Serial_Number=\"SIM5555555506301\"> </Reset_MBus_client>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new Kaifa(calendarFinder, calendarExtractor, deviceMessageFileFinder, deviceMessageFileExtractor, propertySpecService, numberLookupFinder, numberLookupExtractor);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new KaifaDsmr40MessageConverter(getMessagingProtocol(), propertySpecService, this.nlsService, this.converter, this.loadProfileExtractor, numberLookupExtractor, calendarExtractor);
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        return "SIM5555555506301";      //MBus serial number
    }
}