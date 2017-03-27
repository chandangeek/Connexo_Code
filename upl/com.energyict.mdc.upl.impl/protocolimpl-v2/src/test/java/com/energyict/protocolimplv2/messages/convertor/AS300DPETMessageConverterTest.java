package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.Register;
import com.energyict.mdc.upl.properties.DeviceGroup;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.AS300DPET;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link AS300DPETMessageConverter} component.
 *
 * @author sva
 * @since 28/10/13 - 16:15
 */
@RunWith(MockitoJUnitRunner.class)
public class AS300DPETMessageConverterTest extends AS300MessageConverterTest {

    @Test
    public void testAllianderPETMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(SecurityMessage.GENERATE_NEW_PUBLIC_KEY.get(propertySpecService, nlsService, converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GenerateNewPublicKey> </GenerateNewPublicKey>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.GENERATE_NEW_PUBLIC_KEY_FROM_RANDOM.get(propertySpecService, nlsService, converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GenerateNewPublicKey Random 32 bytes (optional)=\"random\"> </GenerateNewPublicKey>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP.get(propertySpecService, nlsService, converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetPublicKeysOfAggregationGroup><Key1>KeyPair1</Key1></SetPublicKeysOfAggregationGroup>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new AS300DPET(deviceMessageFileFinder, deviceMessageFileExtractor, propertySpecService);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        AS300DPETMessageConverter messageConverter = spy(new AS300DPETMessageConverter(getMessagingProtocol(), propertySpecService, nlsService, converter, deviceMessageFileExtractor, calendarExtractor, deviceExtractor, registerExtractor, deviceGroupExtractor));
        // We stub the encode method, cause CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable() is not subject of this test
        doReturn(XMLEncodedActivityCalendar).when(messageConverter).encode(any(TariffCalendar.class));
        return messageConverter;
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.randomBytesAttributeName:
                return "random";
            case DeviceMessageConstants.deviceGroupAttributeName:
                return getMockedDeviceGroup();
            default:
                return super.getPropertySpecValue(propertySpec);
        }
    }

    private DeviceGroup getMockedDeviceGroup() {
        DeviceGroup deviceGroup = mock(DeviceGroup.class);
        Device member = mock(Device.class);
        when(deviceGroupExtractor.members(deviceGroup)).thenReturn(Arrays.asList(member));
        Register register = mock(Register.class);
        when(registerExtractor.lastReading(register)).thenReturn(Optional.of(() -> "KeyPair1"));
        when(deviceExtractor.register(eq(member), any(ObisCode.class))).thenReturn(Optional.of(register));
        return deviceGroup;
    }
}