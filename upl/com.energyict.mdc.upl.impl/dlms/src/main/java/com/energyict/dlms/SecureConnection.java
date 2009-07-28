package com.energyict.dlms;

import java.io.IOException;
import java.util.HashMap;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.client.ParseUtils;
import com.energyict.protocol.ProtocolUtils;
/**
 * <pre>
 * A DLMSConnection acting as a gateway.
 * Depending on the required securityPolicy, the request and responses will be encrypted or decrypted.
 * </pre>
 * @author gna
 *
 */
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
		
		encryptionTagMap.put(DLMSCOSEMGlobals.COSEM_READREQUEST, DLMSCOSEMGlobals.GLO_READREQUEST);
		encryptionTagMap.put(DLMSCOSEMGlobals.COSEM_READRESPONSE, DLMSCOSEMGlobals.GLO_READRESPONSE);
		encryptionTagMap.put(DLMSCOSEMGlobals.COSEM_WRITEREQUEST, DLMSCOSEMGlobals.GLO_WRITEREQUEST);
		encryptionTagMap.put(DLMSCOSEMGlobals.COSEM_WRITERESPONSE, DLMSCOSEMGlobals.GLO_WRITERESPONSE);

	}
	
	public SecureConnection(ApplicationServiceObject aso, DLMSConnection transportConnection){
		this.aso = aso;
		this.connection = transportConnection;
	}
	
	/**
	 * @return the actual DLMSConnection used for dataTransprotation
	 */
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
	 * The sendRequest will check the current securitySuite to encrypt or authenticate the data and then parse the APDU to the DLMSConnection.
	 * The response from the meter is decrypted before sending it back to the object.
	 * @param byteRequestBuffer - The unEncrypted/authenticated request
	 * @return the unEncrypted response from the device
	 */
	public byte[] sendRequest(byte[] byteRequestBuffer) throws IOException {
		
		/* dataTransport security is only applied after we made an established association */
		if(this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED){ 	
			
			/* If no security is applied, then just forward the requests and responses */
			if(this.aso.getSecurityContext().getSecurityPolicy() == 0){
				return getTransportConnection().sendRequest(byteRequestBuffer);
			} else {
				
				// Strip the 3 leading bytes before encrypting
				byte[] leading = ProtocolUtils.getSubArray(byteRequestBuffer, 0, 2);
				byte[] securedRequest = ProtocolUtils.getSubArray(byteRequestBuffer, 3);
				byte tag = ((Byte) encryptionTagMap.get(securedRequest[0])).byteValue();
				
				securedRequest = this.aso.getSecurityContext().dataTransportEncryption(securedRequest);
				securedRequest = ParseUtils.concatArray(new byte[]{tag}, securedRequest);
				
				// Last step is to add the three leading bytes you stripped in the beginning
				securedRequest = ProtocolUtils.concatByteArrays(leading, securedRequest);
				
				// send the encrypted request to the DLMSConnection
				byte[] securedResponse = getTransportConnection().sendRequest(securedRequest);
				
				// check if the response tag is know and decrypt the data if necessary
				if(encryptionTagMap.containsKey(securedResponse[0])){
					byte[] decryptedResponse = this.aso.getSecurityContext().dataTransportDecryption(ProtocolUtils.getSubArray(securedResponse, 2));
					return decryptedResponse;
				} else {
					throw new IOException("Unknown GlobalCiphering-Tag : " + securedResponse[0]);
				}
			}
		} else { /* During association establishment the request just needs to be forwarded */
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