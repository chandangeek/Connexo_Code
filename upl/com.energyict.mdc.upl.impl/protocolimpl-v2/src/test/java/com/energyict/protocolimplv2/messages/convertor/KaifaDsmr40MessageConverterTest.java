package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm.Kaifa;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link com.energyict.protocolimplv2.messages.convertor.KaifaDsmr40MessageConverter} component.
 *
 * @author khe
 */
public class KaifaDsmr40MessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.Reset_MBus_Client);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Reset_MBus_client Mbus_Serial_Number=\"SIM5555555506301\"> </Reset_MBus_client>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new Kaifa();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new KaifaDsmr40MessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        return "SIM5555555506301";      //MBus serial number
    }
}