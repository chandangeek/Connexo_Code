package com.energyict.dlms;

import java.io.IOException;
import java.util.HashMap;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.client.ParseUtils;
import com.energyict.protocol.ProtocolUtils;

public class SecureConnection implements DLMSConnection {
	
	private ApplicationServiceObject aso;
	private DLMSConnection connection;
	
	private static HashMap encryptionTagMap =  new HashMap();
	static{
		encryptionTagMap.put(DLMSCOSEMGlobals.COSEM_GETREQUEST, DLMSCOSEMGlobals.GLO_GETREQUEST);
		encryptionTagMap.put(DLMSCOSEMGlobals.COSEM_ACTIONREQUEST, DLMSCOSEMGlobals.GLO_ACTIOREQUEST);
		encryptionTagMap.put(DLMSCOSEMGlobals.COSEM_SETREQUEST, DLMSCOSEMGlobals.GLO_SETREQUEST);
		encryptionTagMap.put(DLMSCOSEMGlobals.COSEM_GETRESPONSE, DLMSCOSEMGlobals.GLO_GETRESPONSE);
		encryptionTagMap.put(DLMSCOSEMGlobals.COSEM_SETRESPONSE, DLMSCOSEMGlobals.GLO_SETRESPONSE);
		encryptionTagMap.put(DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE, DLMSCOSEMGlobals.GLO_ACTIONRESPONSE);
	}
	
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

	/**
	 * The sendRequest will check the current securitySuit to encrypt or authenticate the data and then parse the APDU to the DLMSConnection.
	 * The response from the meter is decrypted before sending it back to the object.
	 * @param byteRequestBuffer - The unEncrypted/authenticated request
	 * @return the unEncrypted response from the device
	 */
	public byte[] sendRequest(byte[] byteRequestBuffer) throws IOException {
		
		if(this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED){ 	// only then we should encrypt
			
			if((this.aso.getSecurityContext().getSecurityControlByte()&0x30) == 0){				// No Encryption/Authentication
				return getTransportConnection().sendRequest(byteRequestBuffer);
			} else {
				
				// Strip the 3 leading bytes before encrypting
				byte[] leading = ProtocolUtils.getSubArray(byteRequestBuffer, 0, 2);
				byte[] securedRequest = ProtocolUtils.getSubArray(byteRequestBuffer, 3);
				byte tag = ((Byte) encryptionTagMap.get(securedRequest[0])).byteValue();
				
				//TODO add the securityHeader or securityContext or whatever it's called
				securedRequest = this.aso.getSecurityContext().dataTransportEncryption(securedRequest);
				securedRequest = ParseUtils.concatArray(new byte[]{tag}, securedRequest);
				
				// Last step is to add the three leading bytes you stripped in the beginning
				securedRequest = ProtocolUtils.concatByteArrays(leading, securedRequest);
				
				// send the encrypted request to the DLMSConnection
				// TODO decode the frame before sending data to the object 
				
				return getTransportConnection().sendRequest(securedRequest);
				
			}
			
		} else {
			return getTransportConnection().sendRequest(byteRequestBuffer);
		}
		
	}
	
//	/**
//	 * Construct the securityHeader depending on the secuirityContext defined in the ASO
//	 * @return
//	 */
//	private byte[] constructSecurityHeader(){
//		byte[] securityHeader = new byte[5];
//		securityHeader[4] = this.aso.getSecurityContext().getSecurityControlByte();
//		
//		securityHeader[0] = (byte) (this.aso.getSecurityContext().getFrameCounter()&0xFF);
//		securityHeader[1] = (byte) ((this.aso.getSecurityContext().getFrameCounter()>>8)&0xFF);
//		securityHeader[2] = (byte) ((this.aso.getSecurityContext().getFrameCounter()>>16)&0xFF);
//		securityHeader[3] = (byte) ((this.aso.getSecurityContext().getFrameCounter()>>24)&0xFF);
//		
//		return securityHeader;
//	}
	
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