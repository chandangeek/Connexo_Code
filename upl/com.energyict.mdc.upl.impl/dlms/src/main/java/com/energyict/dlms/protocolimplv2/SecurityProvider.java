package com.energyict.dlms.protocolimplv2;

import com.energyict.mdc.upl.UnsupportedException;

import com.energyict.dlms.aso.framecounter.RespondingFrameCounterHandler;

import java.io.IOException;

/**
 * The securityProvider is responsible for providing all possible keys and other security related objects, like a frame counter handler.
 * <p/>
 * Use this interface for all new DeviceProtocols (protocolimplv2)
 */
public interface SecurityProvider {

    /**
     * This carries the challenge for the HLS authentication,
     * for example a random number is used
     *
     * @throws UnsupportedException when an unsupported security level is configured
     */
    byte[] getCallingAuthenticationValue() throws UnsupportedException;

    /**
     * A global key which is held by the AssociotionLN/SN object.
     */
    byte[] getHLSSecret();

    /**
     * A global key is a ciphering key that may be used to cipher xDLMS APDU's,
     * exchanged between the same client and server, in more than one session.
     */
    byte[] getGlobalKey();

    /**
     * A dedicated key is a ciphering key that is delivered during AA establishment and that may be used in subsequent
     * transmissions to cipher xDLMS APDU's, exchanged between the same client and server, within the same AA.
     * The lifetime of the dedicated key is the same as the lifetime of the AA. The dedicated key can be seen as a session key.
     */
    byte[] getDedicatedKey();

    /**
     * A global key used for additional security in the GMC/GMAC encryption
     * NOTE: this can be the same as the original globalKey
     */
    byte[] getAuthenticationKey();

    /**
     * Construct the content of the responseValue when a Manufacturer Specific encryption algorithm ({@link com.energyict.dlms.aso.AuthenticationTypes#MAN_SPECIFIC_LEVEL}) is applied.
     *
     * @param respondingAuthenticationValue the response value from the meter OR null
     * @return the encrypted Value to send back to the meter
     * @throws UnsupportedException when method is not supported
     * @throws java.io.IOException  some of the encryption values aren't correct, or something went wrong while encrypting the value
     */
    byte[] associationEncryptionByManufacturer(byte[] respondingAuthenticationValue) throws IOException;

    /**
     * @return the initial frameCounter
     */
    long getInitialFrameCounter();

    void setInitialFrameCounter(long initialFrameCounter);

    /**
     * Provide the handler for the receiving frameCounter
     *
     * @param respondingFrameCounterHandler the object which will handle the received frameCounter
     */
    void setRespondingFrameCounterHandling(RespondingFrameCounterHandler respondingFrameCounterHandler);

    /**
     * @return the used handler for the responding frameCounter
     */
    RespondingFrameCounterHandler getRespondingFrameCounterHandler();

    /**
     * Swap the currently used encryption key with the new encryption key (see property)
     * This is necessary after writing a new encryption key to a device that switches the keys immediately.
     * This is not necessary if the device only starts to use the new key in the next association
     *
     * @throws java.io.IOException if the key is not correctly filled in
     */
    void changeEncryptionKey(byte[] newEncryptionKey) throws IOException;

    /**
     * Swap the currently used authentication key with the new authentication key (see property)
     * This is necessary after writing a new authentication key to a device that switches the keys immediately.
     * This is not necessary if the device only starts to use the new key in the next association
     *
     * @throws java.io.IOException if the key is not correctly filled in
     */
    void changeAuthenticationKey(byte[] newAuthenticationKey) throws IOException;

    /**
     * Swap the currently used master key with the new master key (see property)
     * This is necessary after writing a new master key to a device that switches the keys immediately.
     * This is not necessary if the device only starts to use the new key in the next association
     *
     * @throws java.io.IOException if the key is not correctly filled in
     */
    void changeMasterKey(byte[] newMasterKey) throws IOException;

    /**
     *
     * The only purpose of this master key property should be the creation of a session key (encryption key) when using {@link com.energyict.dlms.GeneralCipheringKeyType.WrappedKeyTypes}
     * For key wrapping, the configuration available on security accessor at device type level, should be used
     * Currently some v1 protocols still depend on this property for doing a key change. Those should be marked as obsolete and v2 version should be used.
     */
    byte[] getMasterKey();
}
