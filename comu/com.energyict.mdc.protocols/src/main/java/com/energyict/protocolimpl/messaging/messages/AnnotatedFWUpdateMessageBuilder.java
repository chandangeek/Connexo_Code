package com.energyict.protocolimpl.messaging.messages;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.NestedIOException;
import com.energyict.mdw.shadow.OldDeviceMessageShadow;
import com.energyict.protocol.messaging.FirmwareUpdateMessageBuilder;
import com.energyict.protocol.messaging.MessageBuilder;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

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

    @Override
    public OldDeviceMessageShadow build() throws BusinessException {
        final OldDeviceMessageShadow shadow = new OldDeviceMessageShadow();
        shadow.setTrackingId(getTrackingId());
        shadow.setReleaseDate(getReleaseDate());
        shadow.setContents(getCustomMessageContent());
        return shadow;
    }

    /**
     * Create an XML string that matches the format of an {@link FirmwareUpdateMessage}
     *
     * @return The xml string
     * @throws com.energyict.cbo.BusinessException
     */
    private final String getCustomMessageContent() throws BusinessException {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.newDocument();

            final Element root = document.createElement(TAG_FIRMWARE_UPGRADE);
            if (getUrl() != null) {
                root.setAttribute(ATTR_URL, getUrl());
            }

            if (getUserFile() != null) {
                root.setAttribute(ATTR_USER_FILE_ID, String.valueOf(getUserFile().getId()));
                byte[] contents = getUserFile().loadFileInByteArray();
                root.setAttribute(ATTR_USER_FILE_CONTENT, contents != null ? ProtocolTools.getHexStringFromBytes(contents, "") : "");
            }

            document.appendChild(root);

            return getXmlWithoutDocType(document);
        } catch (ParserConfigurationException e) {
            throw new BusinessException(e);
        }
    }

    /**
     * Prints the document to a {@link String}, without the docType (this way we can put it in the RtuMessage)
     *
     * @param doc the {@link org.w3c.dom.Document} to converted
     * @return the XML String from the Document
     */
    private final String getXmlWithoutDocType(Document doc) throws BusinessException {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            try {
                StreamResult result = new StreamResult(new StringWriter());
                DOMSource source = new DOMSource(doc);
                transformer.transform(source, result);
                String codeTableXml = result.getWriter().toString();
                int index = codeTableXml.indexOf("?>");
                return (index != -1) ? codeTableXml.substring(index + 2) : codeTableXml;
            } catch (TransformerException e) {
                throw new BusinessException(e);
            }
        } catch (TransformerConfigurationException e) {
            throw new BusinessException(e);
        }
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
        if (getUserFile() != null) {
            sb.append("UserFile='");
            sb.append(getUserFile().getName());
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
