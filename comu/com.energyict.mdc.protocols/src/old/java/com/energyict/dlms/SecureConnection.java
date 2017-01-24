package com.energyict.dlms;

import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.protocolimplv2.connection.DlmsConnection;
import com.energyict.dlms.protocolimplv2.connection.retryrequestpreparation.RetryRequestPreparationConsumer;
import com.energyict.dlms.protocolimplv2.connection.retryrequestpreparation.RetryRequestPreparationHandler;

import java.io.IOException;

/**
 * <pre>
 * A DLMSConnection acting as a gateway.
 * Depending on the required securityPolicy, the request and responses will be encrypted or decrypted.
 * </pre>
 *
 * @author gna
 */
public class SecureConnection implements DLMSConnection, RetryRequestPreparationHandler {

    private static final int LOCATION_SECURED_XDLMS_APDU_TAG = 3;

    private final ApplicationServiceObject aso;
    private final DlmsConnection transportConnection;

    private byte[] requestBeforeApplyOfSecurity;

    public SecureConnection(final ApplicationServiceObject aso, final DlmsConnection transportConnection) {
        this.aso = aso;
        this.transportConnection = transportConnection;
        if (transportConnection instanceof RetryRequestPreparationConsumer) {
            ((RetryRequestPreparationConsumer) transportConnection).setRetryRequestPreparationHandler(this);
        }
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

    public void setInvokeIdAndPriorityHandler(InvokeIdAndPriorityHandler iiapHandler) {
        getTransportConnection().setInvokeIdAndPriorityHandler(iiapHandler);
    }

    public byte[] sendRawBytes(byte[] data) throws IOException, DLMSConnectionException {
        requestBeforeApplyOfSecurity = null;
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
     * The sendRequest will check the current securitySuite to encrypt or authenticate the data and then parse the APDU to the DLMSConnection.
     * The response from the meter is decrypted before sending it back to the object.
     * <p/>
     * If the request is already encrypted (indicated by the boolean), there's no need to encrypt it again in the sendRequest() method
     *
     * @param byteRequestBuffer - The unEncrypted/authenticated request
     * @return the unEncrypted response from the device
     */
    public byte[] sendRequest(final byte[] byteRequestBuffer, boolean isAlreadyEncrypted) throws IOException {
        requestBeforeApplyOfSecurity = null;

        /* dataTransport security is only applied after we made an established association */
        if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {

                // FIXME: Strip the 3 leading bytes before encrypting -> due to old HDLC code
                final byte[] leading = ProtocolUtils.getSubArray(byteRequestBuffer, 0, 2);
            byte[] securedRequest;

                if (!isAlreadyEncrypted) {     //Don't encrypt the request again if it's already encrypted
                requestBeforeApplyOfSecurity = byteRequestBuffer;
                securedRequest = applyEncryption(byteRequestBuffer);
            } else {                        //No encryption, only increase the frame counter
                securedRequest = byteRequestBuffer;
                    aso.getSecurityContext().incFrameCounter();
                }

                // send the encrypted request to the DLMSConnection
                final byte[] securedResponse = getTransportConnection().sendRequest(securedRequest);

            if (this.aso.getSecurityContext().getSecurityPolicy().isResponsePlain()) {
                return securedResponse;
            } else {
                // check if the response tag is know and decrypt the data if necessary
                if (securedResponse[LOCATION_SECURED_XDLMS_APDU_TAG] == DLMSCOSEMGlobals.COSEM_EXCEPTION_RESPONSE) {
                    //Return any errors as-is
                    return securedResponse;
                } else if (XdlmsApduTags.contains(securedResponse[LOCATION_SECURED_XDLMS_APDU_TAG])) {
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
        requestBeforeApplyOfSecurity = null;

        //framecounter out of sync (it was already incremented in sendRequest, so decrement it)
        aso.getSecurityContext().decrementFrameCounter();

        /* dataTransport security is only applied after we made an established association */
        if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {

            /* If no security is applied, then just forward the response */
            if (this.aso.getSecurityContext().getSecurityPolicy().isResponsePlain()) {
                return getTransportConnection().readResponseWithRetries(retryRequest);
            } else {
                final byte[] leading = ProtocolUtils.getSubArray(retryRequest, 0, 2);
                byte[] securedRequest;

                if (!isAlreadyEncrypted) {     //Don't encrypt the request again if it's already encrypted
                    // frame counter out of sync (it was already incremented in sendRequest, so decrement it)
                    // this way we leave the choice whether or not the frame counter should be incremented for retry requests still open
                    aso.getSecurityContext().decrementFrameCounter();
                    requestBeforeApplyOfSecurity = retryRequest;
                    securedRequest = applyEncryption(retryRequest);
                } else {                        //No encryption, only increase the frame counter
                    securedRequest = retryRequest;
                    aso.getSecurityContext().incFrameCounter();
                }

                // send the encrypted request to the DLMSConnection
                final byte[] securedResponse = getTransportConnection().readResponseWithRetries(securedRequest);

                //response sent, increment the framecounter
                aso.getSecurityContext().incFrameCounter();

                // check if the response tag is know and decrypt the data if necessary
                if (securedResponse[LOCATION_SECURED_XDLMS_APDU_TAG] == DLMSCOSEMGlobals.COSEM_EXCEPTION_RESPONSE) {
                    //Return any errors as-is
                    return securedResponse;
                } else if (XdlmsApduTags.contains(securedResponse[LOCATION_SECURED_XDLMS_APDU_TAG])) {
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

    private byte[] applyEncryption(byte[] plainTextRequest) throws IOException {
        if (!this.aso.getSecurityContext().getSecurityPolicy().isRequestPlain()) {
            // FIXME: Strip the 3 leading bytes before encrypting -> due to old HDLC code
            final byte[] leading = ProtocolUtils.getSubArray(plainTextRequest, 0, 2);
            byte[] securedRequest = ProtocolUtils.getSubArray(plainTextRequest, 3);

            final byte tag = XdlmsApduTags.getEncryptedTag(securedRequest[0], this.aso.getSecurityContext().isGlobalCiphering());
            securedRequest = encrypt(securedRequest);
            securedRequest = ParseUtils.concatArray(new byte[]{tag}, securedRequest);

            // FIXME: Last step is to add the three leading bytes you stripped in the beginning -> due to old HDLC code
            securedRequest = ProtocolUtils.concatByteArrays(leading, securedRequest);
            return securedRequest;
        } else {
            return plainTextRequest;
        }
    }

    private byte[] encrypt(byte[] securedRequest, boolean incrementFrameCounter) throws IOException {
        return this.aso.getSecurityContext().dataTransportEncryption(securedRequest, incrementFrameCounter);
    }

    //Subclasses can override encryption implementation
    protected byte[] encrypt(byte[] securedRequest) throws IOException {
        return this.aso.getSecurityContext().dataTransportEncryption(securedRequest, true);
    }

    protected byte[] decrypt(byte[] securedResponse) throws IOException, DLMSConnectionException {
        return this.aso.getSecurityContext().dataTransportDecryption(securedResponse);
    }

    public void sendUnconfirmedRequest(final byte[] byteRequestBuffer) throws IOException {
        requestBeforeApplyOfSecurity = null;

        /* dataTransport security is only applied after we made an established association */
        if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {
            requestBeforeApplyOfSecurity = byteRequestBuffer;
            byte[] securedRequest = applyEncryption(byteRequestBuffer);
                getTransportConnection().sendUnconfirmedRequest(securedRequest);
        } else { /* During association request (AARQ and AARE) the request just needs to be forwarded */
            getTransportConnection().sendUnconfirmedRequest(byteRequestBuffer);
        }
    }

    @Override
    public byte[] prepareRetryRequest(byte[] originalRequest) throws IOException {
        if (requestBeforeApplyOfSecurity != null && incrementFrameCounterForRetries()) {
            // Re-apply security
            // Note that after applying of security to the original request the frame counter was automatically increased;
            // So, if we here re-apply the security, we are automatically taking into account the increased frame counter!
            return applyEncryption(requestBeforeApplyOfSecurity);
        } else {
            // Else there is no security applied and/or the frame counter should not be increased, thus return the original request as-is
            return originalRequest;
        }
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

    public boolean incrementFrameCounterForRetries() {
        return getTransportConnection() instanceof RetryRequestPreparationConsumer && ((RetryRequestPreparationConsumer) getTransportConnection()).incrementFrameCounterForRetries();
    }
}