package com.energyict.protocols.messaging;

import com.energyict.mdc.protocol.api.DeviceMessageFile;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

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

    /**
     * The tag that is used for an include file.
     */
    private static final String INCLUDE_USERFILE_TAG = "includeFile";

    /**
     * This is an attribute to aforementioned tag indicating the ID of the user file. See RtuMessageContentParser for more details.
     */
    private static final String INCLUDE_USERFILE_ID_ATTRIBUTE = "fileId";

    private final DeviceMessageFileService deviceMessageFileService;
    private String url;
    private DeviceMessageFile deviceMessageFile;
    private long deviceMessageFileId = 0;

    /**
     * Indicates whether to inline the user files or not.
     */
    private boolean inlineUserFiles;

    public FirmwareUpdateMessageBuilder(DeviceMessageFileService deviceMessageFileService) {
        super();
        this.deviceMessageFileService = deviceMessageFileService;
    }

    /**
     * Tell the builder whether to inline the user files or not.
     *
     * @param inlineUserFiles If <code>true</code>, user files will be inlined. If <code>false</code> just a reference to the file ID will be passed.
     */
    public final void setInlineUserFiles(final boolean inlineUserFiles) {
        this.inlineUserFiles = inlineUserFiles;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Sets the user file.
     *
     * @param deviceMessageFile The user file.
     */
    public final void setDeviceMessageFile(DeviceMessageFile deviceMessageFile) {
        if (deviceMessageFile != null) {
            this.deviceMessageFileId = deviceMessageFile.getId();
            this.deviceMessageFile = deviceMessageFile;
        } else {
            this.deviceMessageFileId = 0;
        }
    }

    public void setDeviceMessageFileId(int deviceMessageFileId) {
        this.deviceMessageFileId = deviceMessageFileId;
    }

    public DeviceMessageFile getDeviceMessageFile() {
        if (deviceMessageFile == null) {
            deviceMessageFile = this.findUserFile(deviceMessageFileId);
        }
        return deviceMessageFile;
    }

    private DeviceMessageFile findUserFile(long deviceMessageFileId) {
        return this.deviceMessageFileService.findDeviceMessageFile(deviceMessageFileId).orElse(null);
    }

    protected static String getMessageNodeTag() {
        return MESSAGETAG;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	protected String getMessageContent() {
        if ((url == null) && (this.getDeviceMessageFile() == null)) {
            throw new IllegalArgumentException("URL or user file needed");
        }

        final StringBuilder builder = new StringBuilder("<").append(MESSAGETAG).append(">");

        if (this.url != null) {
            addChildTag(builder, TAG_URL, this.url);
        }

        if (this.getDeviceMessageFile() != null) {
            if (this.inlineUserFiles) {
                builder.append("<").append(INCLUDED_USERFILE_TAG).append(">");

                // This will generate a message that will make the RtuMessageContentParser inline the file.
                builder.append("<").append(INCLUDE_USERFILE_TAG).append(" ").append(INCLUDE_USERFILE_ID_ATTRIBUTE).append("=\"").append(this.deviceMessageFileId).append("\"/>");

                builder.append("</").append(INCLUDED_USERFILE_TAG).append(">");
            } else {
                addChildTag(builder, TAG_USERFILE, this.deviceMessageFileId);
            }
        }

        builder.append("</").append(MESSAGETAG).append(">");

        return builder.toString();
    }

    public String getDescription() {
        StringBuilder builder = new StringBuilder(MESSAGETAG);
        builder.append(" ");
        if (url != null) {
            builder.append("Url='").append(url).append("', ");
        }
        if (deviceMessageFile != null) {
            builder.append("UserFile='").append(getDeviceMessageFile().getName()).append("', ");
        }
        return builder.toString();
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
            if (TAG_USERFILE.equals(localName)) {
                String userFileId = (String) getCurrentValue();
                if (userFileId != null) {
                    int id = Integer.parseInt(userFileId);
                    msgBuilder.setDeviceMessageFileId(id);
                }
            }

            // We have an included file...
            if (INCLUDED_USERFILE_TAG.equals(localName)) {
                this.msgBuilder.setDeviceMessageFile(new IncludedDeviceMessageFile((String) this.getCurrentValue()));
            }
        }

    }

}