package com.energyict.dlms.protocolimplv2.connection;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.*;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.SecurityContextV2EncryptionHandler;
import com.energyict.dlms.aso.framecounter.RespondingFrameCounterHandler;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;

import java.io.IOException;

/**
 * A DLMSConnection acting as a gateway.
 * Depending on the required securityPolicy, the request and responses will be encrypted or decrypted.
 * <p/>
 * Unlike the V1 SecureConnection, this one will never throw any IOExceptions.
 * In case of errors, a proper ComServer runtime exception is thrown.
 *
 * @author gna, khe
 */
public class SecureConnection implements DLMSConnection, DlmsV2Connection {

    private static final int LOCATION_SECURED_XDLMS_APDU_TAG = 3;

    private final ApplicationServiceObject aso;
    private final DlmsV2Connection transportConnection;

    public SecureConnection(final ApplicationServiceObject aso, final DlmsV2Connection transportConnection) {
        this.aso = aso;
        this.transportConnection = transportConnection;
    }

    /**
     * @return the actual DLMSConnection used for dataTransportation
     */
    private DlmsV2Connection getTransportConnection() {
        return this.transportConnection;
    }

    public void connectMAC() {
        getTransportConnection().connectMAC();
    }

    public void disconnectMAC() {
        getTransportConnection().disconnectMAC();
    }

    public HHUSignOn getHhuSignOn() {
        return getTransportConnection().getHhuSignOn();
    }

    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        return getTransportConnection().getInvokeIdAndPriorityHandler();
    }

    public void setInvokeIdAndPriorityHandler(InvokeIdAndPriorityHandler iiapHandler) {
        getTransportConnection().setInvokeIdAndPriorityHandler(iiapHandler);
    }

    public byte[] sendRawBytes(byte[] data) {
        return getTransportConnection().sendRawBytes(data);
    }

    public long getTimeout() {
        return getTransportConnection().getTimeout();
    }

    public void setTimeout(long timeout) {
        getTransportConnection().setTimeout(timeout);
    }

    public void setRetries(int retries) {
        getTransportConnection().setRetries(retries);
    }

    /**
     * {@inheritDoc}
     */
    public byte[] sendRequest(final byte[] byteRequestBuffer) {
        return sendRequest(byteRequestBuffer, false);
    }

    /**
     * {@inheritDoc}
     */
    public byte[] sendRequest(final byte[] byteRequestBuffer, boolean isAlreadyEncrypted) {
        return secureCommunicate(byteRequestBuffer, isAlreadyEncrypted, true, true);
    }

    /**
     * {@inheritDoc}
     */
    public byte[] readResponseWithRetries(byte[] retryRequest) {
        return this.readResponseWithRetries(retryRequest, false);
    }

    /**
     * {@inheritDoc}
     */
    public byte[] readResponseWithRetries(byte[] byteRequestBuffer, boolean isAlreadyEncrypted) {
        //framecounter out of sync (it was already incremented in sendRequest, so decrement it)
        aso.getSecurityContext().decrementFrameCounter();
        byte[] response = secureCommunicate(byteRequestBuffer, isAlreadyEncrypted, false, true);
        //response sent, increment the framecounter
        aso.getSecurityContext().incFrameCounter();
        return response;
    }

    /**
     * {@inheritDoc}
     */
    public void sendUnconfirmedRequest(final byte[] byteRequestBuffer) {
        secureCommunicate(byteRequestBuffer, false, true, false);
    }

    /**
     * First apply DLMS encryption, authentication and signing, then communicate with the device.
     * <p/>
     * 3 scenario's are possible:
     * - normal case: send the secured request, read, decrypt and return the response
     * - unconfirmed request (no response): only send the secured request, do not wait for a response
     * - invokeId use case: don't send the request, only read and decrypt the response. Sending of (re)tries is handled in the application layer.
     *
     * @param send    whether or not to send out the given request to the meter
     * @param receive whether or not to read a response from the meter
     */
    private byte[] secureCommunicate(byte[] byteRequestBuffer, boolean isAlreadyEncrypted, boolean send, boolean receive) {
        /* dataTransport security is only applied after we made an established association */
        if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {

            /* If no security is applied, then just forward the requests and responses */
            if (this.aso.getSecurityContext().getSecurityPolicy() == SecurityContext.SECURITYPOLICY_NONE) {
                return communicate(byteRequestBuffer, send, receive);
            } else if (byteRequestBuffer[3] == DLMSCOSEMGlobals.COSEM_GENERAL_BLOCK_TRANSFER) {
                // As ComServer doesn't send any content, but only request next blocks, no encryption has to be applied
                // If ComServer would send actual content, then this content should be encrypted!
                return communicate(byteRequestBuffer, send, receive);
            } else {

                // FIXME: Strip the 3 leading bytes before encrypting -> due to old HDLC code
                final byte[] leading = ProtocolUtils.getSubArray(byteRequestBuffer, 0, 2);
                byte[] securedRequest = ProtocolUtils.getSubArray(byteRequestBuffer, 3);

                if (!isAlreadyEncrypted) {     //Don't encrypt the request again if it's already encrypted
                    if (useGeneralGloOrGeneralDedCiphering()) {
                        //General global or general dedicated tags
                        final byte tag = (this.aso.getSecurityContext().getCipheringType() == CipheringType.GENERAL_GLOBAL.getType())
                                ? DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING
                                : DLMSCOSEMGlobals.GENERAL_DEDICATED_CIPTHERING;
                        securedRequest = encryptGeneralGloOrDedCiphering(securedRequest);
                        securedRequest = ParseUtils.concatArray(new byte[]{tag}, securedRequest);
                    } else if (useGeneralCiperhing()) {
                        //General ciphering tag
                        securedRequest = encryptGeneralCiphering(securedRequest);
                        securedRequest = ParseUtils.concatArray(new byte[]{DLMSCOSEMGlobals.GENERAL_CIPHERING}, securedRequest);
                    } else {
                        //Service specific tags
                        final byte tag = XdlmsApduTags.getEncryptedTag(securedRequest[0], this.aso.getSecurityContext().isGlobalCiphering());
                        securedRequest = encrypt(securedRequest);
                        securedRequest = ParseUtils.concatArray(new byte[]{tag}, securedRequest);
                    }
                } else {
                    //No encryption, only increase the frame counter
                    aso.getSecurityContext().incFrameCounter();
                }

                // FIXME: Last step is to add the three leading bytes you stripped in the beginning -> due to old HDLC code
                securedRequest = ProtocolUtils.concatByteArrays(leading, securedRequest);

                // send the encrypted request to the DLMSConnection
                byte[] securedResponse = communicate(securedRequest, send, receive);

                byte[] decryptedResponse = null;
                byte cipheredTag;

                /**
                 * Decrypt the received APDU.
                 * If it turns out the received APDU has an invalid frame counter (not greater than the previous frame counter),
                 * there's 2 possible scenarios depending on the {@link RespondingFrameCounterHandler} implementation of the protocol:
                 *  - either reject the received APDU and read in the next APDU (useful in G3PLC UDP networks)
                 *  - or abort the communication (default behaviour)
                 */
                while (decryptedResponse == null) {
                    try {
                        if (!receive || securedResponse == null) {
                            //Unconfirmed request, exit here.
                            return null;
                        } else {

                            // check if the response tag is know and decrypt the data if necessary
                            cipheredTag = securedResponse[LOCATION_SECURED_XDLMS_APDU_TAG];

                            if (cipheredTag == DLMSCOSEMGlobals.COSEM_EXCEPTION_RESPONSE) {
                                //Return any errors as-is
                                return securedResponse;
                            } else if (cipheredTag == DLMSCOSEMGlobals.COSEM_GENERAL_BLOCK_TRANSFER) {
                                // Return as-is, content can only be decrypted once all blocks are received
                                // and thus should be done by the application layer (~ GeneralBlockTransferHandler)
                                return securedResponse;
                            } else if (XdlmsApduTags.contains(cipheredTag)) {
                                //Service specific ciphering
                                decryptedResponse = decrypt(ProtocolUtils.getSubArray(securedResponse, 3));
                            } else if (cipheredTag == DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING || cipheredTag == DLMSCOSEMGlobals.GENERAL_DEDICATED_CIPTHERING) {
                                //General global/dedicated ciphering
                                decryptedResponse = decryptGeneralGloOrDedCiphering(ProtocolUtils.getSubArray(securedResponse, 3));
                            } else if (cipheredTag == DLMSCOSEMGlobals.GENERAL_CIPHERING) {
                                //General ciphering
                                decryptedResponse = decryptGeneralCiphering(ProtocolUtils.getSubArray(securedResponse, 3));
                            } else {
                                IOException ioException = new IOException("Unknown GlobalCiphering-Tag : " + securedResponse[3]);
                                throw ConnectionCommunicationException.unExpectedProtocolError(ioException);
                            }
                        }
                    } catch (DLMSConnectionException e) {
                        //Received an invalid frame counter (not greater than the previous frame counter)
                        if (e.getReason() == DLMSConnectionException.REASON_CONTINUE_INVALID_FRAMECOUNTER) {
                            getAso().getProtocolLink().getLogger().warning(e.getMessage());
                            decryptedResponse = null;

                            //Try to receive the next APDU and decrypt it.
                            securedResponse = communicate(securedRequest, false, true);
                        } else {
                            throw ConnectionCommunicationException.unExpectedProtocolError(new NestedIOException(e));
                        }
                    }
                }

                // FIXME: Last step is to add the three leading bytes you stripped in the beginning -> due to old HDLC code
                return ProtocolUtils.concatByteArrays(leading, decryptedResponse);
            }
        } else { /* During association request (AARQ and AARE) the request just needs to be forwarded */
            return communicate(byteRequestBuffer, send, receive);
        }
    }

    private boolean useGeneralGloOrGeneralDedCiphering() {
        int cipheringType = this.aso.getSecurityContext().getCipheringType();
        return cipheringType == CipheringType.GENERAL_GLOBAL.getType() || cipheringType == CipheringType.GENERAL_DEDICATED.getType();
    }

    private boolean useGeneralCiperhing() {
        return this.aso.getSecurityContext().getCipheringType() == CipheringType.GENERAL_CIPHERING.getType();
    }

    /**
     * Communicate with the device.
     * 3 scenario's are possible:
     * - normal case: send the secured request, read, decrypt and return the response
     * - unconfirmed request (no response): only send the secured request, do not wait for a response
     * - invokeId use case: don't send the request, only read and decrypt the response. Sending of (re)tries is handled in the application layer.
     *
     * @param send    whether or not to send out the given request to the meter
     * @param receive whether or not to read a response from the meter
     */
    private byte[] communicate(byte[] byteRequestBuffer, boolean send, boolean receive) {
        if (send) {
            if (receive) {
                return getTransportConnection().sendRequest(byteRequestBuffer);
            } else {
                getTransportConnection().sendUnconfirmedRequest(byteRequestBuffer);
                return null;
            }
        } else {
            return getTransportConnection().readResponseWithRetries(byteRequestBuffer);
        }
    }

    //Subclasses can override encryption implementation
    protected byte[] encrypt(byte[] securedRequest) {
        return SecurityContextV2EncryptionHandler.dataTransportEncryption(this.aso.getSecurityContext(), securedRequest);
    }

    protected byte[] decrypt(byte[] securedResponse) throws DLMSConnectionException {
        return SecurityContextV2EncryptionHandler.dataTransportDecryption(this.aso.getSecurityContext(), securedResponse);
    }

    private byte[] encryptGeneralGloOrDedCiphering(byte[] request) {
        return SecurityContextV2EncryptionHandler.dataTransportGeneralGloOrDedEncryption(this.aso.getSecurityContext(), request);
    }

    private byte[] decryptGeneralGloOrDedCiphering(byte[] securedResponse) throws DLMSConnectionException {
        return SecurityContextV2EncryptionHandler.dataTransportGeneralGloOrDedDecryption(this.aso.getSecurityContext(), securedResponse);
    }

    private byte[] encryptGeneralCiphering(byte[] securedRequest) {
        return SecurityContextV2EncryptionHandler.dataTransportGeneralEncryption(this.aso.getSecurityContext(), securedRequest);
    }

    private byte[] decryptGeneralCiphering(byte[] securedResponse) throws DLMSConnectionException {
        return SecurityContextV2EncryptionHandler.dataTransportGeneralDecryption(this.aso.getSecurityContext(), securedResponse);
    }

    public void setHHUSignOn(final HHUSignOn hhuSignOn, final String meterId) {
        getTransportConnection().setHHUSignOn(hhuSignOn, meterId);
    }

    public void setHHUSignOn(final HHUSignOn hhuSignOn, final String meterId, int hhuSignonBaudRateCode) {
        getTransportConnection().setHHUSignOn(hhuSignOn, meterId, hhuSignonBaudRateCode);
    }

    public void setIskraWrapper(final int type) {
        if (getTransportConnection() instanceof DLMSConnection) {
            ((DLMSConnection) getTransportConnection()).setIskraWrapper(type);
        }
    }

    public void setSNRMType(final int type) {
        if (getTransportConnection() instanceof DLMSConnection) {
            ((DLMSConnection) getTransportConnection()).setSNRMType(type);
        }
    }

    public int getMaxRetries() {
        return getTransportConnection().getMaxRetries();
    }

    @Override
    public int getMaxTries() {
        return getMaxRetries() + 1;
    }

    @Override
    public boolean useGeneralBlockTransfer() {
        return getTransportConnection().useGeneralBlockTransfer();
    }

    @Override
    public int getGeneralBlockTransferWindowSize() {
        return getTransportConnection().getGeneralBlockTransferWindowSize();
    }

    @Override
    public void prepareComChannelForReceiveOfNextPacket() {
        getTransportConnection().prepareComChannelForReceiveOfNextPacket();
    }

    public ApplicationServiceObject getAso() {
        return aso;
    }
}