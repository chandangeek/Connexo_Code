package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageAttributeImpl;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdw.core.DataVaultProvider;
import com.energyict.mdw.core.RandomProvider;
import com.energyict.mdw.crypto.KeyStoreDataVaultProvider;
import com.energyict.mdw.crypto.SecureRandomProvider;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdw.offlineimpl.OfflineDeviceMessageAttributeImpl;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;
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

    public static final int DEVICE_MESSAGE_ID = 1;
    public static final String ATTRIBUTE_VALUE = "000102030405060708090A0B0C0D0E0F";

    @Mock
    private OfflineDeviceMessage keyMessage;
    @Mock
    private OfflineDeviceMessage decommissionMessage;
    @Mock
    private DeviceProtocol deviceProtocol;

    @Before
    public void doBefore() {
        DataVaultProvider.instance.set(new KeyStoreDataVaultProvider());
        RandomProvider.instance.set(new SecureRandomProvider());

        when(deviceProtocol.format(Matchers.any(PropertySpec.class), Matchers.anyObject())).thenReturn(ATTRIBUTE_VALUE);
        keyMessage = createMessage(MBusSetupDeviceMessage.SetEncryptionKeys);
        decommissionMessage = createMessage(MBusSetupDeviceMessage.Decommission);
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
        when(deviceMessage.getId()).thenReturn(DEVICE_MESSAGE_ID);

        for (PropertySpec propertySpec : messageSpec.getPropertySpecs()) {
            TypedProperties propertyStorage = TypedProperties.empty();
            propertyStorage.setProperty(propertySpec.getName(), ATTRIBUTE_VALUE);
            attributes.add(new OfflineDeviceMessageAttributeImpl(new DeviceMessageAttributeImpl(propertySpec, deviceMessage, propertyStorage), deviceProtocol));
        }
        when(message.getDeviceMessageAttributes()).thenReturn(attributes);
        when(message.getSpecification()).thenReturn(messageSpec);
        return message;
    }

    private OfflineDeviceMessage getEmptyMessageMock() {
        OfflineDeviceMessage mock = mock(OfflineDeviceMessage.class);
        when(mock.getTrackingId()).thenReturn("");
        return mock;
    }

}