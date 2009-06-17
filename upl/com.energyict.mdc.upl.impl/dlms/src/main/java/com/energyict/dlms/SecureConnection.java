package com.energyict.dlms;

import java.io.IOException;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.protocol.ProtocolUtils;

public class SecureConnection implements DLMSConnection {
	
	private ApplicationServiceObject aso;
	private DLMSConnection connection;
	
	public SecureConnection(ApplicationServiceObject aso, DLMSConnection connection){
		this.aso = aso;
		this.connection = connection;
	}
	
	private DLMSConnection getConnection(){
		return this.connection;
	}

	public void connectMAC() throws IOException, DLMSConnectionException {
		getConnection().connectMAC();
	}

	public void disconnectMAC() throws IOException, DLMSConnectionException {
		getConnection().disconnectMAC();
	}

	public HHUSignOn getHhuSignOn() {
		return getConnection().getHhuSignOn();
	}

	public InvokeIdAndPriority getInvokeIdAndPriority() {
		return getConnection().getInvokeIdAndPriority();
	}

	public int getType() {
		return getConnection().getType();
	}

	public byte[] sendRequest(byte[] byteRequestBuffer) throws IOException {
		
		
		// Strip the 3 leading bytes before encrypting
		byte[] leading = ProtocolUtils.getSubArray(byteRequestBuffer, 0, 2);
		
		byte[] encryptedRequest = ProtocolUtils.getSubArray(byteRequestBuffer, 3);
		
		encryptedRequest = this.aso.getSecurityContext().dataTransportEncryption(encryptedRequest);
		
		// Last step is to add the three leading bytes you stripped in the beginning
		ProtocolUtils.concatByteArrays(leading, encryptedRequest);
		
		// send the encrypted request to the DLMSConnection
		return getConnection().sendRequest(encryptedRequest);
	}

	public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId) {
		getConnection().setHHUSignOn(hhuSignOn, meterId);
	}

	public void setInvokeIdAndPriority(InvokeIdAndPriority iiap) {
		getConnection().setInvokeIdAndPriority(iiap);
	}	

	public void setIskraWrapper(int type) {
		getConnection().setIskraWrapper(type);
	}

	public void setSNRMType(int type) {
		getConnection().setSNRMType(type);
	}

}
