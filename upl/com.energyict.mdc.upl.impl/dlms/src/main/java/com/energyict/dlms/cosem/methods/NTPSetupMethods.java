package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * NTP setup IC, methods
 * class id = 100, version = 0, logical name = 0-0:25.10.0.255 (0000190A00FF)
 * Instances of the "NTP setup" IC allow setting up time synchronisation using the NTP protocol as specified in RFC 5905.
 * One or several instances may be configured to support multiple time servers.
 */
public enum NTPSetupMethods implements DLMSClassMethods {

    /**
     * Synchronizes the time of the DLMS server with the NTP server. This method blocks until either
     * the NTP server responds or until timeout. Make sure a valid DNS server has been configured
     * in case the server_address points to an FQDN.
     * request_data ::= integer(0)
     * response_data ::= <empty>
     */
    SYNCHRONIZE(1, 0x01),

    /**
     * Adds a new symmetric authentication key to authentication key array.
     * If the key ID overlaps  with an existing key, the key is replaced.
     * request_data ::= structure
     * {
     *   key_id: double-long-unsigned,
     *   key: octet-string
     * }
     * response_data ::= <empty>
     */
    ADD_AUTHENTICATION_KEY(2, 0x02),

    /**
     * Deletes a symmetric authentication key from the key array.
     * The key to be deleted is identified by its key_id.
     * request_data ::= double-long-unsigned
     * response_data ::= <empty>
     */
    DELETE_AUTHENTICATION_KEY(3, 0x03);


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
    private NTPSetupMethods(final int methodNumber, final int shortAddress) {
        this.methodNumber = methodNumber;
        this.shortAddress = shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final DLMSClassId getDlmsClassId() {
        return DLMSClassId.NTP_SETUP;
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
