package com.energyict.protocols.messaging;

import com.energyict.protocols.util.TempFileLoader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Message builder class responsible of generating and parsing FirmwareUpdate messages.
 *
 * @author alex
 */
public class FirmwareUpdateMessageBuilder extends AbstractMessageBuilder {

    /**
     * Tag that wraps around an included file.
     */
    private static final String INCLUDED_USERFILE_TAG = "IncludedFile";

    private static final String MESSAGETAG = "FirmwareUpdate";
    private static final String TAG_URL = "Url";

    private String url;
    private String path;

    public FirmwareUpdateMessageBuilder() {
        super();
    }

    protected static String getMessageNodeTag() {
        return MESSAGETAG;
    }

    public static MessageBuilder fromXml(String xmlString) throws SAXException, IOException {
        MessageBuilder builder = new FirmwareUpdateMessageBuilder();
        builder.initFromXml(xmlString);
        return builder;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public byte[] getFirmwareBytes() throws IOException {
        return TempFileLoader.loadTempFile(getPath());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getMessageContent() {
        return null;
    }

    // Parsing the message use SAX

    public String getDescription() {
        StringBuilder builder = new StringBuilder(MESSAGETAG);
        builder.append(" ");
        if (url != null) {
            builder.append("Url='").append(url).append("', ");
        }
        if (path != null) {
            builder.append("Path='").append(getPath()).append("', ");
        }
        return builder.toString();
    }

    public AdvancedMessageHandler getMessageHandler(MessageBuilder builder) {
        return new FirmwareUpdateMessageHandler((FirmwareUpdateMessageBuilder) builder, getMessageNodeTag());
    }

    public static class FirmwareUpdateMessageHandler extends AbstractMessageBuilder.AdvancedMessageHandler {

        private FirmwareUpdateMessageBuilder msgBuilder;

        public FirmwareUpdateMessageHandler(FirmwareUpdateMessageBuilder builder, String messageTag) {
            super(messageTag);
            this.msgBuilder = builder;
        }

        protected AbstractMessageBuilder getMessageBuilder() {
            return msgBuilder;
        }

        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts) throws SAXException {
            super.startElement(namespaceURI, localName, qName, atts);
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (TAG_URL.equals(localName)) {
                msgBuilder.setUrl((String) getCurrentValue());
            }

            // We have an included path to a temporary file
            if (INCLUDED_USERFILE_TAG.equals(localName)) {
                this.msgBuilder.setPath((String) this.getCurrentValue());
            }
        }
    }
}