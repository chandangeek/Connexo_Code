package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.MbusDevice;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.openKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.transferKeyAttributeName;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Copyrights EnergyICT
 * Date: 14/10/13
 * Time: 10:08
 * Author: khe
 */
@RunWith(MockitoJUnitRunner.class)
public class SetMBusEncryptionKeysMessageEntryTest {

    public static final long DEVICE_MESSAGE_ID = 1;
    public static final String ATTRIBUTE_VALUE = "000102030405060708090A0B0C0D0E0F";

    @Mock
    private OfflineDeviceMessage keyMessage;
    @Mock
    private OfflineDeviceMessage decommissionMessage;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private PropertySpecService propertySpecServicep;
    @Mock
    private NlsService nlsService;
    @Mock
    private Converter converter;

    @Before
    public void doBefore() {

        when(deviceProtocol.format(Matchers.any(OfflineDevice.class), Matchers.any(OfflineDeviceMessage.class), Matchers.any(PropertySpec.class), Matchers.anyObject())).thenReturn(ATTRIBUTE_VALUE);
        keyMessage = createMessage(MBusSetupDeviceMessage.SetEncryptionKeys.get(propertySpecServicep, nlsService, converter));
        decommissionMessage = createMessage(MBusSetupDeviceMessage.Decommission.get(propertySpecServicep, nlsService, converter));
    }

    @Test
    public void testMBusMessages() {
        MbusDevice messagingProtocol = new MbusDevice();

        MessageEntryCreator messageEntryConverter = new SetMBusEncryptionKeysMessageEntry(openKeyAttributeName, transferKeyAttributeName);
        MessageEntry messageEntry = messageEntryConverter.createMessageEntry(messagingProtocol, keyMessage);
        assertEquals("<Set_Encryption_keys Open_Key_Value=\"000102030405060708090A0B0C0D0E0F\" Transfer_Key_Value=\"000102030405060708090A0B0C0D0E0F\"> </Set_Encryption_keys>", messageEntry.getContent());

        messageEntryConverter = new OneTagMessageEntry(RtuMessageConstant.MBUS_DECOMMISSION);
        messageEntry = messageEntryConverter.createMessageEntry(messagingProtocol, decommissionMessage);
        assertEquals("<Decommission/>", messageEntry.getContent());
    }

    /**
     * Create a device message based on the given spec, and fill its attributes with "1" values.
     */
    private OfflineDeviceMessage createMessage(DeviceMessageSpec messageSpec) {
        OfflineDeviceMessage message = getEmptyMessageMock();
        List<OfflineDeviceMessageAttribute> attributes = new ArrayList<>();
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getMessageId()).thenReturn(DEVICE_MESSAGE_ID);

        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        for (PropertySpec propertySpec : messageSpec.getPropertySpecs()) {
            OfflineDeviceMessageAttribute offlineDeviceMessageAttribute = mock(OfflineDeviceMessageAttribute.class);
            when(offlineDeviceMessageAttribute.getName()).thenReturn(propertySpec.getName());
            when(offlineDeviceMessageAttribute.getValue()).thenReturn(ATTRIBUTE_VALUE);
            when(offlineDeviceMessageAttribute.getDeviceMessageId()).thenReturn(DEVICE_MESSAGE_ID);

            attributes.add(offlineDeviceMessageAttribute);
        }

        doReturn(attributes).when(message).getDeviceMessageAttributes();
        when(message.getSpecification()).thenReturn(messageSpec);
        return message;
    }

    private OfflineDeviceMessage getEmptyMessageMock() {
        OfflineDeviceMessage mock = mock(OfflineDeviceMessage.class);
        when(mock.getTrackingId()).thenReturn("");
        return mock;
    }

}