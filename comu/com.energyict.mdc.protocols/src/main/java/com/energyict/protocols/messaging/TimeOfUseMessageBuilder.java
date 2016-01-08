package com.energyict.protocols.messaging;

import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.UserFileFactory;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.codetables.CodeFactory;

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

    private String name;
    private Date activationDate;
    private int codeId = 0;
    private int userFileId = 0;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Code code;
    private final CodeFactory codeFactory;

    private UserFile userFile;
    private final UserFileFactory userFileFactory;

    /**
     * Indicates whether to inline the {@link UserFile} or not.
     */
    private boolean inlineUserFiles;

    /**
     * Indicates whether to inline the {@link Code} or not.
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

    public TimeOfUseMessageBuilder(CodeFactory codeFactory, UserFileFactory userFileFactory) {
        this.codeFactory = codeFactory;
        this.userFileFactory = userFileFactory;
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
     * @param code The code to be set
     */
    public void setCode(Code code) {
        if (code != null) {
            this.codeId = code.getId();
        } else {
            this.codeId = 0;
        }
    }

    /**
     * Set the id of the codetable to be used as content for this time of use schedule
     *
     * @param codeId The id of the codetable to be set
     */
    public void setCodeId(int codeId) {
        this.codeId = codeId;
    }

    /**
     * Get the codetable to be used as content for this time of use schedule
     *
     * @return the codetable to be used as content for this time of use schedule
     */
    public Code getCode() {
        if (code == null) {
            this.codeFactory.findCode(codeId);
        }
        return code;
    }

    /**
     * Set the userfile to be used as content for this time of use schedule
     *
     * @param userFile The userFile to be set
     */
    public void setUserFile(UserFile userFile) {
        if (userFile != null) {
            this.userFileId = userFile.getId();
            this.userFile = userFile;
        } else {
            this.userFileId = 0;
        }
    }

    /**
     * Set the id of the userfile to be used as content for this time of use schedule
     *
     * @param userFileId The id of the userFile to be set
     */
    public void setUserFileId(int userFileId) {
        this.userFileId = userFileId;
    }

    /**
     * Get the userfile to be used as content for this time of use schedule
     *
     * @return the userfile to be used as content for this time of use schedule
     */
    public UserFile getUserFile() {
        if (userFile == null) {
            this.userFile = this.userFileFactory.findUserFile(this.userFileId);
        }
        return userFile;
    }

    public static String getMessageNodeTag() {
        return MESSAGETAG;
    }

    /**
     * {@inheritDoc}
     */
    protected String getMessageContent() throws ParserConfigurationException, IOException {
        if ((codeId == 0) && (userFileId == 0)) {
            throw new IllegalArgumentException("Code or user file needed");
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
        if (codeId > 0) {
            addChildTag(builder, TAG_CODE, codeId);
        }
        if (userFileId > 0) {
            if (this.inlineUserFiles) {
                builder.append("<").append(INCLUDED_USERFILE_TAG).append(">");

                // This will generate a message that will make the RtuMessageContentParser inline the file.
                builder.append("<").append(INCLUDE_USERFILE_TAG).append(" ").append(INCLUDE_USERFILE_ID_ATTRIBUTE).append("=\"").append(this.userFileId).append("\"");
                if (isZipMessageContent()) {
                    builder.append(" ").append(CREATEZIP_ATTRIBUTE_TAG).append("=\"true\"");
                } else if (isEncodeB64()) {
                    builder.append(" ").append(ENCODEB64_ATTRIBUTE_TAG).append("=\"true\"");
                }
                builder.append("/>");

                builder.append("</").append(INCLUDED_USERFILE_TAG).append(">");
            } else {
                addChildTag(builder, TAG_USERFILE, userFileId);
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
        if (codeId > 0) {
            buf.append("Code='").append(getCode().getName()).append("', ");
        }
        if (userFileId > 0) {
            buf.append("UserFile='").append(getUserFile().getName()).append("'");
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
                    msgBuilder.setCodeId(id);
                }
            }
            if (TAG_USERFILE.equals(localName)) {
                String userFileId = (String) getCurrentValue();
                if (userFileId != null) {
                    int id = Integer.parseInt(userFileId);
                    msgBuilder.setUserFileId(id);
                }
            }

            // We have an included file...
            if (INCLUDED_USERFILE_TAG.equals(localName)) {
                this.msgBuilder.setUserFile(new IncludedUserFile((String) this.getCurrentValue()));
            }
        }

    }

    public int getCodeId() {
        return codeId;
    }

    public int getUserFileId() {
        return userFileId;
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