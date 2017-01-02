package com.energyict.messaging;

import com.energyict.mdc.upl.messages.legacy.DateFormatter;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.TariffCalendar;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Date;

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
    /**
     * The tag that is used for an include file.
     */
    protected static final String INCLUDE_USERFILE_TAG = "includeFile";

    /**
     * This is an attribute to aforementioned tag indicating the ID of the user file. See RtuMessageContentParser for more details.
     */
    protected static final String INCLUDE_USERFILE_ID_ATTRIBUTE = "fileId";
    /**
     * This is an attribute tag to indicate whether zipping needs to be applied. See RtuMessageContentParser for more details.
     */
    protected static final String CREATEZIP_ATTRIBUTE_TAG = "createZip";
    /**
     * This is an attribute tag to indicate whether base64 encoding needs to be applied. See RtuMessageContentParser for more details.
     */
    protected static final String ENCODEB64_ATTRIBUTE_TAG = "encodeB64";

    private static final String MESSAGETAG = "TimeOfUse";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_ACTIVATIONDATE = "activationDate";
    private static final String TAG_CODE = "CodeId";
    private static final String TAG_USERFILE = "UserFileId";

    private final TariffCalendarFinder calendarFinder;
    private final DeviceMessageFileFinder messageFileFinder;
    private final DateFormatter dateFormatter;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final TariffCalendarExtractor calendarExtractor;
    private String name;
    private Date activationDate;
    private String calendarId = "";
    private String deviceMessageFileId = "";

    private TariffCalendar calendar;
    private DeviceMessageFile deviceMessageFile;

    /**
     * Indicates whether to inline the {@link DeviceMessageFile} or not.
     */
    private boolean inlineUserFiles;
    /**
     * Indicates whether to inline the {@link TariffCalendar} or not.
     */
    private boolean inlineCodeTables;

    /**
     * Indicates whether to zip the inlined content or not
     */
    private boolean zipMessageContent;

    /**
     * Indicate whether the content needs to be Base64 encoded or not
     */
    private boolean encodeB64;

    public TimeOfUseMessageBuilder(TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor, DateFormatter dateFormatter) {
        this.calendarFinder = calendarFinder;
        this.messageFileFinder = messageFileFinder;
        this.dateFormatter = dateFormatter;
        this.messageFileExtractor = messageFileExtractor;
        this.calendarExtractor = calendarExtractor;
    }

    protected  TariffCalendarFinder getCalendarFinder() {
        return calendarFinder;
    }

    protected TariffCalendarExtractor getCalendarExtractor() {
        return calendarExtractor;
    }

    protected DeviceMessageFileExtractor getMessageFileExtractor() {
        return messageFileExtractor;
    }

    /**
     * Set the name for the time of use schedule
     * This name is optional, it should not be used, this depends on the protocol
     *
     * @param name The name to be set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the name of this time of use schedule
     *
     * @return the name of this time of use schedule
     */
    public String getName() {
        return name;
    }

    /**
     * Set the activation date of this time of use schedule
     * On this date the new time of use schedule will be activated
     *
     * @param date The date to be set as activation date
     */
    public void setActivationDate(Date date) {
        this.activationDate = date;
    }

    /**
     * Get the activationDate to be used with this time of use schedule
     *
     * @return the activationDate to be used with this time of use schedule
     */
    public Date getActivationDate() {
        return this.activationDate;
    }

    /**
     * Set the codetable to be used as content for this time of use schedule
     *
     * @param calendar The code to be set
     */
    public void setCalendar(TariffCalendar calendar) {
        if (calendar != null) {
            this.calendarId = calendarExtractor.id(calendar);
        } else {
            this.calendarId = "";
        }
    }

    /**
     * Set the id of the codetable to be used as content for this time of use schedule
     *
     * @param calendarId The id of the codetable to be set
     */
    public void setCalendarId(int calendarId) {
        this.calendarId = Integer.toString(calendarId);
    }

    /**
     * Get the codetable to be used as content for this time of use schedule
     *
     * @return the codetable to be used as content for this time of use schedule
     */
    public TariffCalendar getCalendar() {
        if (calendar == null) {
            calendar = calendarFinder.from(calendarId).orElse(null);
        }
        return calendar;
    }

    /**
     * Set the userfile to be used as content for this time of use schedule
     *
     * @param deviceMessageFile The userFile to be set
     */
    public void setDeviceMessageFile(DeviceMessageFile deviceMessageFile) {
        if (deviceMessageFile != null) {
            this.deviceMessageFileId = messageFileExtractor.id(deviceMessageFile);
            this.deviceMessageFile = deviceMessageFile;
        } else {
            this.deviceMessageFileId = "";
        }
    }

    /**
     * Set the id of the userfile to be used as content for this time of use schedule
     *
     * @param deviceMessageFileId The id of the userFile to be set
     */
    public void setDeviceMessageFileId(String deviceMessageFileId) {
        this.deviceMessageFileId = deviceMessageFileId;
    }

    /**
     * Get the userfile to be used as content for this time of use schedule
     *
     * @return the userfile to be used as content for this time of use schedule
     */
    public DeviceMessageFile getDeviceMessageFile() {
        if (deviceMessageFile == null) {
            deviceMessageFile = messageFileFinder.from(deviceMessageFileId).orElse(null);
        }
        return deviceMessageFile;
    }

    public static String getMessageNodeTag() {
        return MESSAGETAG;
    }

    @Override
    protected String getMessageContent() {
        if ((calendarId.isEmpty()) && (deviceMessageFileId.isEmpty())) {
            throw new IllegalArgumentException("Device message file or calendar needed");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(MESSAGETAG);
        if (name != null) {
            addAttribute(builder, ATTRIBUTE_NAME, name);
        }
        if (activationDate != null) {
            addAttribute(builder, ATTRIBUTE_ACTIVATIONDATE, activationDate.getTime() / 1000);
        }
        builder.append(">");
        if (!calendarId.isEmpty()) {
            addChildTag(builder, TAG_CODE, calendarId);
        }
        if (!deviceMessageFileId.isEmpty()) {
            if (this.inlineUserFiles) {
                builder.append("<").append(INCLUDED_USERFILE_TAG).append(">");

                // This will generate a message that will make the RtuMessageContentParser inline the file.
                builder.append("<").append(INCLUDE_USERFILE_TAG).append(" ").append(INCLUDE_USERFILE_ID_ATTRIBUTE).append("=\"").append(this.deviceMessageFileId).append("\"");
                if (isZipMessageContent()) {
                    builder.append(" ").append(CREATEZIP_ATTRIBUTE_TAG).append("=\"true\"");
                } else if (isEncodeB64()) {
                    builder.append(" ").append(ENCODEB64_ATTRIBUTE_TAG).append("=\"true\"");
                }
                builder.append("/>");

                builder.append("</").append(INCLUDED_USERFILE_TAG).append(">");
            } else {
                addChildTag(builder, TAG_USERFILE, deviceMessageFileId);
            }
        }
        builder.append("</");
        builder.append(MESSAGETAG);
        builder.append(">");
        return builder.toString();
    }

    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder(MESSAGETAG);
        builder.append(" ");
        builder.append("Name='").append(name).append("', ");
        if (activationDate != null) {
            builder.append("ActivationDate='").append(dateFormatter.format(activationDate)).append("', ");
        }
        if (!calendarId.isEmpty()) {
            builder.append("Code='").append(calendarExtractor.name(getCalendar())).append("', ");
        }
        if (!deviceMessageFileId.isEmpty()) {
            builder.append("UserFile='").append(messageFileExtractor.name(this.getDeviceMessageFile())).append("'");
        }

        return builder.toString();
    }

    @Override
    public AdvancedMessageHandler getMessageHandler(MessageBuilder builder) {
        return new TimeOfUseMessageHandler((TimeOfUseMessageBuilder) builder, getMessageNodeTag());
    }

    private static class TimeOfUseMessageHandler extends AbstractMessageBuilder.AdvancedMessageHandler {

        TimeOfUseMessageBuilder msgBuilder;

        TimeOfUseMessageHandler(TimeOfUseMessageBuilder builder, String messageTag) {
            super(messageTag);
            this.msgBuilder = builder;
        }

        @Override
        protected AbstractMessageBuilder getMessageBuilder() {
            return msgBuilder;
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
                            msgBuilder.setName(name);
                        }
                        // activationDate attribute
                        String activationDate = atts.getValue(namespaceURI, ATTRIBUTE_ACTIVATIONDATE);
                        if (activationDate != null) {
                            msgBuilder.setActivationDate(new Date(Long.parseLong(activationDate) * 1000));
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
                    int id = Integer.parseInt(codeId);
                    msgBuilder.setCalendarId(id);
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
                this.msgBuilder.setDeviceMessageFile(new IncludedUserFile((String) this.getCurrentValue()));
            }
        }

    }

    public String getCalendarId() {
        return calendarId;
    }

    public String getDeviceMessageFileId() {
        return deviceMessageFileId;
    }

    public static String getAttributeName() {
        return ATTRIBUTE_NAME;
    }

    public static String getTagCode() {
        return TAG_CODE;
    }

    public static String getTagUserfile() {
        return TAG_USERFILE;
    }

    public static String getAttributeActivationDate() {
        return ATTRIBUTE_ACTIVATIONDATE;
    }

    public boolean isInlineUserFiles() {
        return inlineUserFiles;
    }

    public void setInlineUserFiles(final boolean inlineUserFiles) {
        this.inlineUserFiles = inlineUserFiles;
    }

    public boolean isZipMessageContent() {
        return zipMessageContent;
    }

    public void setZipMessageContent(final boolean zipMessageContent) {
        this.zipMessageContent = zipMessageContent;
    }

    public boolean isEncodeB64() {
        return encodeB64;
    }

    public void setEncodeB64(final boolean encodeB64) {
        this.encodeB64 = encodeB64;
    }

}