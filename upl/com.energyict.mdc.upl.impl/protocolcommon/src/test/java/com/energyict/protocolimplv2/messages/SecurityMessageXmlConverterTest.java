package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.shadow.protocol.task.MessagesTaskShadow;
import com.energyict.mdw.core.DataVault;
import com.energyict.mdw.core.DataVaultProvider;
import com.energyict.mdw.xml.MdwXmlSerializer;
import com.energyict.util.ArrayDiffList;
import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SecurityMessageXmlConverterTest {
    @Mock
    private DataVaultProvider dataVaultProvider;
    @Mock
    private DataVault dataVault;

    @Before
    public void setUp() throws Exception {
        when(dataVaultProvider.getKeyVault()).thenReturn(dataVault);
        DataVaultProvider.instance.set(dataVaultProvider);
    }

    @Test
    public void testXmlSerialization() throws Exception {
        MessagesTaskShadow messagesTaskShadow = new MessagesTaskShadow();
        messagesTaskShadow.setAllCategories(true);
        messagesTaskShadow.setDeviceMessageSpecs(ArrayDiffList.fromOriginal(Arrays.<DeviceMessageSpec>asList(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION)));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MdwXmlSerializer serializer = new MdwXmlSerializer(outputStream);
        serializer.writeObject(messagesTaskShadow);
        serializer.close();
        assertThat(outputStream.toString()).contains(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION.toString());
    }

    @Test
    public void testXmlDeserialization() throws Exception {
        MessagesTaskShadow messagesTaskShadow = new MessagesTaskShadow();
        messagesTaskShadow.setAllCategories(true);
        messagesTaskShadow.setDeviceMessageSpecs(ArrayDiffList.fromOriginal(Arrays.<DeviceMessageSpec>asList(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION)));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MdwXmlSerializer serializer = new MdwXmlSerializer(outputStream);
        serializer.writeObject(messagesTaskShadow);
        serializer.close();
        String xml = outputStream.toString();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        XMLDecoder xmlDecoder = new XMLDecoder(inputStream);
        MessagesTaskShadow reconstructedMessageTaskShadow = (MessagesTaskShadow) xmlDecoder.readObject();
        assertThat(reconstructedMessageTaskShadow.getDeviceMessageSpecs()).containsExactly(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION);
    }
}
