package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.mdw.xml.MdwXmlSerializer;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SecurityMessageXmlConverterTest {

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Converter converter;

    @Test
    public void testXmlSerialization() throws Exception {
        DeviceMessageSpec spec = SecurityMessage.ACTIVATE_DLMS_ENCRYPTION.get(this.propertySpecService, this.nlsService, this.converter);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MdwXmlSerializer serializer = new MdwXmlSerializer(outputStream);
        serializer.writeObject(spec);
        serializer.close();
        assertThat(outputStream.toString()).contains(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION.toString());
    }

    @Test
    public void testXmlDeserialization() throws Exception {
        DeviceMessageSpec spec = SecurityMessage.ACTIVATE_DLMS_ENCRYPTION.get(this.propertySpecService, this.nlsService, this.converter);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MdwXmlSerializer serializer = new MdwXmlSerializer(outputStream);
        serializer.writeObject(spec);
        serializer.close();
        String xml = outputStream.toString();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        XMLDecoder xmlDecoder = new XMLDecoder(inputStream);
        DeviceMessageSpec restored = (DeviceMessageSpec) xmlDecoder.readObject();
        assertThat(restored).isEqualTo(spec);
    }

}