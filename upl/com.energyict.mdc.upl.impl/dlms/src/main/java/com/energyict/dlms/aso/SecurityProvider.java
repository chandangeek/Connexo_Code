package com.energyict.dlms.aso;

import com.energyict.dlms.aso.framecounter.RespondingFrameCounterHandler;

import java.io.IOException;

/**
 * The securityProvider is responsible for providing all possible keys.
 *
 * @author gna
 * @change 3th nov. 2009 - Added the getNEWLLSSecret method
 * @change 17th oct. 2021 - Added methods to change the used encryption and authentication keys instantly, this should be called after setting new keys to devices that use them instantly.
 * @change 22th oct. 2012 - Added the getNEWAuthenticationKeys method and getNEWEncryptionKeys method, returning a string array of the original and the wrapped key.
 */
public interface SecurityProvider {

    /**
     * This carries the challenge for the HLS authentication,
     * for example a random number is used
     *
     * @throws IOException
     */
    byte[] getCallingAuthenticationValue() throws IOException;

    /**
     * A global key which is held by the AssociotionLN/SN object.
     *
     * @throws IOException
     */
    byte[] getHLSSecret() throws IOException;

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
     * A global key is a ciphering key that may be used to cipher xDLMS APDU's,
     * exchanged between the same client and server, in more than one session.
     *
     * @throws IOException
     */
    byte[] getGlobalKey() throws IOException;

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
     * A dedicated key is a ciphering key that is delivered during AA establishment and that may be used in subsequent
     * transmissions to cipher xDLMS APDU's, exchanged between the same client and server, within the same AA.
     * The lifetime of the dedicated key is the same as the lifetime of the AA. The dedicated key can be seen as a session key.
     */
    byte[] getDedicatedKey();

    /**
     * A global key used for additional security in the GMC/GMAC encryption
     * NOTE: this can be the same as the original globalKey
     *
     * @throws IOException
     */
    byte[] getAuthenticationKey() throws IOException;

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

    //	/**
    //	 * @param plainText - the text to encrypt
    //	 * @return the cipherdText
    //	 */
    //	byte[] encrypt(byte[] plainText) throws IOException;
    //
    //	/**
    //	 * @param cipherdText - the encrypted text
    //	 * @return the plainText
    //	 */
    //	byte[] decrypt(byte[] cipherdText) throws IOException;

    /**
     * Construct the content of the responseValue when a Manufacturer Specific encryption algorithm ({@link com.energyict.dlms.aso.AuthenticationTypes#MAN_SPECIFIC_LEVEL}) is applied.
     *
     * @param respondingAuthenticationValue the response value from the meter OR null
     * @return the encrypted Value to send back to the meter
     * @throws java.io.IOException when method is not supported or some of the encryption values aren't correct
     */
    byte[] associationEncryptionByManufacturer(byte[] respondingAuthenticationValue) throws IOException;

    /**
     * @return the initial frameCounter
     */
    long getInitialFrameCounter();

    /**
     * Provide the handler for the receiving frameCounter
     *
     * @param respondingFrameCounterHandler the object which will handle the received frameCounter
     */
    public void setRespondingFrameCounterHandling(RespondingFrameCounterHandler respondingFrameCounterHandler);

    /**
     * @return the used handler for the responding frameCounter
     */
    public RespondingFrameCounterHandler getRespondingFrameCounterHandler();

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
