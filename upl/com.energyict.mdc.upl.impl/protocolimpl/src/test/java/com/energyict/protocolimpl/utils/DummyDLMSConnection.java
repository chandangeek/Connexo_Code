package com.energyict.protocolimpl.utils;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.*;
import com.energyict.dlms.aso.ApplicationServiceObject;

import java.io.IOException;

/**
 * Dummy DLMS connection class.
 * Just for testing purposes.
 *
 * Before each DLMSRequest you must set the response with {@link DummyDLMSConnection#setResponseByte(byte[])}
 *
 * @author gna
 *
 */
public class DummyDLMSConnection implements DLMSConnection {

	/** the expected response */
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
	 * @see com.energyict.dlms.DLMSConnection#getInvokeIdAndPriority()
	 */
	public InvokeIdAndPriority getInvokeIdAndPriority() {
		return new InvokeIdAndPriority();
	}

	/* (non-Javadoc)
	 * @see com.energyict.dlms.DLMSConnection#getType()
	 */
	public int getType() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.energyict.dlms.DLMSConnection#sendRequest(byte[])
	 */
	public byte[] sendRequest(byte[] byteRequestBuffer) throws IOException {
		this.sentBytes = byteRequestBuffer.clone();
		return this.responseByte;
	}

	/* (non-Javadoc)
	 * @see com.energyict.dlms.DLMSConnection#setHHUSignOn(com.energyict.dialer.connection.HHUSignOn, java.lang.String)
	 */
	public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.energyict.dlms.DLMSConnection#setInvokeIdAndPriority(com.energyict.dlms.InvokeIdAndPriority)
	 */
	public void setInvokeIdAndPriority(InvokeIdAndPriority iiap) {
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
	 * @param response - the response you would like to receive
	 */
	public void setResponseByte(byte[] response){
		this.responseByte = response.clone();
	}

	/**
	 * @return the last byteArray you send over
	 */
	public byte[] getSentBytes(){
		return this.sentBytes;
	}

	public int getMaxRetries() {
		return 0;
	}

    public ApplicationServiceObject getApplicationServiceObject() {
        return aso;
    }

    public void setApplicationServiceObject(ApplicationServiceObject aso){
        this.aso = aso;
    }

}
