package com.energyict.dlms;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.protocol.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AdaptorConnection implements DLMSConnection {

	final int DEBUG=0;
	ByteArrayOutputStream baos = null;
	private InvokeIdAndPriority invokeIdAndPriority = new InvokeIdAndPriority();

	public AdaptorConnection() throws IOException {
		// TODO Auto-generated constructor stub
	}

	public void connectMAC() throws IOException, DLMSConnectionException {
		// TODO Auto-generated method stub

	}

	public void disconnectMAC() throws IOException, DLMSConnectionException {
		// TODO Auto-generated method stub

	}

	public HHUSignOn getHhuSignOn() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public byte[] sendRequest(byte[] byteRequestBuffer) throws IOException {
		if (DEBUG>=1) {
			System.out.println(ProtocolUtils.outputHexString(byteRequestBuffer));
		}
		if (baos == null) {
			baos = new ByteArrayOutputStream();
		}
		baos.write(byteRequestBuffer, 3, byteRequestBuffer.length-3); // skip HDLS LLC
		return null;
	}

	public void reset() {
		baos.reset();
	}

	public byte[] getCompoundData() {
		byte[] data = null;
		data = baos.toByteArray();
//		baos=null;
		return data;
	}

	public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId) {
		// TODO Auto-generated method stub

	}

	public void setIskraWrapper(int type) {
		// TODO Auto-generated method stub

	}

	public void setSNRMType(int type) {
		// TODO Auto-generated method stub

	}

    /********************************************************************************************************
     * Invoke-Id-And-Priority byte setting
     ********************************************************************************************************/

    public void setInvokeIdAndPriority(InvokeIdAndPriority iiap){
    	this.invokeIdAndPriority = iiap;
    }

    public InvokeIdAndPriority getInvokeIdAndPriority(){
    	return this.invokeIdAndPriority;
    }

	public int getMaxRetries() {
		return 0;
	}

    public ApplicationServiceObject getApplicationServiceObject() {
        return null;
    }

}
