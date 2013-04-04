/**
 *
 */
package com.energyict.dlms.mocks;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.aso.ApplicationServiceObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gna
 */
public class MockDLMSConnection implements DLMSConnection {

    private Map<String, String> requestResponsePairs = new HashMap<String, String>();

    private byte[] responseByte;

    /**
     *
     */
    public MockDLMSConnection() {
    }

    public void connectMAC() throws IOException, DLMSConnectionException {

    }

    public byte[] sendRequest(final byte[] encryptedRequest, boolean isAlreadyEncrypted) throws IOException {
        return sendRequest(encryptedRequest);
    }

    public void setTimeout(int timeout) {
    }

    public int getTimeout() {
        return 0;
    }

    public void disconnectMAC() throws IOException, DLMSConnectionException {
    }

    public HHUSignOn getHhuSignOn() {
        return null;
    }

    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        return new NonIncrementalInvokeIdAndPriorityHandler();
    }

    public int getType() {
        return 0;
    }

    public byte[] sendRawBytes(byte[] data) throws IOException {
        return new byte[0];
    }

    /**
     * Doesn't send anything, just returns the response you have to set before the send. If you didn't set anything, then your requestBuffer is returned
     */
    public byte[] sendRequest(byte[] byteRequestBuffer) throws IOException {
        String requestAsHexString = DLMSUtils.getHexStringFromBytes(byteRequestBuffer);
        String responseAsHexString = requestResponsePairs.get(requestAsHexString);
        if (responseAsHexString != null) {
            return DLMSUtils.getBytesFromHexString(responseAsHexString);
        } else {
            if (this.responseByte != null) {
                return this.responseByte;
            } else {
                System.out.println(DLMSUtils.getHexStringFromBytes(byteRequestBuffer));
                return byteRequestBuffer;
            }
        }
    }

    /**
     * Doesn't send anything, just returns the response you have to set before the send. If you didn't set anything, then your requestBuffer is returned
     */
    public byte[] readResponseWithRetries(byte[] retryRequest) throws IOException {
        String requestAsHexString = DLMSUtils.getHexStringFromBytes(retryRequest);
        String responseAsHexString = requestResponsePairs.get(requestAsHexString);
        if (responseAsHexString != null) {
            return DLMSUtils.getBytesFromHexString(responseAsHexString);
        } else {
            if (this.responseByte != null) {
                return this.responseByte;
            } else {
                System.out.println(DLMSUtils.getHexStringFromBytes(retryRequest));
                return retryRequest;
            }
        }
    }

    public byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) throws IOException {
        return this.readResponseWithRetries(retryRequest);
    }

    public void sendUnconfirmedRequest(final byte[] request) throws IOException {
        // Nothing to do here
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId) {
    }

    public void setInvokeIdAndPriorityHandler(InvokeIdAndPriorityHandler iiapHandler) {
    }

    public void setIskraWrapper(int type) {
    }

    public void setSNRMType(int type) {
    }

    public void addRequestResponsePair(String request, String response) {
        requestResponsePairs.put(request, response);
    }

    public void clearRequestResponses() {
        requestResponsePairs.clear();
    }

    /**
     * Set the responseBytes you want to get back from the sendRequest
     *
     * @param response
     */
    public void setResponseByte(byte[] response) {
        this.responseByte = response;
    }

    public int getMaxRetries() {
        return 0;
    }

    public ApplicationServiceObject getApplicationServiceObject() {
        return null;
    }

}
