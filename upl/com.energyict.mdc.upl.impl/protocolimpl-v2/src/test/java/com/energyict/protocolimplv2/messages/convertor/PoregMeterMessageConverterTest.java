package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimpl.din19244.poreg2.Poreg2;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.enums.DSTAlgorithm;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.dstEndAlgorithmAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.dstStartAlgorithmAttributeName;
import static org.junit.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link com.energyict.protocolimplv2.messages.convertor.ModbusMessageConverter} component.
 *
 * @author khe
 * @since 24/10/13 - 10:50
 */
@RunWith(MockitoJUnitRunner.class)
public class PoregMeterMessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() throws IOException {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(ClockDeviceMessage.SetEndOfDSTWithoutHour);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<EndOfDST Month=\"1\" Day of month=\"1\" Day of week=\"1\"> </EndOfDST>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ClockDeviceMessage.SetDSTAlgorithm);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Algorithms Start Algorithm=\"7\" End Algorithm=\"7\"> </Algorithms>", messageEntry.getContent());



    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new Poreg2();
    }

    protected LegacyMessageConverter doGetMessageConverter() {
        return new PoregMeterMessageConverter();
    }

    /**
     * Gets the value to use for the given {@link com.energyict.cpo.PropertySpec}
     */
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        if (propertySpec.getName().equals(dstStartAlgorithmAttributeName) || propertySpec.getName().equals(dstEndAlgorithmAttributeName)) {
            return DSTAlgorithm.monthlywd.getDescription();
        }
        return "1";     //All other attribute values are "1"
    }
}