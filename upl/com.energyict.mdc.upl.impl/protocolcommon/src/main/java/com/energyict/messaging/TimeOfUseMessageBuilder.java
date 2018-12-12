package com.energyict.messaging;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Optional;

/**
 * Message builder class responsible of generating and parsing TimeOfUse messages.
 *
 * @author isabelle
 */
public class TimeOfUseMessageBuilder extends AbstractMessageBuilder {

    /**
     * Tag that wraps around an included file.
     */
    protected static final String INCLUDED_USERFILE_TAG = "IncludedFile";

    private static final String MESSAGETAG = "TimeOfUse";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_ACTIVATIONDATE = "activationDate";
    private static final String TAG_CODE = "CodeId";
    private static final String TAG_USERFILE = "UserFileId";

    private final DeviceMessageFileFinder messageFileFinder;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private String calendarId = "";
    private String deviceMessageFileContent = null;

    public TimeOfUseMessageBuilder(DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor) {
        this.messageFileFinder = messageFileFinder;
        this.messageFileExtractor = messageFileExtractor;
    }

    public static String getMessageNodeTag() {
        return MESSAGETAG;
    }

    /**
     * Set the id of the userfile to be used as content for this time of use schedule
     *
     * @param deviceMessageFileId The id of the userFile to be set
     */
    public void setDeviceMessageFileId(String deviceMessageFileId) {
        Optional<DeviceMessageFile> deviceMessageFile = messageFileFinder.from(deviceMessageFileId);
        if (deviceMessageFile.isPresent()) {
            deviceMessageFileContent = messageFileExtractor.contents(deviceMessageFile.get());
        }
    }

    /**
     * Get the userfile to be used as content for this time of use schedule
     *
     * @return the userfile to be used as content for this time of use schedule
     */
    public String getDeviceMessageFileContent() {
        return deviceMessageFileContent;
    }

    /**
     * Set the userfile to be used as content for this time of use schedule
     *
     * @param deviceMessageFileContent The content to be set
     */
    protected void setDeviceMessageFileContent(String deviceMessageFileContent) {
        this.deviceMessageFileContent = deviceMessageFileContent;
    }

    @Override
    public AdvancedMessageHandler getMessageHandler(MessageBuilder builder) {
        return new TimeOfUseMessageHandler((TimeOfUseMessageBuilder) builder, getMessageNodeTag());
    }

    public String getCalendarId() {
        return calendarId;
    }

    /**
     * Set the id of the codetable to be used as content for this time of use schedule
     *
     * @param calendarId The id of the codetable to be set
     */
    protected void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

    private static class TimeOfUseMessageHandler extends AbstractMessageBuilder.AdvancedMessageHandler {

        TimeOfUseMessageBuilder msgBuilder;

        TimeOfUseMessageHandler(TimeOfUseMessageBuilder builder, String messageTag) {
            super(messageTag);
            this.msgBuilder = builder;
        }

        @Override
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts) throws SAXException {
            super.startElement(namespaceURI, localName, qName, atts);
            if (messageTagEncountered()) {
                if (getMessageTag().equals(localName)) {
                    if (atts != null) {
                        // name attribute
                        String name = atts.getValue(namespaceURI, ATTRIBUTE_NAME);
                        if (name != null) {
                            //Not used anymore
                        }
                        // activationDate attribute
                        String activationDate = atts.getValue(namespaceURI, ATTRIBUTE_ACTIVATIONDATE);
                        if (activationDate != null) {
                            //Not used anymore
                        }
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (TAG_CODE.equals(localName)) {
                String codeId = (String) getCurrentValue();
                if (codeId != null) {
                    msgBuilder.setCalendarId(codeId);
                }
            }
            if (TAG_USERFILE.equals(localName)) {
                String userFileId = (String) getCurrentValue();
                if (userFileId != null) {
                    msgBuilder.setDeviceMessageFileId(userFileId);
                }
            }

            // We have an included file...
            if (INCLUDED_USERFILE_TAG.equals(localName)) {
                this.msgBuilder.setDeviceMessageFileContent((String) this.getCurrentValue());
            }
        }
    }
}