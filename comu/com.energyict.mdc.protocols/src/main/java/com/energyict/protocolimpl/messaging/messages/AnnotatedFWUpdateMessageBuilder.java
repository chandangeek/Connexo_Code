package com.energyict.protocolimpl.messaging.messages;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;
import com.energyict.protocols.messaging.FirmwareUpdateMessageBuilder;
import com.energyict.protocols.messaging.MessageBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7/11/12
 * Time: 1:13 PM
 */
public class AnnotatedFWUpdateMessageBuilder extends FirmwareUpdateMessageBuilder {

    public static final String TAG_FIRMWARE_UPGRADE = "FirmwareUpgrade";
    public static final String ATTR_USER_FILE_ID = "userFileID";
    public static final String ATTR_USER_FILE_CONTENT = "userFileContent";
    public static final String ATTR_URL = "url";

    public AnnotatedFWUpdateMessageBuilder(DeviceMessageFileService deviceMessageFileService) {
        super(deviceMessageFileService);
    }

    @Override
    public String getDescription() {
        final StringBuilder sb = new StringBuilder(TAG_FIRMWARE_UPGRADE);
        sb.append(' ');
        if (getUrl() != null) {
            sb.append(ATTR_URL);
            sb.append("='");
            sb.append(getUrl());
            sb.append("', ");
        }
        if (getDeviceMessageFile() != null) {
            sb.append("UserFile='");
            sb.append(getDeviceMessageFile().getName());
            sb.append('\'');
        }
        return sb.toString();
    }

    public void initFromXml(String xmlString) throws SAXException, IOException {

        System.out.println(xmlString);

        try {
            final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            final Document document = docBuilder.parse(new ByteArrayInputStream(xmlString.getBytes()));
            document.getDocumentElement().normalize();

            final Element element = document.getDocumentElement();
            final String tagName = element.getTagName();
            if (!tagName.equals(TAG_FIRMWARE_UPGRADE)) {
                throw new IOException("Expected tag [" + TAG_FIRMWARE_UPGRADE + "] but received [" + tagName + "]. Could not init from xml!");
            }

        } catch (ParserConfigurationException e) {
            throw new NestedIOException(e);
        }
    }

    @Override
    public AdvancedMessageHandler getMessageHandler(MessageBuilder builder) {
        return new FirmwareUpdateMessageHandler((FirmwareUpdateMessageBuilder) builder, TAG_FIRMWARE_UPGRADE);
    }

}