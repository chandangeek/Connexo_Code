package com.energyict.protocols.messaging;

/**
 * Copyrights EnergyICT
 * Date: 17/08/11
 * Time: 10:40
 */
public class FirmwareUpdateMessagingConfig {

    private boolean supportsUrls;
    private boolean supportsUserFiles;
    private boolean supportsUserFileReferences;

    /**
     * This class is needed to describe the capabilities supported by the protocol,
     * and is used to create a fitting interface in EIServer.
     *
     * @param supportsUrls               to indicate support for URLs
     * @param supportsUserFiles          to indicate support for user files
     * @param supportsUserFileReferences to indicate support for user files, by reference (id)
     */
    public FirmwareUpdateMessagingConfig(boolean supportsUrls, boolean supportsUserFiles, boolean supportsUserFileReferences) {
        this.supportsUrls = supportsUrls;
        this.supportsUserFiles = supportsUserFiles;
        this.supportsUserFileReferences = supportsUserFileReferences;
    }

    /**
     * This class is needed to describe the capabilities supported by the protocol.
     * This constructor creates a config with all features disabled
     */
    public FirmwareUpdateMessagingConfig() {
        this(false, false, false);
    }

    /**
     * Indicates whether the protocol supports URL as a mechanism for transferring the firmware image. The idea is that the protocol gets an URL in the message
     * and uses that URL to download the image.
     *
     * @return <code>true</code> if downloading images from an URL is supported, <code>false</code> if not.
     */
    public boolean supportsUrls() {
        return this.supportsUrls;
    }

    /**
     * Indicates whether the protocol supports user files for the transfer of a firmware image.
     * The idea here is that the upgrade image is created in EIServer by means of a {@link com.energyict.mdc.protocol.api.DeviceMessageFile}
     * and is then communicated to the protocol by means of the ID of the file.
     * The protocol subsequently picks it up from the database (supportsUserFileReferences returns <code>true</code>),
     * or it is embedded into the message itself (supportsUserFileReferences returns <code>false</code>).
     *
     * @return <code>true</code> if the implementor supports {@link com.energyict.mdc.protocol.api.DeviceMessageFile}s to store firmware images and communicating them, <code>false</code> if it does not.
     */
    public boolean supportsUserFiles() {
        return this.supportsUserFiles;
    }

    /**
     * Indicates whether the implementor supports user file references.
     * This will typically be used by generic protocols, as these have access to the database.
     * If supportsUserFilesForFirmwareUpdate is true, and this method returns <code>false</code>,
     * the entire file is sent as payload in the message.
     * If this method returns <code>true</code>, a user file ID is passed on to the implementor,
     * who can then query the database for the contents of the file.
     *
     * @return <code>true</code> if the protocol supports a file ID reference, <code>false</code> if it does not. This can equally be translated into
     *         <code>true</code> for a generic protocol, and <code>false</code> for a normal one.
     */
    public boolean supportsUserFileReferences() {
        return this.supportsUserFileReferences;
    }

    public void setSupportsUrls(boolean supportsUrls) {
        this.supportsUrls = supportsUrls;
    }

    public void setSupportsUserFiles(boolean supportsUserFiles) {
        this.supportsUserFiles = supportsUserFiles;
    }

    public void setSupportsUserFileReferences(boolean supportsUserFileReferences) {
        this.supportsUserFileReferences = supportsUserFileReferences;
    }

}