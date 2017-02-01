package com.energyict.messaging;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Optional;

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
    private static final String TAG_USERFILE = "UserFileId";
    private final DeviceMessageFileFinder deviceMessageFileFinder;
    private final DeviceMessageFileExtractor deviceMessageFileExtractor;

    private String userFileContent = null;

    public FirmwareUpdateMessageBuilder(DeviceMessageFileFinder deviceMessageFileFinder, DeviceMessageFileExtractor deviceMessageFileExtractor) {
        this.deviceMessageFileFinder = deviceMessageFileFinder;
        this.deviceMessageFileExtractor = deviceMessageFileExtractor;
    }

    protected static String getMessageNodeTag() {
        return MESSAGETAG;
    }

    public String getUserFileContent() {
        return userFileContent;
    }

    protected void setUserFileContent(String userFileContent) {
        this.userFileContent = userFileContent;
    }

    private void setUserFileId(String userFileId) {
        Optional<DeviceMessageFile> deviceMessageFile = deviceMessageFileFinder.from(userFileId);
        if (deviceMessageFile.isPresent()) {
            setUserFileContent(deviceMessageFileExtractor.contents(deviceMessageFile.get()));
        }
    }

    // Parsing the message use SAX
    public AdvancedMessageHandler getMessageHandler(MessageBuilder builder) {
        return new FirmwareUpdateMessageHandler((FirmwareUpdateMessageBuilder) builder, getMessageNodeTag());
    }

    public static class FirmwareUpdateMessageHandler extends AbstractMessageBuilder.AdvancedMessageHandler {

        private FirmwareUpdateMessageBuilder msgBuilder;

        public FirmwareUpdateMessageHandler(FirmwareUpdateMessageBuilder builder, String messageTag) {
            super(messageTag);
            this.msgBuilder = builder;
        }

        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts) throws SAXException {
            super.startElement(namespaceURI, localName, qName, atts);
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (TAG_URL.equals(localName)) {
                //Not used anymore
            }
            if (TAG_USERFILE.equals(localName)) {
                String userFileId = (String) getCurrentValue();
                if (userFileId != null) {
                    this.msgBuilder.setUserFileId(userFileId);
                }
            }

            // We have an included file...
            if (INCLUDED_USERFILE_TAG.equals(localName)) {
                this.msgBuilder.setUserFileContent((String) this.getCurrentValue());
            }
        }
    }
}