package com.energyict.protocolimpl.utils;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.*;
import com.energyict.dlms.aso.ApplicationServiceObject;

import java.io.IOException;

/**
 * Dummy DLMS connection class.
 * Just for testing purposes.
 * <p/>
 * Before each DLMSRequest you must set the response with {@link DummyDLMSConnection#setResponseByte(byte[])}
 *
 * @author gna
 */
public class DummyDLMSConnection implements DLMSConnection {

    /**
     * the expected response
     */
    private byte[] responseByte;
    private byte[] sentBytes;

    private ApplicationServiceObject aso = new DummyApplicationServiceObject();

    /**
     *
     */
    public DummyDLMSConnection() {
    }

    /* (non-Javadoc)
      * @see com.energyict.dlms.DLMSConnection#connectMAC()
      */
    public void connectMAC() throws IOException, DLMSConnectionException {

    }

    /* (non-Javadoc)
      * @see com.energyict.dlms.DLMSConnection#disconnectMAC()
      */
    public void disconnectMAC() throws IOException, DLMSConnectionException {
    }

    /* (non-Javadoc)
      * @see com.energyict.dlms.DLMSConnection#getHhuSignOn()
      */
    public HHUSignOn getHhuSignOn() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.energyict.dlms.DLMSConnection#getInvokeIdAndPriorityHandler()
     */
    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        return new NonIncrementalInvokeIdAndPriorityHandler();
    }

    public byte[] sendRawBytes(byte[] data) throws IOException {
        return new byte[0];
    }

    /* (non-Javadoc)
      * @see com.energyict.dlms.DLMSConnection#sendRequest(byte[])
      */
    public byte[] sendRequest(byte[] byteRequestBuffer) throws IOException {
        this.sentBytes = byteRequestBuffer.clone();
        return this.responseByte;
    }

    public void sendUnconfirmedRequest(final byte[] request) throws IOException {
        this.sentBytes = request.clone();
    }

    /**
     * Doesn't send anything, just returns the response you have to set before the send.
     */
    public byte[] sendRequest(final byte[] encryptedRequest, boolean isAlreadyEncrypted) throws IOException {
        return sendRequest(encryptedRequest);
    }

    public void setTimeout(long timeout) {
    }

    public long getTimeout() {
        return 0;
    }

    public void setRetries(int retries) {
    }

    /**
     * Doesn't send anything, just returns the response you have to set before the send.
     */
    public byte[] readResponseWithRetries(byte[] retryRequest) throws IOException {
        this.sentBytes = retryRequest.clone();
        return this.responseByte;
    }

    public byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) throws IOException {
        return this.readResponseWithRetries(retryRequest);
    }

    /* (non-Javadoc)
    * @see com.energyict.dlms.DLMSConnection#setHHUSignOn(com.energyict.dialer.connection.HHUSignOn, java.lang.String)
    */
    public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId) {
        // TODO Auto-generated method stub

    }

    public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId, int baudRateCode) {
    }


    /* (non-Javadoc)
     * @see com.energyict.dlms.DLMSConnection.setInvokeIdAndPriorityHandler(com.energyict.dlms.InvokeIdAndPriorityHandler)
     */
    public void setInvokeIdAndPriorityHandler(InvokeIdAndPriorityHandler iiapHandler) {
    }

    /* (non-Javadoc)
      * @see com.energyict.dlms.DLMSConnection#setIskraWrapper(int)
      */
    public void setIskraWrapper(int type) {
    }

    /* (non-Javadoc)
      * @see com.energyict.dlms.DLMSConnection#setSNRMType(int)
      */
    public void setSNRMType(int type) {
    }

    /**
     * Set the desired response for your next DLMS request.
     *
     * @param response - the response you would like to receive
     */
    public void setResponseByte(byte[] response) {
        this.responseByte = response.clone();
    }

    /**
     * @return the last byteArray you send over
     */
    public byte[] getSentBytes() {
        return this.sentBytes;
    }

    public int getMaxRetries() {
        return 0;
    }

    @Override
    public int getMaxTries() {
        return 0;
    }
}