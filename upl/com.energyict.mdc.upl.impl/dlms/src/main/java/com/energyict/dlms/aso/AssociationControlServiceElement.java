package com.energyict.dlms.aso;

import java.io.IOException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.encryption.XDlmsDecryption;
import com.energyict.encryption.XDlmsEncryption;
import com.energyict.protocol.ProtocolUtils;

/**
 * The AssociationControlServiceElement is an application layer protocol to establish and release an association between two entities
 * and to determine the application context of that association.
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
	private byte[] respondingAuthenticationValue;
	private byte[] respondingAPTitle;
	private XdlmsAse xdlmsAse;
	private byte[] callingAPTitle;
	private SecurityContext sc;

	/**
	 * Create a new instance of the AssociationControlServiceElement
	 * @param dlmsAse - the xDLMS_ASE
	 * @param contextId - the applicationContextId which indicates which type of reference(LN/SN) and the use of ciphering
	 * @param mechanismId - the associationAuthenticationMechanism id
	 * @param callingAuthenticationValue - the secret or challenge used for the authenticated association establishment
	 */
	public AssociationControlServiceElement(XdlmsAse xDlmsAse, int contextId, SecurityContext securityContext) {
		this.xdlmsAse = xDlmsAse;
		this.contextId = contextId;
		this.mechanismId = securityContext.getAuthenticationLevel();
		this.sc = securityContext;
	}

	private byte[] getCallingAuthenticationValue() {
		try {
			return sc.getSecurityProvider().getCallingAuthenticationValue();
		} catch (IOException e) {
			return null;
		}
	}

	public SecurityContext getSecurityContext() {
		return sc;
	}

	@Override
	public String toString() {
		final Object crlf = "\r\n";
		StringBuffer sb = new StringBuffer();
		sb.append("AssociationControlServiceElement").append(crlf);
		sb.append(" > xdlmsAse = ").append(xdlmsAse != null ? xdlmsAse.toString().replace("\r\n", "") : "null").append(crlf);
		sb.append(" > contextId = ").append(contextId).append(crlf);
		sb.append(" > mechanismId = ").append(mechanismId).append(crlf);
		sb.append(" > callingAuthenticationValue = ").append(getCallingAuthenticationValue() != null ? ProtocolUtils.getResponseData(getCallingAuthenticationValue()) : "null").append(crlf);
		sb.append(crlf);
		return sb.toString();
	}

	/**
	 * Create an Association, based on the available variables
	 * @throws IOException
	 */
	public byte[] createAssociationRequest() throws IOException {
		byte[] userInformation = this.xdlmsAse.getInitiatRequestByteArray();

		switch (getSecurityContext().getSecurityPolicy()) {
			case SecurityContext.SECURITYPOLICY_BOTH:
			case SecurityContext.SECURITYPOLICY_ENCRYPTION:
				XDlmsEncryption xdlmsEncryption = new XDlmsEncryption();
				xdlmsEncryption.setPlainText(userInformation);
				xdlmsEncryption.setSystemTitle(getSecurityContext().getSystemTitle());
				xdlmsEncryption.setFrameCounter(getSecurityContext().getFrameCounterInBytes());
				xdlmsEncryption.setAuthenticationKey(getSecurityContext().getSecurityProvider().getAuthenticationKey());
				xdlmsEncryption.setGlobalKey(getSecurityContext().getSecurityProvider().getGlobalKey());
				xdlmsEncryption.setSecurityControlByte((byte) 0x30);
				userInformation = xdlmsEncryption.generateCipheredAPDU();
				getSecurityContext().incFrameCounter();
		}

		setUserInformation(userInformation);
		return addUnusedPrefixBytesForCompliancyWithOldCode(buildAARQApdu());
	}

	public byte[] getCallingAPTitle() {
		return callingAPTitle;
	}

	public void setCallingAPTitle(byte[] callingAPTitle) {
		this.callingAPTitle = callingAPTitle;
	}

	/**
	 * Release the current association
	 * @return a byteArray containing an ApplicationAssociationReleaseRequest
	 * @throws IOException
	 */
	public byte[] releaseAssociationRequest() throws IOException {
		return addUnusedPrefixBytesForCompliancyWithOldCode(buildRLRQApdu());
	}

	/**
	 * FIXME TCPIPConnection strips the first three bytes of the byteArray.
	 * This method add three redundant bytes in front of your array to be compliant with old implementation.
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
		byte[] aarq = new byte[1024];
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
		 * called-AP-title [2] AP-title OPTIONAL,
		 * called-AE-qualifier [3]
		 * AE-qualifier OPTIONAL, called-AP-invocation-id [4]
		 * AP-invocation-identifier OPTIONAL, called-AE-invocation-id [5]
		 * AE-invocation-identifier OPTIONAL,
		 *
		 * TODO for application contexts using ciphering, the calling-AP-title field shall carry the CLIENT-SYSTEM-TITLE
		 * calling-AP-title [6] AP-title
		 *
		 *
		 * OPTIONAL, calling-AE-qualifier [7] AE-qualifier OPTIONAL,
		 * calling-AP-invocation-id [8] AP-invocation-identifier OPTIONAL,
		 * calling-AE-invocation-id [9] AE-invocation-identifier OPTIONAL,
		 *
		 * All above mentioned attributes are optional in the request. If they
		 * are not used then they are not coded. They are encoded as
		 * printableStrings. TODO encode the above attributes
		 */

		if (generateCallingAPTitleField() != null) {
			System.arraycopy(generateCallingAPTitleField(), 0, aarq, t, generateCallingAPTitleField().length);
			t += generateCallingAPTitleField().length;
		}

		if (this.mechanismId != 0) {
			System.arraycopy(getSenderACSERequirements(), 0, aarq, t,
					getSenderACSERequirements().length);
			t += getSenderACSERequirements().length;

			System.arraycopy(getMechanismName(), 0, aarq, t,
					getMechanismName().length);
			t += getMechanismName().length;

			System.arraycopy(assembleCallingAuthenticationValue(), 0, aarq, t,
					assembleCallingAuthenticationValue().length);
			t += assembleCallingAuthenticationValue().length;

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
	 * Analyze the responseData
	 * @param responseData from the device
	 * @throws IOException
	 */

	protected void analyzeAARE(byte[] responseData) throws IOException {
		int i = 0;
		String strResultSourceDiagnostics = "";
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
//							return;	 //Don't return otherwise you don't get all info
							i += responseData[i]; // skip length + data
						} else {
							// the result wasn't OK, but we keep going so we get the proper info
							i += responseData[i]; // skip length + data
						}
					} // else if (responseData[i] == AARE_RESULT)

					else if(responseData[i] == DLMSCOSEMGlobals.AARE_RESPONING_AP_TITLE){
						i++; // skip tag
						if (responseData[i] > 0) { // length of octet string
							this.respondingAPTitle = ProtocolUtils.getSubArray2(responseData, i+3, responseData[i+2]);
						}
						i += responseData[i];
					}

					else if (responseData[i] == DLMSCOSEMGlobals.AARE_RESULT_SOURCE_DIAGNOSTIC) {
						i++; // skip tag
						if (responseData[i] == 5) // check length
						{
							if (responseData[i + 1] == DLMSCOSEMGlobals.ACSE_SERVICE_USER) {
								if ((responseData[i + 2] == 3)
										&& (responseData[i + 3] == 2)
										&& (responseData[i + 4] == 1)) {
									if (responseData[i + 5] == 0x00){
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER";
									}
									else if (responseData[i + 5] == 0x01){
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, no reason given";
										throw new IOException("Application Association Establishment Failed"
												+ strResultSourceDiagnostics);
									}
									else if (responseData[i + 5] == 0x02){
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Application Context Name Not Supported";
										throw new IOException("Application Association Establishment Failed"
												+ strResultSourceDiagnostics);
									}
									else if (responseData[i + 5] == 0x0B){
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Mechanism Name Not Recognised";
										throw new IOException("Application Association Establishment Failed"
												+ strResultSourceDiagnostics);
									}
									else if (responseData[i + 5] == 0x0C){
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Mechanism Name Required";
										throw new IOException("Application Association Establishment Failed"
												+ strResultSourceDiagnostics);
									}
									else if (responseData[i + 5] == 0x0D){
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Failure";
										throw new IOException("Application Association Establishment Failed"
												+ strResultSourceDiagnostics);
									}
									else if (responseData[i + 5] == 0x0E) {
										strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Required";
									} else {
										throw new IOException(
												"Application Association Establishment failed, ACSE_SERVICE_USER, unknown result!");
									}
								} else {
									throw new IOException(
											"Application Association Establishment Failed, result_source_diagnostic, ACSE_SERVICE_USER,  wrong tag");
								}
							} // if (responseData[i+1] == ACSE_SERVICE_USER)
							else if (responseData[i + 1] == DLMSCOSEMGlobals.ACSE_SERVICE_PROVIDER) {
								if ((responseData[i + 2] == 3)
										&& (responseData[i + 3] == 2)
										&& (responseData[i + 4] == 1)) {
									if (responseData[i + 5] == 0x00) {
										strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER!";
									} else if (responseData[i + 5] == 0x01) {
										strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER, No Reason Given!";
									} else if (responseData[i + 5] == 0x02) {
										strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER, No Common ACSE Version!";
									} else {
										throw new IOException(
												"Application Association Establishment Failed, ACSE_SERVICE_PROVIDER, unknown result");
									}
								} else {
									throw new IOException(
											"Application Association Establishment Failed, result_source_diagnostic, ACSE_SERVICE_PROVIDER,  wrong tag");
								}
							} // else if (responseData[i+1] ==
								// ACSE_SERVICE_PROVIDER)
							else {
								throw new IOException(
										"Application Association Establishment Failed, result_source_diagnostic,  wrong tag");
							}
						} else {
							throw new IOException(
									"Application Association Establishment Failed, result_source_diagnostic, wrong length");
						}

						i += responseData[i]; // skip length + data
					} // else if (responseData[i] == AARE_RESULT_SOURCE_DIAGNOSTIC)

					else if (responseData[i] == DLMSCOSEMGlobals.AARE_MECHANISM_NAME){
						i++; //skip tag
						if(responseData[i + 7] != this.mechanismId){
							throw new IOException("Application Association Establishment Failed, mechanim_id("+ responseData[i+7] +"),  different then proposed(" + this.mechanismId + ")");
						}
						i += responseData[i]; // skip length + data
					}

					else if (responseData[i] == DLMSCOSEMGlobals.AARE_RESPONDING_AUTHENTICATION_VALUE){
						i++; //skip tag

						if(responseData[i + 1] == (byte)0x80){ // encoding choice for GraphicString
							setRespondingAuthenticationValue(ProtocolUtils.getSubArray2(responseData, i+3, responseData[i+2]));
						}

						i += responseData[i]; // skip length + data
					}

					else if (responseData[i] == DLMSCOSEMGlobals.AARE_USER_INFORMATION) {
						i++; // skip tag

						if (responseData[i + 2] > 0) { // length of octet string

							/*
							 * Check if the userinformation field is encrypted,
							 * and if so, replace the encrypted part with the
							 * plain text for furter parsing
							 */
							if (DLMSCOSEMGlobals.AARE_GLOBAL_INITIATE_RESPONSE_TAG == responseData[i + 3]) {
								byte[] encryptedUserInformation = new byte[responseData.length - (i + 4)];
								System.arraycopy(responseData, i + 4, encryptedUserInformation, 0, encryptedUserInformation.length);
								byte[] ui = decryptUserInformation(encryptedUserInformation);
								System.arraycopy(ui, 0, responseData, i+3, ui.length);
								responseData = ProtocolUtils.getSubArray(responseData, 0, i + 2 + ui.length);
							}

							if (DLMSCOSEMGlobals.DLMS_PDU_INITIATE_RESPONSE == responseData[i + 3]) {
								getXdlmsAse().setNegotiatedQOS(responseData[i + 4]);
								getXdlmsAse().setNegotiatedDlmsVersion(responseData[i + 5]);
								getXdlmsAse().setNegotiatedConformance((ProtocolUtils.getInt(responseData, i + 8) & 0x00FFFFFF)); // conformance has only 3 bytes, 24 bit
								getXdlmsAse().setMaxRecPDUServerSize(ProtocolUtils.getShort(responseData, i + 12));
								getXdlmsAse().setVAAName(ProtocolUtils.getShort(responseData, i + 14));
								return;

							} else if (DLMSCOSEMGlobals.DLMS_PDU_CONFIRMED_SERVICE_ERROR == responseData[i + 3]) {
								if (0x01 == responseData[i + 4]) {
									strResultSourceDiagnostics += ", InitiateError";
								} else if (0x02 == responseData[i + 4]) {
									strResultSourceDiagnostics += ", getStatus";
								} else if (0x03 == responseData[i + 4]) {
									strResultSourceDiagnostics += ", getNameList";
								} else if (0x13 == responseData[i + 4]) {
									strResultSourceDiagnostics += ", terminateUpload";
								} else {
									throw new IOException(
											"Application Association Establishment Failed, AARE_USER_INFORMATION, unknown ConfirmedServiceError choice");
								}

								if (0x06 != responseData[i + 5]) {
									strResultSourceDiagnostics += ", No ServiceError tag";
								}

								if (0x00 == responseData[i + 6]) {
									strResultSourceDiagnostics += "";
								} else if (0x01 == responseData[i + 6]) {
									strResultSourceDiagnostics += ", DLMS version too low";
								} else if (0x02 == responseData[i + 6]) {
									strResultSourceDiagnostics += ", Incompatible conformance";
								} else if (0x03 == responseData[i + 6]) {
									strResultSourceDiagnostics = ", pdu size too short";
								} else if (0x04 == responseData[i + 6]) {
									strResultSourceDiagnostics = ", refused by the VDE handler";
								} else {
									throw new IOException("Application Association Establishment Failed, AARE_USER_INFORMATION, unknown respons ");
								}
							} else {
								throw new IOException("Application Association Establishment Failed, AARE_USER_INFORMATION, unknown respons!");
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
	 * @param encryptedUserInformation
	 * @param offset
	 * @throws IOException
	 */
	private byte[] decryptUserInformation(byte[] encryptedUserInformation) throws IOException {
		int ptr = 0;
		byte length = encryptedUserInformation[ptr++];
		byte scb = encryptedUserInformation[ptr++];
		byte[] fc = new byte[4];
		byte[] at = new byte[12];
		for (int j = 0; j < fc.length; j++) {
			fc[j] = encryptedUserInformation[ptr++];
		}

		int ctLen = length - fc.length - at.length - 1;
		byte[] ct = new byte[ctLen];
		for (int j = 0; j < ct.length; j++) {
			ct[j] = encryptedUserInformation[ptr++];
		}

		for (int j = 0; j < at.length; j++) {
			at[j] = encryptedUserInformation[ptr++];
		}

		XDlmsDecryption decryption = new XDlmsDecryption();
		decryption.setAuthenticationKey(getSecurityContext().getSecurityProvider().getAuthenticationKey());
		decryption.setGlobalKey(getSecurityContext().getSecurityProvider().getGlobalKey());
		decryption.setAuthenticationTag(at);
		decryption.setCipheredText(ct);
		decryption.setFrameCounter(fc);
		decryption.setSecurityControlByte(scb);
		decryption.setSystemTitle(respondingAPTitle);
		return decryption.generatePlainText();
	}

	/**
	 * @return the generated RLRQ to release the association
	 */
	protected byte[] buildRLRQApdu() {
		int t = 0;
		byte[] rlrq = new byte[1024];
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
		senderACSEReq[2] = (byte) 0x07; // coding number of unused bits in the last byte of the BIT STRING
		senderACSEReq[3] = (byte) 0x80; // coding of the authentication functional unit
		return senderACSEReq;
	}

	protected byte[] getUserInformation() {
		if (this.userInformationData != null) {
			byte[] uiData = new byte[this.userInformationData.length + 4];
			uiData[0] = DLMSCOSEMGlobals.AARQ_USER_INFORMATION;
			uiData[1] = (byte) (this.userInformationData.length + 2);
			uiData[2] = (byte) 0x04;// choice for user information
			uiData[3] = (byte) this.userInformationData.length;
			System.arraycopy(this.userInformationData, 0, uiData, 4, this.userInformationData.length);
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
		appContextName[2] = (byte) 0x06; // choice for application context name ...
		appContextName[3] = (byte) 0x07; // length
		System.arraycopy(DEFAULT_OBJECT_IDENTIFIER, 0, appContextName, 4, DEFAULT_OBJECT_IDENTIFIER.length);
		appContextName[DEFAULT_OBJECT_IDENTIFIER.length + 4] = 1; // 1 meaning application context
		appContextName[DEFAULT_OBJECT_IDENTIFIER.length + 5] = (byte) this.contextId;
		return appContextName;
	}

	/**
	 * @return
	 */
	private byte[] generateCallingAPTitleField() {
		if (getCallingAPTitle() != null) {
			byte[] callingAppTitleField = new byte[getCallingAPTitle().length + 4];
			callingAppTitleField[0] = DLMSCOSEMGlobals.AARE_CALLING_AP_TITLE;
			callingAppTitleField[1] = (byte) (callingAppTitleField.length - 2); // length
			callingAppTitleField[2] = (byte) 0x04; // choice for calling app title
			callingAppTitleField[3] = (byte) callingAppTitleField.length; // length
			System.arraycopy(getCallingAPTitle(), 0, callingAppTitleField, 4, getCallingAPTitle().length);
			return callingAppTitleField;
		} else {
			return null;
		}
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
		System.arraycopy(DEFAULT_OBJECT_IDENTIFIER, 0, mechanismName, 2, DEFAULT_OBJECT_IDENTIFIER.length);
		mechanismName[DEFAULT_OBJECT_IDENTIFIER.length + 2] = 2; // 2 meaning mechanism name
		mechanismName[DEFAULT_OBJECT_IDENTIFIER.length + 3] = (byte) this.mechanismId;
		return mechanismName;
	}

	/**
	 * @return a byteArray containing the callingAuthenticationValue(in other
	 *         words, the password) coded as a graphical string...
	 * @throws ConnectionException when the callingauthenticationvalue is not filled in
	 */
	protected byte[] assembleCallingAuthenticationValue() throws ConnectionException {
		if(getCallingAuthenticationValue() == null){
			throw new ConnectionException("CallingAuthenticationValue is not filled in.");
		}
		byte[] authValue = new byte[getCallingAuthenticationValue().length + 4];
		authValue[0] = DLMSCOSEMGlobals.AARQ_CALLING_AUTHENTICATION_VALUE;
		authValue[1] = (byte) (getCallingAuthenticationValue().length + 2);
		authValue[2] = (byte) 0x80; // choice for authentication-information ...
		authValue[3] = (byte) getCallingAuthenticationValue().length;
		System.arraycopy(getCallingAuthenticationValue(), 0, authValue, 4, getCallingAuthenticationValue().length);
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
			return new BitString(this.ACSE_protocolVersion).getBEREncodedByteArray();
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
	 * Setter for the userInformation field. If you plan on initiating an
	 * association, then normally this is a xDLMS.initiateRequest()
	 *
	 * @param userInformation
	 */
	public void setUserInformation(byte[] userInformation) {
		this.userInformationData = userInformation.clone();
	}

	/**
	 * @return the current xDLMS_ASE
	 */
	protected XdlmsAse getXdlmsAse(){
		if(this.xdlmsAse == null){
			this.xdlmsAse = new XdlmsAse();
		}
		return this.xdlmsAse;
	}

	/**
	 * @return the authenticaionValue(challenge) from the server
	 */
	protected byte[] getRespondingAuthenticationValue(){
		return this.respondingAuthenticationValue;
	}

	/**
	 * Set the authenticationValue(challenge) from the server
	 * @param respondingAuthenticationValue - the challenge from the server
	 */
	protected void setRespondingAuthenticationValue(byte[] respondingAuthenticationValue){
		this.respondingAuthenticationValue = respondingAuthenticationValue;
	}

	/**
	 * @return the applicationContextId
	 */
	public int getContextId() {
		return this.contextId;
	}

	protected void setRespondingAPTitle(byte[] respondingAPTitle){
		this.respondingAPTitle = respondingAPTitle;
	}

	public byte[] getRespondingAPTtitle(){
		return this.respondingAPTitle;
	}
}
