package com.energyict.dlms.protocolimplv2.connection;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.XdlmsApduTags;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.SecurityContextV2EncryptionHandler;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimplv2.MdcManager;

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

    public byte[] sendRawBytes(byte[] data) {
        return getTransportConnection().sendRawBytes(data);
    }

    public void setTimeout(long timeout) {
        getTransportConnection().setTimeout(timeout);
    }

    public long getTimeout() {
        return getTransportConnection().getTimeout();
    }

    public void setRetries(int retries) {
        getTransportConnection().setRetries(retries);
    }

    /**
     * The sendRequest will check the current securitySuite to encrypt or authenticate the data and then parse the APDU to the DLMSConnection.
     * The response from the meter is decrypted before sending it back to the object.
     * <p/>
     * If the request is already encrypted (indicated by the boolean), there's no need to encrypt it again in the sendRequest() method
     *
     * @param byteRequestBuffer - The unEncrypted/authenticated request
     * @return the unEncrypted response from the device
     */
    public byte[] sendRequest(final byte[] byteRequestBuffer, boolean isAlreadyEncrypted) {

        /* dataTransport security is only applied after we made an established association */
        if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {

            /* If no security is applied, then just forward the requests and responses */
            if (this.aso.getSecurityContext().getSecurityPolicy() == SecurityContext.SECURITYPOLICY_NONE) {
                return getTransportConnection().sendRequest(byteRequestBuffer);
            } else if (byteRequestBuffer[3] == DLMSCOSEMGlobals.COSEM_GENERAL_BLOCK_TRANSFER) {
                return getTransportConnection().sendRequest(byteRequestBuffer); // As ComServer doesn't send any content, but only request next blocks, no encryption has to be applied
            } else {                                                            // If ComServer would send actual content, then this content should be encrypted!

                // FIXME: Strip the 3 leading bytes before encrypting -> due to old HDLC code
                final byte[] leading = ProtocolUtils.getSubArray(byteRequestBuffer, 0, 2);
                byte[] securedRequest = ProtocolUtils.getSubArray(byteRequestBuffer, 3);

                if (!isAlreadyEncrypted) {     //Don't encrypt the request again if it's already encrypted
                    if (useGeneralGloOrGeneralDedCiphering(this.aso.getSecurityContext().getCipheringType())) {
                        final byte tag = (this.aso.getSecurityContext().getCipheringType() == CipheringType.GENERAL_GLOBAL.getType())
                                ? DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING
                                : DLMSCOSEMGlobals.GENERAL_DEDICATED_CIPTHERING;
                        securedRequest = encryptGeneralCiphering(securedRequest);
                        securedRequest = ParseUtils.concatArray(new byte[]{tag}, securedRequest);
                    } else {
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
                final byte[] securedResponse = getTransportConnection().sendRequest(securedRequest);

                // check if the response tag is know and decrypt the data if necessary
                byte cipheredTag = securedResponse[LOCATION_SECURED_XDLMS_APDU_TAG];
                if (XdlmsApduTags.contains(cipheredTag)) {
                    // FIXME: Strip the 3 leading bytes before decryption -> due to old HDLC code
                    // Strip the 3 leading bytes before encrypting
                    final byte[] decryptedResponse;
                    decryptedResponse = decrypt(ProtocolUtils.getSubArray(securedResponse, 3));

                    // FIXME: Last step is to add the three leading bytes you stripped in the beginning -> due to old HDLC code
                    return ProtocolUtils.concatByteArrays(leading, decryptedResponse);
                } else if (cipheredTag == DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING || cipheredTag == DLMSCOSEMGlobals.GENERAL_DEDICATED_CIPTHERING) {
                    // Strip the 3 leading bytes before encrypting
                    final byte[] decryptedResponse;
                    decryptedResponse = decryptGeneralCiphering(ProtocolUtils.getSubArray(securedResponse, 3));

                    // FIXME: Last step is to add the three leading bytes you stripped in the beginning -> due to old HDLC code
                    return ProtocolUtils.concatByteArrays(leading, decryptedResponse);
                } else if (cipheredTag == DLMSCOSEMGlobals.COSEM_GENERAL_BLOCK_TRANSFER) {
                    return securedResponse; // Return the secured response as-is - it can only be decrypted after all blocks have been received
                                            // and thus should be done by the application layer (~ GeneralBlockTransferHandler)
                } else {
                    IOException ioException = new IOException("Unknown GlobalCiphering-Tag : " + securedResponse[3]);
                    throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(ioException);
                }
            }
        } else { /* During association request (AARQ and AARE) the request just needs to be forwarded */
            return getTransportConnection().sendRequest(byteRequestBuffer);
        }
    }

    private boolean useGeneralGloOrGeneralDedCiphering(int cipheringType) {
        return cipheringType == CipheringType.GENERAL_GLOBAL.getType() || cipheringType == CipheringType.GENERAL_DEDICATED.getType();
    }

    public byte[] sendRequest(final byte[] encryptedRequest) {
        return sendRequest(encryptedRequest, false);
    }

    public byte[] readResponseWithRetries(byte[] retryRequest) {
        return this.readResponseWithRetries(retryRequest, false);
    }

    public byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) {

        /* dataTransport security is only applied after we made an established association */
        if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {

            /* If no security is applied, then just forward the requests and responses */
            if (this.aso.getSecurityContext().getSecurityPolicy() == SecurityContext.SECURITYPOLICY_NONE) {
                return getTransportConnection().readResponseWithRetries(retryRequest);
            } else if (retryRequest[3] == DLMSCOSEMGlobals.COSEM_GENERAL_BLOCK_TRANSFER) {
                return getTransportConnection().readResponseWithRetries(retryRequest);  // As ComServer doesn't send any content, but only request next blocks, no encryption has to be applied
            } else {                                                                    // If ComServer would send actual content, then this content should be encrypted!

                // FIXME: Strip the 3 leading bytes before encrypting -> due to old HDLC code
                final byte[] leading = ProtocolUtils.getSubArray(retryRequest, 0, 2);
                byte[] securedRequest = ProtocolUtils.getSubArray(retryRequest, 3);

                if (!isAlreadyEncrypted) {     //Don't encrypt the request again if it's already encrypted
                    if (useGeneralGloOrGeneralDedCiphering(this.aso.getSecurityContext().getCipheringType())) {
                        final byte tag = (this.aso.getSecurityContext().getCipheringType() == CipheringType.GENERAL_GLOBAL.getType())
                                ? DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING
                                : DLMSCOSEMGlobals.GENERAL_DEDICATED_CIPTHERING;
                        securedRequest = encryptGeneralCiphering(securedRequest);
                        securedRequest = ParseUtils.concatArray(new byte[]{tag}, securedRequest);
                    } else {
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
                final byte[] securedResponse = getTransportConnection().readResponseWithRetries(securedRequest);

                // check if the response tag is know and decrypt the data if necessary
                byte cipheredTag = securedResponse[LOCATION_SECURED_XDLMS_APDU_TAG];
                if (XdlmsApduTags.contains(cipheredTag)) {
                    // FIXME: Strip the 3 leading bytes before decryption -> due to old HDLC code
                    // Strip the 3 leading bytes before encrypting
                    final byte[] decryptedResponse;
                    decryptedResponse = decrypt(ProtocolUtils.getSubArray(securedResponse, 3));

                    // FIXME: Last step is to add the three leading bytes you stripped in the beginning -> due to old HDLC code
                    return ProtocolUtils.concatByteArrays(leading, decryptedResponse);
                } else if (cipheredTag == DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING || cipheredTag == DLMSCOSEMGlobals.GENERAL_DEDICATED_CIPTHERING) {
                    // Strip the 3 leading bytes before encrypting
                    final byte[] decryptedResponse;
                    decryptedResponse = decryptGeneralCiphering(ProtocolUtils.getSubArray(securedResponse, 3));

                    // FIXME: Last step is to add the three leading bytes you stripped in the beginning -> due to old HDLC code
                    return ProtocolUtils.concatByteArrays(leading, decryptedResponse);
                } else if (cipheredTag == DLMSCOSEMGlobals.COSEM_GENERAL_BLOCK_TRANSFER) {
                    return securedResponse; // Return as-is, content can only be decrypted once all blocks are received
                } else {                    // and thus should be done by the application layer (~ GeneralBlockTransferHandler)
                    IOException ioException = new IOException("Unknown GlobalCiphering-Tag : " + securedResponse[3]);
                    throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(ioException);
                }
            }
        } else { /* During association request (AARQ and AARE) the request just needs to be forwarded */
            return getTransportConnection().readResponseWithRetries(retryRequest);
        }
    }

    //Subclasses can override encryption implementation
    protected byte[] encrypt(byte[] securedRequest) {
        return SecurityContextV2EncryptionHandler.dataTransportEncryption(this.aso.getSecurityContext(), securedRequest);
    }

    protected byte[] decrypt(byte[] securedResponse) {
        return SecurityContextV2EncryptionHandler.dataTransportDecryption(this.aso.getSecurityContext(), securedResponse);
    }

    private byte[] decryptGeneralCiphering(byte[] securedResponse) {
        return SecurityContextV2EncryptionHandler.dataTransportGeneralDecryption(this.aso.getSecurityContext(), securedResponse);
    }

    private byte[] encryptGeneralCiphering(byte[] request) {
        return SecurityContextV2EncryptionHandler.dataTransportGeneralEncryption(this.aso.getSecurityContext(), request);
    }

    public void sendUnconfirmedRequest(final byte[] byteRequestBuffer) {
        /* dataTransport security is only applied after we made an established association */
        if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {

            /* If no security is applied, then just forward the requests and responses */
            if (this.aso.getSecurityContext().getSecurityPolicy() == SecurityContext.SECURITYPOLICY_NONE) {
                getTransportConnection().sendUnconfirmedRequest(byteRequestBuffer);
            } else if (byteRequestBuffer[3] == DLMSCOSEMGlobals.COSEM_GENERAL_BLOCK_TRANSFER) {
                getTransportConnection().sendUnconfirmedRequest(byteRequestBuffer); // As ComServer doesn't send any content, but only request next blocks, no encryption has to be applied
            } else {                                                                 // If ComServer would send actual content, then this content should be encrypted!
                // FIXME: Strip the 3 leading bytes before encrypting -> due to old HDLC code
                final byte[] leading = ProtocolUtils.getSubArray(byteRequestBuffer, 0, 2);
                byte[] securedRequest = ProtocolUtils.getSubArray(byteRequestBuffer, 3);

                if (useGeneralGloOrGeneralDedCiphering(this.aso.getSecurityContext().getCipheringType())) {
                    final byte tag = (this.aso.getSecurityContext().getCipheringType() == CipheringType.GENERAL_GLOBAL.getType())
                            ? DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING
                            : DLMSCOSEMGlobals.GENERAL_DEDICATED_CIPTHERING;
                    securedRequest = encryptGeneralCiphering(securedRequest);
                    securedRequest = ParseUtils.concatArray(new byte[]{tag}, securedRequest);
                } else {
                    final byte tag = XdlmsApduTags.getEncryptedTag(securedRequest[0], this.aso.getSecurityContext().isGlobalCiphering());
                    securedRequest = encrypt(securedRequest);
                    securedRequest = ParseUtils.concatArray(new byte[]{tag}, securedRequest);
                }

                // FIXME: Last step is to add the three leading bytes you stripped in the beginning -> due to old HDLC code
                securedRequest = ProtocolUtils.concatByteArrays(leading, securedRequest);

                // send the encrypted request to the DLMSConnection
                getTransportConnection().sendUnconfirmedRequest(securedRequest);
            }
        } else { /* During association request (AARQ and AARE) the request just needs to be forwarded */
            getTransportConnection().sendUnconfirmedRequest(byteRequestBuffer);
        }
    }

    public void setHHUSignOn(final HHUSignOn hhuSignOn, final String meterId) {
        getTransportConnection().setHHUSignOn(hhuSignOn, meterId);
    }

    public void setHHUSignOn(final HHUSignOn hhuSignOn, final String meterId, int hhuSignonBaudRateCode) {
        getTransportConnection().setHHUSignOn(hhuSignOn, meterId, hhuSignonBaudRateCode);
    }

    public void setInvokeIdAndPriorityHandler(InvokeIdAndPriorityHandler iiapHandler) {
        getTransportConnection().setInvokeIdAndPriorityHandler(iiapHandler);
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