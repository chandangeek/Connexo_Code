package com.energyict.dlms;

import java.io.IOException;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.protocol.ProtocolUtils;

public class SecureConnection implements DLMSConnection {
	
	private ApplicationServiceObject aso;
	private DLMSConnection connection;
	
	public SecureConnection(ApplicationServiceObject aso, DLMSConnection transportConnection){
		this.aso = aso;
		this.connection = transportConnection;
	}
	
	private DLMSConnection getTransportConnection(){
		return this.connection;
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

	public InvokeIdAndPriority getInvokeIdAndPriority() {
		return getTransportConnection().getInvokeIdAndPriority();
	}

	public int getType() {
		return getTransportConnection().getType();
	}

	public byte[] sendRequest(byte[] byteRequestBuffer) throws IOException {
		
		if(this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED){ // only then we should encrypt
			
			// Strip the 3 leading bytes before encrypting
			byte[] leading = ProtocolUtils.getSubArray(byteRequestBuffer, 0, 2);
			
			byte[] encryptedRequest = ProtocolUtils.getSubArray(byteRequestBuffer, 3);
			
			//TODO add the securityHeader
			
			encryptedRequest = this.aso.getSecurityContext().dataTransportEncryption(encryptedRequest);
			
			// Last step is to add the three leading bytes you stripped in the beginning
			encryptedRequest = ProtocolUtils.concatByteArrays(leading, encryptedRequest);
			
			// send the encrypted request to the DLMSConnection
			return getTransportConnection().sendRequest(encryptedRequest);
			
		} else {
			return getTransportConnection().sendRequest(byteRequestBuffer);
		}
		
	}

	public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId) {
		getTransportConnection().setHHUSignOn(hhuSignOn, meterId);
	}

	public void setInvokeIdAndPriority(InvokeIdAndPriority iiap) {
		getTransportConnection().setInvokeIdAndPriority(iiap);
	}	

	public void setIskraWrapper(int type) {
		getTransportConnection().setIskraWrapper(type);
	}

	public void setSNRMType(int type) {
		getTransportConnection().setSNRMType(type);
	}

}