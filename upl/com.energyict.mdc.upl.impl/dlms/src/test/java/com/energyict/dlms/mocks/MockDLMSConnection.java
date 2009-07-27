/**
 * 
 */
package com.energyict.dlms.mocks;

import java.io.IOException;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.InvokeIdAndPriority;

/**
 * @author gna
 *
 */
public class MockDLMSConnection implements DLMSConnection {
	
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
		if(this.responseByte == null){
			return byteRequestBuffer;
		} else {
			return this.responseByte;
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
	
	/**
	 * Set the responseBytes you want to get back from the sendRequest
	 * @param response
	 */
	public void setResponseByte(byte[] response){
		this.responseByte = response;
	}

}
