package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.amr.RegisterReading;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.Group;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.AS300DPET;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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

        offlineDeviceMessage = createMessage(SecurityMessage.GENERATE_NEW_PUBLIC_KEY);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GenerateNewPublicKey> </GenerateNewPublicKey>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.GENERATE_NEW_PUBLIC_KEY_FROM_RANDOM);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GenerateNewPublicKey Random 32 bytes (optional)=\"random\"> </GenerateNewPublicKey>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetPublicKeysOfAggregationGroup><Key1>KeyPair1</Key1></SetPublicKeysOfAggregationGroup>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new AS300DPET();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        AS300DPETMessageConverter messageConverter = spy(new AS300DPETMessageConverter());
        // We stub the encode method, cause CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable() is not subject of this test
        doReturn(XMLEncodedActivityCalendar).when(messageConverter).encode(any(Code.class));
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

    private Group getMockedDeviceGroup() {
        Group deviceGroup = mock(Group.class);
        Device member = mock(Device.class);
        Register register = mock(Register.class);
        List<RegisterReading> registerReadings = new ArrayList<>(1);
        List<Device> members = new ArrayList<>(1);

        RegisterReading registerReading = mock(RegisterReading.class);
        when(registerReading.getText()).thenReturn("KeyPair1");
        registerReadings.add(registerReading);

        when(register.getLastXReadings(any(Integer.class))).thenReturn(registerReadings);
        when(member.getRegister(any(ObisCode.class))).thenReturn(register);
        members.add(member);

        doReturn(members).when(deviceGroup).getMembers();
        return deviceGroup;
    }
}