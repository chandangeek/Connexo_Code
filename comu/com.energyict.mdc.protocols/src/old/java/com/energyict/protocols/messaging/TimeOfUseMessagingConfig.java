package com.energyict.protocols.messaging;

import com.elster.jupiter.calendar.Calendar;
import com.energyict.mdc.protocol.api.DeviceMessageFile;

/**
 * Gives a summary of all the TimeOfUseMessaging features that are supported by the protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/08/11
 * Time: 11:44
 */
public class TimeOfUseMessagingConfig {

    private boolean needsName;
    private boolean supportsCalendars;
    private boolean supportsUserFiles;
    private boolean supportsUserFileReferences;
    private boolean supportsCalendarReferences;
    private boolean zipContent;
    private boolean encodeContentToBase64;

    /**
     * Gives a summary of all the TimeOfUseMessaging features that are supported by the protocol
     *
     * @param needsName Indicates whether the protocol needs a 'name' for the tariff calendar or not.
     * @param supportsCalendars Indicates whether the tarif calendar data is saved in code table or not.
     * @param supportsUserFiles Indicates whether the tariff calendar data is saved in a userfile or not.
     * @param supportsUserFileReferences Indicates whether the implementer supports {@link com.energyict.mdc.protocol.api.DeviceMessageFile} references.
     * @param supportsCalendarReferences Indicates whether the implementer supports {@link Calendar} references
     * @param zipContent Indicates whether the content of the DeviceMessageFile of the Code must be zipped when it is inlined in the DeviceMessage.
     * @param encodeContentToBase64 Indicates whether the content of the DeviceMessageFile or Code must be Base64 Encoded.
     */
    public TimeOfUseMessagingConfig(boolean needsName, boolean supportsCalendars, boolean supportsUserFiles, boolean supportsUserFileReferences,
                                    boolean supportsCalendarReferences, boolean zipContent, boolean encodeContentToBase64) {
        this.needsName = needsName;
        this.supportsCalendars = supportsCalendars;
        this.supportsUserFiles = supportsUserFiles;
        this.supportsUserFileReferences = supportsUserFileReferences;
        this.supportsCalendarReferences = supportsCalendarReferences;
        this.zipContent = zipContent;
        this.encodeContentToBase64 = encodeContentToBase64;
    }

    /**
     * Gives a summary of all the TimeOfUseMessaging features that are supported by the protocol
     * This constructor creates a config with every feature disabled
     */
    public TimeOfUseMessagingConfig() {
        this(false, false, false, false, false, false, false);
    }

    /**
     * Indicates whether the protocol needs a 'name' for the tariff calendar or not.
     * For some meter a name needs to be sent to the meter.  If this is necessary the protocol must
     * return true for this method.
     *
     * @return <code>true</code> a 'name' is needed for the tarif calendar, <code>false</code> if not.
     */
    public boolean needsName() {
        return this.needsName;
    }

    /**
     * Indicates whether the tarif calendar data is saved in a Calendar or not.
     *
     * @return <code>true</code> the tariff calendar data is saved in a Calendar, <code>false</code> if not.
     */
    public boolean supportsCalendars() {
        return this.supportsCalendars;
    }

    /**
     * Indicates whether the tariff calendar data is saved in a userfile or not.
     *
     * @return <code>true</code> the tariff calendar data is saved in a userfile, <code>false</code> if not.
     */
    public boolean supportsUserFiles() {
        return this.supportsUserFiles;
    }

    /**
     * Indicates whether the implementer supports {@link DeviceMessageFile} references.
     * This will typically be used by generic protocols, as these have access to the database.
     * If {@link #supportsUserFiles()} is true, and this method returns <code>false</code>,
     * the entire file is sent as payload in the message.
     * If this method returns <code>true</code>, a user file ID is passed on to the implementer,
     * who can then query the database for the contents of the file.
     *
     * @return <code>true</code> if the protocol supports a file ID reference, <code>false</code> if it does not. This can equally be translated into
     *         <code>true</code> for a generic protocol, and <code>false</code> for a normal one.
     */
    public boolean supportsUserFileReferences() {
        return this.supportsUserFileReferences;
    }

    /**
     * Indicates whether the implementer supports {@link Calendar} references.
     * This will typically be used by generic protocols, as these have access to the database.
     * If {@link #supportsCalendars()} is true, and this method returns <code>false</code>,
     * the entire file is sent as payload in the message.
     * If this method returns <code>true</code>, a user file ID is passed on to the implementer,
     * who can then query the database for the contents of the file.
     *
     * @return <code>true</code> if the protocol supports a codeTable ID reference, <code>false</code> if it does not. This can equally be translated into
     *         <code>true</code> for a generic protocol, and <code>false</code> for a normal one.
     */
    public boolean supportsCalendarReferences() {
        return this.supportsCalendarReferences;
    }

    /**
     * Indicates whether the content of the {@link com.energyict.mdc.protocol.api.DeviceMessageFile}
     * of the {@link Calendar} must be zipped when it is inlined in the DeviceMessage.
     * This is only taken into account when {@link #supportsCalendarReferences()} or {@link #supportsUserFileReferences()} is false.</br>
     * <b><u>NOTE:</u> If the content is zipped, then Base64 Encoding is also applied!</b>
     *
     * @return true if the content needs to be zipped, false otherwise
     */
    public boolean zipContent() {
        return this.zipContent;
    }

    /**
     * Indicate whether the content of the
     * {@link com.energyict.mdc.protocol.api.DeviceMessageFile} or the
     * {@link Calendar} must be Base64 Encoded.
     * <b><u>NOTE:</u> Base64 encoding will be automatically applied if {@link #zipContent()} returns true</b>
     *
     * @return true if the content needs to be Base64 encoded, false otherwise
     */
    public boolean encodeContentToBase64() {
        return this.encodeContentToBase64;
    }

    public void setNeedsName(boolean needsName) {
        this.needsName = needsName;
    }

    public void setSupportsCalendars(boolean supportsCalendars) {
        this.supportsCalendars = supportsCalendars;
    }

    public void setSupportsUserFiles(boolean supportsUserFiles) {
        this.supportsUserFiles = supportsUserFiles;
    }

    public void setSupportsUserFileReferences(boolean supportsUserFileReferences) {
        this.supportsUserFileReferences = supportsUserFileReferences;
    }

    public void setSupportsCalendarReferences(boolean supportsCalendarReferences) {
        this.supportsCalendarReferences = supportsCalendarReferences;
    }

    public void setZipContent(boolean zipContent) {
        this.zipContent = zipContent;
    }

    public void setEncodeContentToBase64(boolean encodeContentToBase64) {
        this.encodeContentToBase64 = encodeContentToBase64;
    }

}