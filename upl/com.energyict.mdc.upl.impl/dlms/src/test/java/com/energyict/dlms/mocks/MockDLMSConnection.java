/**
 *
 */
package com.energyict.dlms.mocks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.InvokeIdAndPriority;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * @author gna
 *
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

	public void disconnectMAC() throws IOException, DLMSConnectionException {
	}

	public HHUSignOn getHhuSignOn() {
		return null;
	}

	public InvokeIdAndPriority getInvokeIdAndPriority() {
		return new InvokeIdAndPriority();
	}

	public int getType() {
		return 0;
	}

	/**
	 * Doesn't send anything, just returns the response you have to set before the send. If you didn't set anything, then your requestBuffer is returned
	 */
	public byte[] sendRequest(byte[] byteRequestBuffer) throws IOException {
		String requestAsHexString = ProtocolTools.getHexStringFromBytes(byteRequestBuffer);
		String responseAsHexString = requestResponsePairs.get(requestAsHexString);
		if (responseAsHexString != null) {
			return ProtocolTools.getBytesFromHexString(responseAsHexString);
		} else {
			if (this.responseByte != null) {
				return this.responseByte;
			} else {
				return byteRequestBuffer;
			}
		}
	}

	public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId) {
	}

	public void setInvokeIdAndPriority(InvokeIdAndPriority iiap) {
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
	 * @param response
	 */
	public void setResponseByte(byte[] response){
		this.responseByte = response;
	}

	public int getMaxRetries() {
		return 0;
	}

}
