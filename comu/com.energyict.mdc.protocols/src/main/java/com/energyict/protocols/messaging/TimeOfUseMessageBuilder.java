/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.messaging;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.protocol.api.DeviceMessageFile;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.SimpleDateFormat;
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

    private final DeviceMessageFileService deviceMessageFileService;
    private String name;
    private Date activationDate;
    private long calendarId = 0;
    private long deviceMessageFileId = 0;
    private DeviceMessageFile deviceMessageFile;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Calendar calendar;
    private final CalendarService calendarService;

    /**
     * Indicates whether to inline the {@link DeviceMessageFile} or not.
     */
    private boolean inlineUserFiles;

    /**
     * Indicates whether to zip the inlined content or not
     */
    private boolean zipMessageContent;
    /**
     * Indicate whether the content needs to be Base64 encoded or not
     */
    private boolean encodeB64;

    public TimeOfUseMessageBuilder(CalendarService calendarService, DeviceMessageFileService deviceMessageFileService) {
        this.calendarService = calendarService;
        this.deviceMessageFileService = deviceMessageFileService;
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
     * Set the Calendar to be used as content for this time of use schedule
     *
     * @param calendar The calendar to be set
     */
    public void setCalendar(Calendar calendar) {
        if (calendar != null) {
            this.calendarId = calendar.getId();
        } else {
            this.calendarId = 0;
        }
    }

    /**
     * Set the id of the codetable to be used as content for this time of use schedule
     *
     * @param calendarId The id of the codetable to be set
     */
    public void setCalendarId(int calendarId) {
        this.calendarId = calendarId;
    }

    /**
     * Get the {@link Calendar} to be used as content for this time of use schedule
     *
     * @return the Calendar
     */
    public Calendar getCalendar() {
        if (this.calendar == null) {
            this.calendar = this.calendarService.findCalendar(this.calendarId).orElse(null);
        }
        return this.calendar;
    }

    /**
     * Set the userfile to be used as content for this time of use schedule
     *
     * @param deviceMessageFile The userFile to be set
     */
    public void setDeviceMessageFile(DeviceMessageFile deviceMessageFile) {
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
            this.deviceMessageFile = this.deviceMessageFileService.findDeviceMessageFile(this.deviceMessageFileId).orElse(null);
        }
        return deviceMessageFile;
    }

    public static String getMessageNodeTag() {
        return MESSAGETAG;
    }

    /**
     * {@inheritDoc}
     */
    protected String getMessageContent() throws ParserConfigurationException, IOException {
        if ((calendarId == 0) && (deviceMessageFileId == 0)) {
            throw new IllegalArgumentException("Calendar or device message file needed");
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
        if (calendarId > 0) {
            addChildTag(builder, TAG_CODE, calendarId);
        }
        if (deviceMessageFileId > 0) {
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

    public String getDescription() {
        StringBuilder buf = new StringBuilder(MESSAGETAG);
        buf.append(" ");
        buf.append("Name='").append(name).append("', ");
        if (activationDate != null) {
            buf.append("ActivationDate='").append(formatter.format(activationDate)).append("', ");
        }
        if (calendarId > 0) {
            // Wanted to change this to Calendar= but was not sure if it was part of an agreed contract with the device firmware
            buf.append("Code='").append(getCalendar().getName()).append("', ");
        }
        if (deviceMessageFileId > 0) {
            buf.append("UserFile='").append(getDeviceMessageFile().getName()).append("'");
        }

        return buf.toString();
    }

    // Parsing the message use SAX

    public AdvancedMessageHandler getMessageHandler(MessageBuilder builder) {
        return new TimeOfUseMessageHandler((TimeOfUseMessageBuilder) builder, getMessageNodeTag());
    }

    public static class TimeOfUseMessageHandler extends AbstractMessageBuilder.AdvancedMessageHandler {

        protected TimeOfUseMessageBuilder msgBuilder;

        public TimeOfUseMessageHandler(TimeOfUseMessageBuilder builder, String messageTag) {
            super(messageTag);
            this.msgBuilder = builder;
        }

        protected AbstractMessageBuilder getMessageBuilder() {
            return msgBuilder;
        }

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

    public long getCalendarId() {
        return calendarId;
    }

    public long getDeviceMessageFileId() {
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