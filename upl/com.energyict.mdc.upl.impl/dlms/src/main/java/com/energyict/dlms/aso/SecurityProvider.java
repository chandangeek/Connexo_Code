package com.energyict.dlms.aso;

import java.io.IOException;

/**
 * The securityProvider is responsible for providing all possible keys.
 * This is the same as the V2 interface (extends), but it adds methods to change the keys.
 * This interface is used by V1 protocols only.
 *
 * @author gna
 * @change 3th nov. 2009 - Added the getNEWLLSSecret method
 * @change 17th oct. 2021 - Added methods to change the used encryption and authentication keys instantly, this should be called after setting new keys to devices that use them instantly.
 * @change 22th oct. 2012 - Added the getNEWAuthenticationKeys method and getNEWEncryptionKeys method, returning a string array of the original and the wrapped key.
 * @Change 23rd oct. 2013 - This now extends the v2 SecurityProvider, which is used for the new DeviceProtocols
 */
public interface SecurityProvider extends com.energyict.dlms.protocolimplv2.SecurityProvider {

    /**
     * @return the NEW HLSSecret for the KeyChange functionality
     * @throws IOException
     */
    byte[] getNEWHLSSecret() throws IOException;

    /**
     * @return the NEW LLSSecret for the KeyChange functionality
     * @throws IOException
     */
    byte[] getNEWLLSSecret() throws IOException;

    /**
     * @return the new GlobalKey for the KeyChange functionality
     * @throws IOException
     */
    byte[] getNEWGlobalKey() throws IOException;

    /**
     * @return the new GlobalKeys (original and wrapped) for the KeyChange functionality
     * @throws IOException
     */
    String[] getNEWGlobalKeys() throws IOException;

    /**
     * @return the new authentication key for the KeyChange functionality
     * @throws IOException
     */
    byte[] getNEWAuthenticationKey() throws IOException;

    /**
     * @return the new authentication key (original and wrapped) for the KeyChange functionality
     * @throws IOException
     */
    String[] getNEWAuthenticationKeys() throws IOException;

    /**
     * A master key shall be present in each COSEM server logical device configured in the system.
     * This key is used for wrapping global keys. The MasterKey should not be transfered during a session.
     *
     * @throws IOException
     */
    byte[] getMasterKey() throws IOException;

    /**
     * Swap the currently used encryption key with the new encryption key (see property)
     * This is necessary after writing a new encryption key to a device that switches the keys immediately.
     * This is not necessary if the device only starts to use the new key in the next association
     *
     * @throws IOException if the key is not correctly filled in
     */
    public void changeEncryptionKey() throws IOException;

    /**
     * Swap the currently used authentication key with the new authentication key (see property)
     * This is necessary after writing a new authentication key to a device that switches the keys immediately.
     * This is not necessary if the device only starts to use the new key in the next association
     *
     * @throws IOException if the key is not correctly filled in
     */
    public void changeAuthenticationKey() throws IOException;
}
