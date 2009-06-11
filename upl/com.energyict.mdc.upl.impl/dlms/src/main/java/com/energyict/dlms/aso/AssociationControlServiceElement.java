package com.energyict.dlms.aso;

import java.io.IOException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.protocol.ProtocolUtils;

/**
 * 
 * @author gna
 * 
 */
public class AssociationControlServiceElement {

	/**
	 * <pre>
	 * This default object identifier means:
	 *  - two ASE's are present (ACSE and xDLMS_ASE)
	 *  - xDLMS_ASE is as it is specified in 61334-4-41
	 *  - the transfer syntax is A-XDR
	 * </pre>
	 */
	private static byte[] DEFAULT_OBJECT_IDENTIFIER = new byte[] { (byte) 0x60,
			(byte) 0x85, (byte) 0x74, (byte) 0x05, (byte) 0x08 };
	
	public static int LOGICAL_NAME_REFERENCING_NO_CIPHERING = 1;
	public static int LOGICAL_NAME_REFERENCING_WITH_CIPHERING = 3;
	public static int SHORT_NAME_REFERENCING_NO_CIPHERING = 2;
	public static int SHORT_NAME_REFERENCING_WITH_CIPHERING = 4;

	private int ACSE_protocolVersion = 0; // default version1
	private int contextId = 1;
	private int mechanismId = 0;
	private byte[] userInformationData;
	private String callingAuthenticationValue;
	private XdlmsAse xdlmsAse;

	public AssociationControlServiceElement() {
	}

	public AssociationControlServiceElement(int securityLevel, String callingAuthenticationValue, XdlmsAse dlmsAse, int contextId) {
		setCallingAuthenticationValue(callingAuthenticationValue);
		setAuthMechanismId(securityLevel);
		this.xdlmsAse = dlmsAse;
		this.contextId = contextId;
	}

	/**
	 * Create an Association, based on the available variables
	 * @throws IOException
	 */
	public byte[] createAssociationRequest() throws IOException {
//		this.xdlmsAse = dlmsAse;
//		setUserInformation(this.xdlmsAse.getInitiatRequestByteArray());
//		byte[] response = this.connection.sendRequest(addUnusedPrefixBytesForCompliancyWithOldCode(buildAARQApdu()));
//		analyzeAARE(response);
//		if(this.mechanismId >= 2){
//			throw new UnsupportedException("High Level security is not yet supported");
//		}
		setUserInformation(this.xdlmsAse.getInitiatRequestByteArray());
		return addUnusedPrefixBytesForCompliancyWithOldCode(buildAARQApdu());
	}
	
	public byte[] releaseAssociationRequest() throws IOException {
		return addUnusedPrefixBytesForCompliancyWithOldCode(buildRLRQApdu());
	}
	
	/**
	 * FIXME TCPIPConnection strips the first three bytes of the byteArray.
	 */
	private byte[] addUnusedPrefixBytesForCompliancyWithOldCode(byte[] request){
		byte[] r = new byte[request.length+3];
		System.arraycopy(new byte[]{(byte)0xE6,(byte)0xE6,(byte)0x00}, 0, r, 0, 3);
		System.arraycopy(request, 0, r, 3, request.length);
		return r;
	}

	/**
	 * @return the generated AARQ to establish an ApplicationAssociation
	 * @throws IOException
	 */
	protected byte[] buildAARQApdu() throws IOException {
		int t = 0;
		byte[] aarq = new byte[1024]; // TODO fill in the maximum value of the
		// byte
		aarq[t++] = DLMSCOSEMGlobals.AARQ_TAG;
		aarq[t++] = 0; 

		if (getACSEProtocolVersion() != null) { // Optional parameter
			System.arraycopy(getACSEProtocolVersion(), 0, aarq, t,
					getACSEProtocolVersion().length);
			t += getACSEProtocolVersion().length;
		}

		System.arraycopy(getApplicationContextName(), 0, aarq, t,
				getApplicationContextName().length);
		t += getApplicationContextName().length;

		/**
		 * called-AP-title [2] AP-title OPTIONAL, called-AE-qualifier [3]
		 * AE-qualifier OPTIONAL, called-AP-invocation-id [4]
		 * AP-invocation-identifier OPTIONAL, called-AE-invocation-id [5]
		 * AE-invocation-identifier OPTIONAL, calling-AP-title [6] AP-title
		 * OPTIONAL, calling-AE-qualifier [7] AE-qualifier OPTIONAL,
		 * calling-AP-invocation-id [8] AP-invocation-identifier OPTIONAL,
		 * calling-AE-invocation-id [9] AE-invocation-identifier OPTIONAL,
		 * 
		 * All above mentioned attributes are optional in the request. If they
		 * are not used then they are not coded. They are encoded as
		 * printableStrings. TODO encode the above attributes
		 */

		if (this.mechanismId != 0) {
			System.arraycopy(getSenderACSERequirements(), 0, aarq, t,
					getSenderACSERequirements().length);
			t += getSenderACSERequirements().length;

			System.arraycopy(getMechanismName(), 0, aarq, t,
					getMechanismName().length);
			t += getMechanismName().length;

			System.arraycopy(getCallingAuthenticationValue(), 0, aarq, t,
					getCallingAuthenticationValue().length);
			t += getCallingAuthenticationValue().length;

		}

		// TODO implementation-information

		if (getUserInformation() != null) {
			System.arraycopy(getUserInformation(), 0, aarq, t,
					getUserInformation().length);
			t += getUserInformation().length;
		}

		aarq[1] = (byte) (t - 2);
		return ProtocolUtils.getSubArray(aarq, 0, t - 1);
	}
	
	/**
	 * Analyze the responsedata
	 * @param responseData from the device
	 * @throws IOException 
	 */
	protected void analyzeAARE(byte[] responseData) throws IOException {
		int i;
		String strResultSourceDiagnostics = "";

		i = 0;
		while (true) {
			if (responseData[i] == DLMSCOSEMGlobals.AARE_TAG) {
				i += 2; // skip tag & length
				while (true) {
					if (responseData[i] == DLMSCOSEMGlobals.AARE_APPLICATION_CONTEXT_NAME) {
						i++; // skip tag
						i += responseData[i]; // skip length + data
					} // if (responseData[i] == AARE_APPLICATION_CONTEXT_NAME)

					else if (responseData[i] == DLMSCOSEMGlobals.AARE_RESULT) {
						i++; // skip tag
						if ((responseData[i] == 3)
								&& (responseData[i + 1] == 2)
								&& (responseData[i + 2] == 1)
								&& (responseData[i + 3] == 0)) {
							// Result OK
							return;
						}
						i += responseData[i]; // skip length + data
					} // else if (responseData[i] == AARE_RESULT)

					else if (responseData[i] == DLMSCOSEMGlobals.AARE_RESULT_SOURCE_DIAGNOSTIC) {
						i++; // skip tag
						if (responseData[i] == 5) // check length
						{
							if (responseData[i + 1] == DLMSCOSEMGlobals.ACSE_SERVICE_USER) {
								if ((responseData[i + 2] == 3)
										&& (responseData[i + 3] == 2)
										&& (responseData[i + 4] == 1)) {
									if (responseData[i + 5] == 0x00)
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER";
									else if (responseData[i + 5] == 0x01)
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, no reason given";
									else if (responseData[i + 5] == 0x02)
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Application Context Name Not Supported";
									else if (responseData[i + 5] == 0x0B)
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Mechanism Name Not Recognised";
									else if (responseData[i + 5] == 0x0C)
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Mechanism Name Required";
									else if (responseData[i + 5] == 0x0D)
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Failure";
									else if (responseData[i + 5] == 0x0E)
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Required";
									else
										throw new IOException(
												"Application Association Establishment failed, ACSE_SERVICE_USER, unknown result!");
								} else {
									throw new IOException(
											"Application Association Establishment Failed, result_source_diagnostic, ACSE_SERVICE_USER,  wrong tag");
								}
							} // if (responseData[i+1] == ACSE_SERVICE_USER)
							else if (responseData[i + 1] == DLMSCOSEMGlobals.ACSE_SERVICE_PROVIDER) {
								if ((responseData[i + 2] == 3)
										&& (responseData[i + 3] == 2)
										&& (responseData[i + 4] == 1)) {
									if (responseData[i + 5] == 0x00)
										strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER!";
									else if (responseData[i + 5] == 0x01)
										strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER, No Reason Given!";
									else if (responseData[i + 5] == 0x02)
										strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER, No Common ACSE Version!";
									else
										throw new IOException(
												"Application Association Establishment Failed, ACSE_SERVICE_PROVIDER, unknown result");
								} else
									throw new IOException(
											"Application Association Establishment Failed, result_source_diagnostic, ACSE_SERVICE_PROVIDER,  wrong tag");
							} // else if (responseData[i+1] ==
								// ACSE_SERVICE_PROVIDER)
							else
								throw new IOException(
										"Application Association Establishment Failed, result_source_diagnostic,  wrong tag");
						} else {
							throw new IOException(
									"Application Association Establishment Failed, result_source_diagnostic, wrong length");
						}

						i += responseData[i]; // skip length + data
					} // else if (responseData[i] ==
						// AARE_RESULT_SOURCE_DIAGNOSTIC)

					else if (responseData[i] == DLMSCOSEMGlobals.AARE_USER_INFORMATION) {
						i++; // skip tag
						if (responseData[i + 2] > 0) { // length of octet string
							if (DLMSCOSEMGlobals.DLMS_PDU_INITIATE_RESPONSE == responseData[i + 3]) {
								getXdlmsAse().setNegotiatedQOS(responseData[i + 4]);
								getXdlmsAse().setNegotiatedDlmsVersion(responseData[i + 5]);
								getXdlmsAse().setNegotiatedConformance((ProtocolUtils.getInt(responseData, i + 8) & 0x00FFFFFF)); // conformance
																					// has only 3 bytes, 24 bit
								getXdlmsAse().setMaxRecPDUServerSize(ProtocolUtils.getShort(responseData, i + 12));
								getXdlmsAse().setVAAName(ProtocolUtils.getShort(responseData, i + 14));

							} else if (DLMSCOSEMGlobals.DLMS_PDU_CONFIRMED_SERVICE_ERROR == responseData[i + 3]) {
								if (0x01 == responseData[i + 4])
									strResultSourceDiagnostics += ", InitiateError";
								else if (0x02 == responseData[i + 4])
									strResultSourceDiagnostics += ", getStatus";
								else if (0x03 == responseData[i + 4])
									strResultSourceDiagnostics += ", getNameList";
								else if (0x13 == responseData[i + 4])
									strResultSourceDiagnostics += ", terminateUpload";
								else
									throw new IOException(
											"Application Association Establishment Failed, AARE_USER_INFORMATION, unknown ConfirmedServiceError choice");

								if (0x06 != responseData[i + 5])
									strResultSourceDiagnostics += ", No ServiceError tag";

								if (0x00 == responseData[i + 6])
									strResultSourceDiagnostics += "";
								else if (0x01 == responseData[i + 6])
									strResultSourceDiagnostics += ", DLMS version too low";
								else if (0x02 == responseData[i + 6])
									strResultSourceDiagnostics += ", Incompatible conformance";
								else if (0x03 == responseData[i + 6])
									strResultSourceDiagnostics = ", pdu size too short";
								else if (0x04 == responseData[i + 6])
									strResultSourceDiagnostics = ", refused by the VDE handler";
								else
									throw new IOException(
											"Application Association Establishment Failed, AARE_USER_INFORMATION, unknown respons ");
							} else {
								throw new IOException(
										"Application Association Establishment Failed, AARE_USER_INFORMATION, unknown respons!");
							}

						} // if (responseData[i+2] > 0) --> length of the octet
							// string

						i += responseData[i]; // skip length + data
					} // else if (responseData[i] == AARE_USER_INFORMATION)
					else {
						i++; // skip tag
						// Very tricky, suppose we receive a length > 128
						// because of corrupted data,
						// then if we keep byte, it is signed and we can enter a
						// LOOP because length will
						// be subtracted from i!!!
						i += (((int) responseData[i]) & 0x000000FF); // skip
																		// length
																		// +
																		// data
					}

					if (i++ >= (responseData.length - 1)) {
						i = (responseData.length - 1);
						break;
					}
				} // while(true)

			} // if (responseData[i] == AARE_TAG)

			if (i++ >= (responseData.length - 1)) {
				i = (responseData.length - 1);
				break;
			}
		} // while(true)

		throw new IOException("Application Association Establishment Failed"
				+ strResultSourceDiagnostics);
	}
	
	/**
	 * @return the generated RLRQ to release the association
	 */
	protected byte[] buildRLRQApdu() {
		int t = 0;
		byte[] rlrq = new byte[1024]; // TODO fill in the maximum value of the
		// byte
		rlrq[t++] = DLMSCOSEMGlobals.RLRQ_TAG;
		rlrq[t++] = 0; 

		//TODO a Release-Request-Reason and UserInformationField can be added, but they are optional ...
		
		rlrq[1] = (byte) (t - 2);
		return ProtocolUtils.getSubArray(rlrq, 0, t - 1);
	}
	
	protected void analyzeRLRE(byte[] responseData) throws IOException {
		int i;

		i = 0;
		while (true) {
			if (responseData[i] == DLMSCOSEMGlobals.RLRE_TAG) {
				i += 2; // skip tag & length
				while(true){
					if(responseData[i] == DLMSCOSEMGlobals.RLRE_RELEASE_RESPONSE_REASON){
						i++; // skip tag
						if ((responseData[i] == 3)	// length of the response
								&& (responseData[i + 1] == 2) // encoding of INTEGER?
								&& (responseData[i + 2] == 1)) { // length of the integer
							switch(responseData[i + 3]){
							case 0: return; // normal release
							case 1: throw new IOException("Release was not finished.");
							case 30: throw new IOException("Response from the release is userDefined: " + 30);
							default: throw new IOException("Unknown release response");
							}
						}
					}
					
					if (i++ >= (responseData.length - 1)) {
						i = (responseData.length - 1);
						break;
					}
				}
			} 
			
			if (i++ >= (responseData.length - 1)) {
				i = (responseData.length - 1);
				break;
			}
		}
	}

	private byte[] getSenderACSERequirements() {
		byte[] senderACSEReq = new byte[4];
		senderACSEReq[0] = DLMSCOSEMGlobals.AARQ_SENDER_ACSE_REQUIREMENTS;
		senderACSEReq[1] = (byte) 0x02; // length of the following bitString
		senderACSEReq[2] = (byte) 0x07; // coding number of unused bits in the
		// last byte of the BIT STRING
		senderACSEReq[3] = (byte) 0x80; // coding of the authentication
		// functional unit
		return senderACSEReq;
	}

	protected byte[] getUserInformation() {
		if (this.userInformationData != null) {
			byte[] uiData = new byte[this.userInformationData.length + 4];
			uiData[0] = DLMSCOSEMGlobals.AARQ_USER_INFORMATION;
			uiData[1] = (byte) (this.userInformationData.length + 2);
			uiData[2] = (byte) 0x04;// choice for user information
			uiData[3] = (byte) this.userInformationData.length;
			System.arraycopy(this.userInformationData, 0, uiData, 4,
					this.userInformationData.length);
			return uiData;
		}
		return null;
	}

	/**
	 * The applicationContextName is generated from a default objectIdentifier
	 * and two specific bytes
	 * 
	 * @return the byteArray encoded applicationContextName
	 */
	protected byte[] getApplicationContextName() {
		byte[] appContextName = new byte[DEFAULT_OBJECT_IDENTIFIER.length + 6];
		appContextName[0] = DLMSCOSEMGlobals.AARQ_APPLICATION_CONTEXT_NAME;
		appContextName[1] = (byte) 0x09; // length
		appContextName[2] = (byte) 0x06; // choice for application context name
		// ...
		appContextName[3] = (byte) 0x07; // length
		System.arraycopy(DEFAULT_OBJECT_IDENTIFIER, 0, appContextName, 4,
				DEFAULT_OBJECT_IDENTIFIER.length);
		appContextName[DEFAULT_OBJECT_IDENTIFIER.length + 4] = 1; // 1 meaning
		// application
		// context
		appContextName[DEFAULT_OBJECT_IDENTIFIER.length + 5] = (byte) this.contextId;
		return appContextName;
	}

	/**
	 * The mechanism name is generated from a default objectIdentifier and to
	 * specific bytes
	 * 
	 * @return the byteArray encoded mechanism name
	 */
	protected byte[] getMechanismName() {
		byte[] mechanismName = new byte[DEFAULT_OBJECT_IDENTIFIER.length + 4];
		mechanismName[0] = DLMSCOSEMGlobals.AARQ_MECHANISM_NAME;
		mechanismName[1] = (byte) 0x07; // length
		System.arraycopy(DEFAULT_OBJECT_IDENTIFIER, 0, mechanismName, 2,
				DEFAULT_OBJECT_IDENTIFIER.length);
		mechanismName[DEFAULT_OBJECT_IDENTIFIER.length + 2] = 2; // 2 meaning
		// mechanism
		// name
		mechanismName[DEFAULT_OBJECT_IDENTIFIER.length + 3] = (byte) this.mechanismId;
		return mechanismName;
	}

	/**
	 * @return a byteArray containing the callingAuthenticationValue(in other
	 *         words, the password) coded as a graphical string...
	 * @throws ConnectionException when the callingauthenticationvalue is not filled in
	 */
	protected byte[] getCallingAuthenticationValue() throws ConnectionException {
		if(this.callingAuthenticationValue == null){
			throw new ConnectionException("CallingAuthenticationValue is not filled in.");
		}
		byte[] authValue = new byte[this.callingAuthenticationValue.length() + 4];
		authValue[0] = DLMSCOSEMGlobals.AARQ_CALLING_AUTHENTICATION_VALUE;
		authValue[1] = (byte) (this.callingAuthenticationValue.length() + 2);
		authValue[2] = (byte) 0x80; // choice for authentication-information ...
		authValue[3] = (byte) this.callingAuthenticationValue.length();
		for (int i = 0; i < this.callingAuthenticationValue.length(); i++) {
			authValue[4 + i] = (byte) this.callingAuthenticationValue.charAt(i);
		}
		return authValue;
	}

	/**
	 * <pre>
	 * Create the byteArray for the protocolVersion.
	 * If it is the default value(which will mostly be the case) then it's not required to put it in the request
	 * </pre>
	 * 
	 * @return the BER encoded BitString protocolVersion
	 * @throws IOException
	 *             is never throw, but it's defined on the interface
	 */
	private byte[] getACSEProtocolVersion() throws IOException {
		if (this.ACSE_protocolVersion != 0) {
			return new BitString(this.ACSE_protocolVersion)
					.getBEREncodedByteArray();
		}
		return null;
	}

	/**
	 * <p>
	 * Setter for the application context name - context id
	 * <p>
	 * 
	 * @param contextId
	 */
	public void setContextId(int contextId) {
		this.contextId = contextId;
	}

	/**
	 * <p>
	 * Setter for the authentication mechanism - mechanism id
	 * </p>
	 * 
	 * @param mechanismId
	 */
	public void setAuthMechanismId(int mechanismId) {
		this.mechanismId = mechanismId;
	}

	/**
	 * <p>
	 * Setter for the calling authentication value
	 * </p>
	 * 
	 * @param authValue
	 */
	public void setCallingAuthenticationValue(String authValue) {
		this.callingAuthenticationValue = authValue;
	}

	/**
	 * Setter for the userInformation field. If you plan on initiating an
	 * association, then normally this is a xDLMS.initiateRequest()
	 * 
	 * @param userInformation
	 */
	public void setUserInformation(byte[] userInformation) {
		this.userInformationData = userInformation;
	}
	
	protected XdlmsAse getXdlmsAse(){
		if(this.xdlmsAse == null){
			this.xdlmsAse = new XdlmsAse();
		}
		return this.xdlmsAse;
	}
}
