package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.*;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm.Kaifa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link com.energyict.protocolimplv2.messages.convertor.KaifaDsmr40MessageConverter} component.
 *
 * @author khe
 */
@RunWith(MockitoJUnitRunner.class)
public class KaifaDsmr40MessageConverterTest extends AbstractMessageConverterTest {

    @Mock
    private TariffCalendarFinder calendarFinder;
    @Mock
    private Extractor extractor;
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

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.Reset_MBus_Client.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Reset_MBus_client Mbus_Serial_Number=\"SIM5555555506301\"> </Reset_MBus_client>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new Kaifa(calendarFinder, extractor, this.propertySpecService);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new KaifaDsmr40MessageConverter(null, this.propertySpecService, this.nlsService, this.converter, this.extractor);
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        return "SIM5555555506301";      //MBus serial number
    }
}