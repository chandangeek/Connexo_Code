package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Used by beacon
 * Debug log IC
 * class id = 20004, version = 2, logical name = 0-192:96.128.0.255 (00C0608000FF)
 * This manufacturer-defined COSEM IC allows for controlling the debug logging.
 */
public enum LoggerSettingsMethods implements DLMSClassMethods {
    /**
     * Fetching the debug logging currently stored on the device. Returns a gzip compressed tarball
     * containing the content of the /var/log directory. Since the content returned exceeds
     * the DLMS PDU size, a block transfer is invoked to return this logging. Resulting octet-string
     * contains the logging files.
     */
    FETCH_LOGGING(1, 0x00);


    /** The method number. */
    private final int methodNumber;

    /** The short address. */
    private final int shortAddress;

    /**
     * Create a new instance.
     *
     * @param 	methodNumber		The method number.
     * @param 	shortAddress		The short address.
     */
    private LoggerSettingsMethods(final int methodNumber, final int shortAddress) {
        this.methodNumber = methodNumber;
        this.shortAddress = shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final DLMSClassId getDlmsClassId() {
        return DLMSClassId.LOGGER_SETTINGS;
    }

    /**
     * {@inheritDoc}
     */
    public final int getShortName() {
        return this.shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final int getMethodNumber() {
        return this.methodNumber;
    }
}
