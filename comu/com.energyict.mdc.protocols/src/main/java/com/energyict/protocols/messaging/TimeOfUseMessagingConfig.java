package com.energyict.protocols.messaging;

import com.energyict.mdc.protocol.api.UserFile;

/**
 * Gives a summary of all the TimeOfUseMessaging features that are supported by the protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/08/11
 * Time: 11:44
 */
public class TimeOfUseMessagingConfig {

    private boolean needsName;
    private boolean supportsCodeTables;
    private boolean supportsUserFiles;
    private boolean supportsUserFileReferences;
    private boolean supportsCodeTableReferences;
    private boolean zipContent;
    private boolean encodeContentToBase64;

    /**
     * Gives a summary of all the TimeOfUseMessaging features that are supported by the protocol
     *
     * @param needsName                   Indicates whether the protocol needs a 'name' for the tariff calendar or not.
     * @param supportsCodeTables          Indicates whether the tarif calendar data is saved in code table or not.
     * @param supportsUserFiles           Indicates whether the tariff calendar data is saved in a userfile or not.
     * @param supportsUserFileReferences  Indicates whether the implementer supports {@link UserFile} references.
     * @param supportsCodeTableReferences Indicates whether the implementer supports {@link com.energyict.mdw.core.Code} references
     * @param zipContent                  Indicates whether the content of the {@link UserFile} of the {@link com.energyict.mdw.core.Code} must be zipped when it is inlined in the DeviceMessage.
     * @param encodeContentToBase64       Indicate whether the content of the {@link UserFile} or {@link com.energyict.mdw.core.Code} must be Base64 Encoded.
     */
    public TimeOfUseMessagingConfig(boolean needsName, boolean supportsCodeTables, boolean supportsUserFiles, boolean supportsUserFileReferences,
                                    boolean supportsCodeTableReferences, boolean zipContent, boolean encodeContentToBase64) {
        this.needsName = needsName;
        this.supportsCodeTables = supportsCodeTables;
        this.supportsUserFiles = supportsUserFiles;
        this.supportsUserFileReferences = supportsUserFileReferences;
        this.supportsCodeTableReferences = supportsCodeTableReferences;
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
     * Indicates whether the tarif calendar data is saved in code table or not.
     *
     * @return <code>true</code> the tariff calendar data is saved in code table, <code>false</code> if not.
     */
    public boolean supportsCodeTables() {
        return this.supportsCodeTables;
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
     * Indicates whether the implementer supports {@link UserFile} references. This will typically be used by generic protocols, as these have
     * access to the database. If {@link #supportsUserFiles()} is true, and this method returns <code>false</code>, the entire file is sent as payload in
     * the message. If this method returns <code>true</code>, a user file ID is passed on to the implementer, who can then query the database for the
     * contents of the file.
     *
     * @return <code>true</code> if the protocol supports a file ID reference, <code>false</code> if it does not. This can equally be translated into
     *         <code>true</code> for a generic protocol, and <code>false</code> for a normal one.
     */
    public boolean supportsUserFileReferences() {
        return this.supportsUserFileReferences;
    }

    /**
     * Indicates whether the implementer supports {@link com.energyict.mdw.core.Code} references. This will typically be used by generic protocols, as these have
     * access to the database. If {@link #supportsCodeTables()} is true, and this method returns <code>false</code>, the entire file is sent as payload in
     * the message. If this method returns <code>true</code>, a user file ID is passed on to the implementer, who can then query the database for the
     * contents of the file.
     *
     * @return <code>true</code> if the protocol supports a codeTable ID reference, <code>false</code> if it does not. This can equally be translated into
     *         <code>true</code> for a generic protocol, and <code>false</code> for a normal one.
     */
    public boolean supportsCodeTableReferences() {
        return this.supportsCodeTableReferences;
    }

    /**
     * Indicates whether the content of the {@link UserFile} of the {@link com.energyict.mdw.core.Code} must be zipped when it is inlined in the
     * DeviceMessage. This is only taken into account when {@link #supportsCodeTableReferences()} or {@link #supportsUserFileReferences()} is false.</br>
     * <b><u>NOTE:</u> If the content is zipped, then Base64 Encoding is also applied!</b>
     *
     * @return true if the content needs to be zipped, false otherwise
     */
    public boolean zipContent() {
        return this.zipContent;
    }

    /**
     * Indicate whether the content of the {@link UserFile} or {@link com.energyict.mdw.core.Code} must be Base64 Encoded.
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

    public void setSupportsCodeTables(boolean supportsCodeTables) {
        this.supportsCodeTables = supportsCodeTables;
    }

    public void setSupportsUserFiles(boolean supportsUserFiles) {
        this.supportsUserFiles = supportsUserFiles;
    }

    public void setSupportsUserFileReferences(boolean supportsUserFileReferences) {
        this.supportsUserFileReferences = supportsUserFileReferences;
    }

    public void setSupportsCodeTableReferences(boolean supportsCodeTableReferences) {
        this.supportsCodeTableReferences = supportsCodeTableReferences;
    }

    public void setZipContent(boolean zipContent) {
        this.zipContent = zipContent;
    }

    public void setEncodeContentToBase64(boolean encodeContentToBase64) {
        this.encodeContentToBase64 = encodeContentToBase64;
    }
}
