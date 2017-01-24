package com.energyict.dlms.protocolimplv2.connection;

import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.XdlmsApduTags;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.protocols.exception.ProtocolEncryptionException;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;
import com.energyict.protocols.util.ProtocolUtils;

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
            } else {

                // FIXME: Strip the 3 leading bytes before encrypting -> due to old HDLC code
                final byte[] leading = ProtocolUtils.getSubArray(byteRequestBuffer, 0, 2);
                byte[] securedRequest = ProtocolUtils.getSubArray(byteRequestBuffer, 3);

                if (!isAlreadyEncrypted) {     //Don't encrypt the request again if it's already encrypted
                    final byte tag = XdlmsApduTags.getEncryptedTag(securedRequest[0], this.aso.getSecurityContext().isGlobalCiphering());
                    securedRequest = encrypt(securedRequest);
                    securedRequest = ParseUtils.concatArray(new byte[]{tag}, securedRequest);
                } else {
                    //No encryption, only increase the frame counter
                    aso.getSecurityContext().incFrameCounter();
                }

                // FIXME: Last step is to add the three leading bytes you stripped in the beginning -> due to old HDLC code
                securedRequest = ProtocolUtils.concatByteArrays(leading, securedRequest);

                // send the encrypted request to the DLMSConnection
                final byte[] securedResponse = getTransportConnection().sendRequest(securedRequest);

                // check if the response tag is know and decrypt the data if necessary
                if (XdlmsApduTags.contains(securedResponse[LOCATION_SECURED_XDLMS_APDU_TAG])) {
                    // FIXME: Strip the 3 leading bytes before decryption -> due to old HDLC code
                    // Strip the 3 leading bytes before encrypting
                    final byte[] decryptedResponse;
                    decryptedResponse = decrypt(ProtocolUtils.getSubArray(securedResponse, 3));

                    // FIXME: Last step is to add the three leading bytes you stripped in the beginning -> due to old HDLC code
                    return ProtocolUtils.concatByteArrays(leading, decryptedResponse);
                } else {
                    IOException ioException = new IOException("Unknown GlobalCiphering-Tag : " + securedResponse[3]);
                    throw new ProtocolEncryptionException(MessageSeeds.ENCRYPTION_ERROR, ioException);
                }
            }
        } else { /* During association request (AARQ and AARE) the request just needs to be forwarded */
            return getTransportConnection().sendRequest(byteRequestBuffer);
        }
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
            } else {

                // FIXME: Strip the 3 leading bytes before encrypting -> due to old HDLC code
                final byte[] leading = ProtocolUtils.getSubArray(retryRequest, 0, 2);
                byte[] securedRequest = ProtocolUtils.getSubArray(retryRequest, 3);

                if (!isAlreadyEncrypted) {     //Don't encrypt the request again if it's already encrypted
                    final byte tag = XdlmsApduTags.getEncryptedTag(securedRequest[0], this.aso.getSecurityContext().isGlobalCiphering());
                    securedRequest = encrypt(securedRequest);
                    securedRequest = ParseUtils.concatArray(new byte[]{tag}, securedRequest);
                } else {
                    //No encryption, only increase the frame counter
                    aso.getSecurityContext().incFrameCounter();
                }

                // FIXME: Last step is to add the three leading bytes you stripped in the beginning -> due to old HDLC code
                securedRequest = ProtocolUtils.concatByteArrays(leading, securedRequest);

                // send the encrypted request to the DLMSConnection
                final byte[] securedResponse = getTransportConnection().readResponseWithRetries(securedRequest);

                // check if the response tag is know and decrypt the data if necessary
                if (XdlmsApduTags.contains(securedResponse[LOCATION_SECURED_XDLMS_APDU_TAG])) {
                    // FIXME: Strip the 3 leading bytes before decryption -> due to old HDLC code
                    // Strip the 3 leading bytes before encrypting
                    final byte[] decryptedResponse;
                    decryptedResponse = decrypt(ProtocolUtils.getSubArray(securedResponse, 3));

                    // FIXME: Last step is to add the three leading bytes you stripped in the beginning -> due to old HDLC code
                    return ProtocolUtils.concatByteArrays(leading, decryptedResponse);
                } else {
                    IOException ioException = new IOException("Unknown GlobalCiphering-Tag : " + securedResponse[3]);
                    throw new ProtocolEncryptionException(MessageSeeds.ENCRYPTION_ERROR, ioException);
                }
            }
        } else { /* During association request (AARQ and AARE) the request just needs to be forwarded */
            return getTransportConnection().readResponseWithRetries(retryRequest);
        }
    }

    //Subclasses can override encryption implementation
    protected byte[] encrypt(byte[] securedRequest) {
        try {
            return this.aso.getSecurityContext().dataTransportEncryption(securedRequest);
        } catch (UnsupportedException e) {             //Unsupported security policy
            throw new CommunicationException(MessageSeeds.PROTOCOL_IO_PARSE_ERROR, e);
        }
    }

    protected byte[] decrypt(byte[] securedResponse) {
        try {
            return this.aso.getSecurityContext().dataTransportDecryption(securedResponse);
        } catch (ConnectionException | DLMSConnectionException e) {              //Failed to decrypt data
            throw new ProtocolEncryptionException(MessageSeeds.ENCRYPTION_ERROR, e);
        } catch (UnsupportedException e) {             //Unsupported security policy
            throw new CommunicationException(MessageSeeds.PROTOCOL_IO_PARSE_ERROR, e);
        }
    }

    public void sendUnconfirmedRequest(final byte[] byteRequestBuffer) {
        /* dataTransport security is only applied after we made an established association */
        if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {

            /* If no security is applied, then just forward the requests and responses */
            if (this.aso.getSecurityContext().getSecurityPolicy() != SecurityContext.SECURITYPOLICY_NONE) {
                // FIXME: Strip the 3 leading bytes before encrypting -> due to old HDLC code
                final byte[] leading = ProtocolUtils.getSubArray(byteRequestBuffer, 0, 2);
                byte[] securedRequest = ProtocolUtils.getSubArray(byteRequestBuffer, 3);

                final byte tag = XdlmsApduTags.getEncryptedTag(securedRequest[0], this.aso.getSecurityContext().isGlobalCiphering());

                securedRequest = encrypt(securedRequest);
                securedRequest = ParseUtils.concatArray(new byte[]{tag}, securedRequest);

                // FIXME: Last step is to add the three leading bytes you stripped in the beginning -> due to old HDLC code
                securedRequest = ProtocolUtils.concatByteArrays(leading, securedRequest);

                // send the encrypted request to the DLMSConnection
                getTransportConnection().sendUnconfirmedRequest(securedRequest);
            } else {
                getTransportConnection().sendUnconfirmedRequest(byteRequestBuffer);
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
}