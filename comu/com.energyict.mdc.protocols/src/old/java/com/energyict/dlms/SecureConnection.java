package com.energyict.dlms;

import com.energyict.dlms.protocolimplv2.connection.DlmsConnection;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 * <pre>
 * A DLMSConnection acting as a gateway.
 * Depending on the required securityPolicy, the request and responses will be encrypted or decrypted.
 * </pre>
 *
 * @author gna
 */
public class SecureConnection implements DLMSConnection {

    private static final int LOCATION_SECURED_XDLMS_APDU_TAG = 3;

    private final ApplicationServiceObject aso;
    private final DlmsConnection transportConnection;

    public SecureConnection(final ApplicationServiceObject aso, final DlmsConnection transportConnection) {
        this.aso = aso;
        this.transportConnection = transportConnection;
    }

    /**
     * @return the actual DLMSConnection used for dataTransportation
     */
    private DlmsConnection getTransportConnection() {
        return this.transportConnection;
    }

    public void connectMAC() throws IOException, DLMSConnectionException {
        getTransportConnection().connectMAC();
    }

    public void disconnectMAC() throws IOException, DLMSConnectionException {
        getTransportConnection().disconnectMAC();
    }

    public HHUSignOn getHhuSignOn() {
        return getTransportConnection().getHhuSignOn();
    }

    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        return getTransportConnection().getInvokeIdAndPriorityHandler();
    }

    public byte[] sendRawBytes(byte[] data) throws IOException, DLMSConnectionException {
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
    public byte[] sendRequest(final byte[] byteRequestBuffer, boolean isAlreadyEncrypted) throws IOException {

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
                    try {
                        decryptedResponse = decrypt(ProtocolUtils.getSubArray(securedResponse, 3));
                    } catch (DLMSConnectionException e) {
                        throw new IOException(e.getMessage());
                    }

                    // FIXME: Last step is to add the three leading bytes you stripped in the beginning -> due to old HDLC code
                    return ProtocolUtils.concatByteArrays(leading, decryptedResponse);
                } else {
                    throw new IOException("Unknown GlobalCiphering-Tag : " + securedResponse[3]);
                }
            }
        } else { /* During association request (AARQ and AARE) the request just needs to be forwarded */
            return getTransportConnection().sendRequest(byteRequestBuffer);
        }
    }

    public byte[] sendRequest(final byte[] encryptedRequest) throws IOException {
        return sendRequest(encryptedRequest, false);
    }

    public byte[] readResponseWithRetries(byte[] retryRequest) throws IOException {
        return this.readResponseWithRetries(retryRequest, false);
    }

    public byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) throws IOException {

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
                    try {
                        decryptedResponse = decrypt(ProtocolUtils.getSubArray(securedResponse, 3));
                    } catch (DLMSConnectionException e) {
                        throw new IOException(e.getMessage());
                    }

                    // FIXME: Last step is to add the three leading bytes you stripped in the beginning -> due to old HDLC code
                    return ProtocolUtils.concatByteArrays(leading, decryptedResponse);
                } else {
                    throw new IOException("Unknown GlobalCiphering-Tag : " + securedResponse[3]);
                }
            }
        } else { /* During association request (AARQ and AARE) the request just needs to be forwarded */
            return getTransportConnection().readResponseWithRetries(retryRequest);
        }
    }

    //Subclasses can override encryption implementation
    protected byte[] encrypt(byte[] securedRequest) throws IOException {
        return this.aso.getSecurityContext().dataTransportEncryption(securedRequest);
    }

    protected byte[] decrypt(byte[] securedResponse) throws IOException, DLMSConnectionException {
        return this.aso.getSecurityContext().dataTransportDecryption(securedResponse);
    }

    public void sendUnconfirmedRequest(final byte[] byteRequestBuffer) throws IOException {
        /* dataTransport security is only applied after we made an established association */
        if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {

            /* If no security is applied, then just forward the requests and responses */
            if (this.aso.getSecurityContext().getSecurityPolicy() != SecurityContext.SECURITYPOLICY_NONE) {
                // FIXME: Strip the 3 leading bytes before encrypting -> due to old HDLC code
                final byte[] leading = ProtocolUtils.getSubArray(byteRequestBuffer, 0, 2);
                byte[] securedRequest = ProtocolUtils.getSubArray(byteRequestBuffer, 3);

                final byte tag = XdlmsApduTags.getEncryptedTag(securedRequest[0], this.aso.getSecurityContext().isGlobalCiphering());

                securedRequest = this.aso.getSecurityContext().dataTransportEncryption(securedRequest);
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