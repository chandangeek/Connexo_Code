package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.common.TimeDuration;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.protocolimpl.dlms.Z3.DLMSZ3Messaging;
import com.energyict.protocols.messaging.LegacyMessageConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigDecimal;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;
import static org.junit.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link ModbusMessageConverter} component.
 *
 * @author khe
 * @since 24/10/13 - 10:50
 */
@RunWith(MockitoJUnitRunner.class)
public class DLMSZ3MessagingMessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() throws IOException {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<connectLoad Digital_output=\"1\"> </connectLoad>", messageEntry.getContent());


        offlineDeviceMessage = createMessage(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_PARAMETERS_Z3);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Configure_load_limiting Read_frequency=\"2\" Threshold=\"2\" Duration=\"10\" Digital_Output1_Invert=\"1\" Digital_Output2_Invert=\"1\" Activate_now=\"1\"> </Configure_load_limiting>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new DLMSZ3Messaging();
    }

    protected LegacyMessageConverter doGetMessageConverter() {
        return new DLMSZ3MessagingMessageConverter();
    }

    /**
     * Gets the value to use for the given {@link PropertySpec}
     */
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        if (propertySpec.getName().equals(digitalOutputAttributeName)) {
            return "1";
        } else if (propertySpec.getName().equals(readFrequencyInMinutesAttributeName)) {
            return new TimeDuration(120);
        } else if (propertySpec.getName().equals(overThresholdDurationAttributeName)) {
            return new TimeDuration(10);
        } else if (propertySpec.getName().equals(normalThresholdAttributeName)) {
            return new BigDecimal(2);
        }
        return Boolean.TRUE;
    }
}