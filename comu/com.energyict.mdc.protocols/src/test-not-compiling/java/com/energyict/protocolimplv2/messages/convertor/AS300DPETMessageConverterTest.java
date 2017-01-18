package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.upl.meterdata.Register;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.protocol.api.device.DeviceFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

import com.energyict.smartmeterprotocolimpl.elster.apollo5.AS300DPET;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
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

    private static final int DEVICE_ID = 97;

    @Mock
    private DeviceFactory deviceFactory;
    @Mock
    private DeviceFactoryProvider deviceFactoryProvider;
    @Mock
    private Device device;

    @Before
    public void initializeMocksAndFactories () {
        when(this.deviceFactoryProvider.getDeviceFactory()).thenReturn(this.deviceFactory);
        DeviceFactoryProvider.instance.set(this.deviceFactoryProvider);
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(this.deviceFactory.find(DEVICE_ID)).thenReturn(this.device);

        Register register = mock(Register.class);
        List<RegisterReading> registerReadings = new ArrayList<>(1);

        RegisterReading registerReading = mock(RegisterReading.class);
        when(registerReading.getText()).thenReturn("KeyPair1");
        registerReadings.add(registerReading);

        when(register.getLastTextRegisterReadings(any(Integer.class))).thenReturn(registerReadings);
        when(this.device.getRegisterWithDeviceObisCode(any(ObisCode.class))).thenReturn(register);
    }

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
        AS300DPETMessageConverter messageConverter = spy(new AS300DPETMessageConverter(identificationService));
        // We stub the encode method, cause CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable() is not subject of this test
        doReturn(XMLEncodedActivityCalendar).when(messageConverter).encode(any(Code.class));
        return messageConverter;
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.randomBytesAttributeName:
                return "random";
            case DeviceMessageConstants.deviceListAttributeName:
                return getMockedDeviceGroup();
            default:
                return super.getPropertySpecValue(propertySpec);
        }
    }

    private String getMockedDeviceGroup() {
        return String.valueOf(DEVICE_ID);
    }

}