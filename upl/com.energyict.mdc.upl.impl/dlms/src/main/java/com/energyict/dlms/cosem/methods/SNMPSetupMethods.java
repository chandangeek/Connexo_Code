package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * SNMP setup IC, methods
 * class id = 20032, version = 0, logical name = 0-128:96.194.0.255 (008060C200FF)
 * The manufacturer-specific SNMP setup IC allows configuring the SNMP agent running on the device.
 * This includes both upstream trap eventing and MIB querying. Changes are applied after closing the DLMS association, since these force the SNMP agent to restart.
 */
public enum SNMPSetupMethods implements DLMSClassMethods {

    /**
     * Changes the community string and securityName of an SNMP user profile.
     * request_data ::= structure
     * {
     *   user_profile snmp_user_enum, -- User profile to be changed
     *   user_name utf8-string -- New securityName
     * }
     * response_data ::= integer(0)
     */
    CHANGE_USER_NAME(1, 0x01),

    /**
     * Changes the authentication and privacy passphrases of an SNMP user profile. The public
     * user profile does not hold any credentials, and therefore cannot be used in this method.
     * request_data ::= structure
     * {
     *   user_profile snmp_user_enum, -- SNMP user profile to be changed
     *   priv_passphrase utf8-string, -- USM privacy passphrase
     *   auth_passphrase utf8-string -- USM authentication passphrase
     * }
     * response_data ::= integer(0)
     */
    CHANGE_USER_PASSPHRASES(2, 0x02),

    /**
     * Enables/disables an SNMP user profile.
     * request_data ::= structure
     * {
     *   user_profile snmp_user_enum, -- User profile to be changed
     *   user_state boolean -- True if SNMP user should be enabled,
     *                      -- false if disabled
     * }
     * response_data ::= integer(0)
     */
    ENABLE_USER(3, 0x03);


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
    private SNMPSetupMethods(final int methodNumber, final int shortAddress) {
        this.methodNumber = methodNumber;
        this.shortAddress = shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final DLMSClassId getDlmsClassId() {
        return DLMSClassId.SNMP_SETUP;
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
