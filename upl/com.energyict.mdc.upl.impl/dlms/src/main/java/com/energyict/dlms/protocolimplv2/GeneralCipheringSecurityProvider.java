package com.energyict.dlms.protocolimplv2;

import com.energyict.dlms.aso.SecurityContext;

/**
 * Adds additional functionality to the standard SecurityProvider.
 * Specifically, it supports the use of session keys that are used in general ciphering.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 21/01/2016 - 16:06
 */
public interface GeneralCipheringSecurityProvider extends SecurityProvider {

    /**
     * Return a randomly generated key that can be used to encrypt APDUs in a single application association.
     * This is similar to the use of the dedicated key.
     * However, the dedicated key is agreed in the AARQ, while the session key can be renewed for every APDU within an AA.
     */
    byte[] getSessionKey();

    /**
     * Update the cached session key with a new value.
     * This needs to be called when the used session key changes within an existing AA.
     */
    void setSessionKey(byte[] sessionKey);
}