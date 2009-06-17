package com.energyict.dlms.aso;

import java.io.IOException;
import java.util.Arrays;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.AssociationSN;
import com.energyict.protocol.ProtocolUtils;

/**
 * 
 * @author gna
 * 
 *<pre>
 * The applicationServiceObject has three main objectives.
 * - Establish/maintain/release application associations
 * - Data transfer
 * - Layer management...
 * </pre>
 */
public class ApplicationServiceObject {

	protected XdlmsAse xDlmsAse;
	protected AssociationControlServiceElement acse;
	private SecurityProvider securityProvider;
	protected ProtocolLink protocolLink;
	
	public static String ALGORITHM_MD5 = "MD5"; 
	public static String ALGORITHM_SHA1 = "SHA-1";
	public static String ALGORITHM_GMAC = "GMAC";
	
	public ApplicationServiceObject(XdlmsAse xDlmsAse, ProtocolLink protocolLink, SecurityProvider securityProvider, int contextId) throws IOException{
		this.xDlmsAse = xDlmsAse;
		this.protocolLink = protocolLink;
		this.securityProvider = securityProvider;
		this.acse = new AssociationControlServiceElement(this.xDlmsAse, contextId, this.securityProvider.getSecurityLevel(), this.securityProvider.getCallingAuthenticationValue());
	}
	
	/*******************************************************************************************************
	 * Application association management
	 *******************************************************************************************************/
	
	/**
	 * Create an ApplicationAssociation.
	 * Depending on the securityLevel encrypted challenges will be used to authenticate the client and server
	 * 
	 */
	public void createAssociation() throws IOException{
		byte[] request = this.acse.createAssociationRequest();
		byte[] response = this.protocolLink.getDLMSConnection().sendRequest(request);
		this.acse.analyzeAARE(response);
		handleHighLevelSecurityAuthentication();

	}
	
	protected void handleHighLevelSecurityAuthentication() throws IOException {
		byte[] encryptedResponse;
		byte[] plainText;
		switch(this.securityProvider.getSecurityLevel()){
		case 0: break;
		case 1: break;
		case 2: throw new IOException("High level security 2 is not supported.");
		case 3:{
			if(this.acse.getRespondingAuthenticationValue() != null){
				plainText = ProtocolUtils.concatByteArrays(this.acse.getRespondingAuthenticationValue(), this.securityProvider.getHLSSecret());
				encryptedResponse = replyToHLSAuthentication(this.securityProvider.encrypt(plainText));
				analyzeEncryptedResponse(ALGORITHM_MD5, encryptedResponse);
			} else {
				throw new ConnectionException("No challenge was responded; Current securityLevel(" + this.securityProvider.getSecurityLevel() +
				") requires the server to respond with a challenge.");
			}
		};break;
		case 4:{
			if(this.acse.getRespondingAuthenticationValue() != null){
				plainText = ProtocolUtils.concatByteArrays(this.acse.getRespondingAuthenticationValue(), this.securityProvider.getHLSSecret());
				encryptedResponse = replyToHLSAuthentication(this.securityProvider.encrypt(plainText));
				analyzeEncryptedResponse(ALGORITHM_SHA1, encryptedResponse);
			} else {
				throw new ConnectionException("No challenge was responded; Current securityLevel(" + this.securityProvider.getSecurityLevel() +
				") requires the server to respond with a challenge.");
			}
		};break;
		case 5:{
			//TODO  implement the GMAC authentication
			throw new IOException("High level security 5 (GMAC) is not supported YET.");
		}default:{
			// should never get here
			throw new ConnectionException("Unknown securitylevel: " + this.securityProvider.getSecurityLevel());
		}
		}
	}

	/**
	 * Encrypt the clientToServer challenge and compare it with the encrypted response from the server
	 * @param algorithm - name of the algorithm to use to encrypt the challenge
	 * @param encryptedResponse is the response from the server to the reply_to_HLS_authentication
	 * @throws IOException if the two challenges don't match, or if the HLSSecret could be supplied, if it's not a valid algorithm or when there is no callingAuthenticationvalue
	 */
	private void analyzeEncryptedResponse(String algorithm, byte[] encryptedResponse) throws IOException {
		byte[] plainText = ProtocolUtils.concatByteArrays(this.securityProvider.getCallingAuthenticationValue(), this.securityProvider.getHLSSecret());
		byte[] cToSEncrypted = this.securityProvider.encrypt(plainText);
		if(!Arrays.equals(cToSEncrypted, encryptedResponse)){
			throw new IOException("HighLevelAuthentication failed, client and server challenges do not match.");
		}
	}

	/**
	 * Send a reply to the server using the AssociationLN/SN method 'reply_to_HLS_authentication'
	 * @param digest - the 'encrypted' ServerToClient challenge
	 * @return the encrypted response, which should contain the cToS authenticationValue
	 * @throws IOException
	 */
	private byte[] replyToHLSAuthentication(byte[] digest) throws IOException {
		OctetString encryptedResponse = null;
		if((this.acse.getContextId() == 1) || (this.acse.getContextId() == 3)){			// reply with AssociationLN
			AssociationLN aln = new AssociationLN(this.protocolLink);
			encryptedResponse = new OctetString(aln.replyToHLSAuthentication(digest));
		} else if((this.acse.getContextId() == 2) || (this.acse.getContextId() == 4)){	// reply with AssociationSN 
			AssociationSN asn = new AssociationSN(this.protocolLink);
			encryptedResponse = new OctetString(asn.replyToHLSAuthentication(digest));
		}
		return encryptedResponse.getContentBytes();
	}

	/**
	 * Release the current association
	 * @throws IOException
	 */
	public void releaseAssociation() throws IOException{
		byte[] request = this.acse.releaseAssociationRequest();
		byte[] response = this.protocolLink.getDLMSConnection().sendRequest(request);
		this.acse.analyzeRLRE(response);
	}
	
	/*******************************************************************************************************/
}