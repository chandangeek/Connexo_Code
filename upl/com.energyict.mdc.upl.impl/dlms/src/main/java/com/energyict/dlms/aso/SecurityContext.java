package com.energyict.dlms.aso;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.ProtocolUtils;

/**
 * The securityContext manages the different securityLevels for establishing associations and dataTransport
 * 
 * @author gna
 *
 */
public class SecurityContext {

	/**
	 * Holds the securityLevel for the DataTransport.
	 * Possible values are:
	 * <pre>
	 * - 0 : Security not imposed
	 * - 1 : All messages(APDU's) must be authenticated
	 * - 2 : All messages(APDU's) must be encrypted
	 * - 3 : All messages(APDU's) must be authenticated AND encrypted
	 * </pre>
	 */
	private int securityPolicy;
	
	/**
	 * Points to the encryption Method that has to be used for dataTransport.
	 * Currently only 0 (meaning AES-GCM-128) is allowed
	 */
	private int securitySuite;
	
	/**
	 * Holds the securityLevel for the Authentication mechanism used during Association Establishment
	 */
	private int authenticationLevel;
	
	/**
	 * The provider containing all the keys that may be used during an Authenticated/Encrypted communication
	 */
	private SecurityProvider securityProvider;
	
	protected long frameCounter;
	private String systemTitle;
	
	private String[] authenticationEncryptions = new String[]{"","","","MD5","SHA-1","GMAC"};
	private String authenticationAlgorithm;
	
	/**
	 * @param dataTransportSecurityLevel - SecurityLevel during data transport
	 * @param associationAuthenticationLevel -  SecurityLevel during associationEstablishment
	 * @param dataTransportEncryptionType - Which type of security to use during data transport
	 * @param systemTitle - the server his logicalDeviceName, used for the construction of the initializationVector
	 * @param securityProvider - The securityProvider holding the keys
	 */
	public SecurityContext(int dataTransportSecurityLevel, int associationAuthenticationLevel, int dataTransportEncryptionType, String systemTitle, SecurityProvider securityProvider){
		this.securityPolicy = dataTransportSecurityLevel;
		this.authenticationLevel = associationAuthenticationLevel;
		this.securitySuite = dataTransportEncryptionType;
		this.securityProvider = securityProvider;
		this.authenticationAlgorithm = authenticationEncryptions[this.authenticationLevel];
		this.frameCounter = 0;
		this.systemTitle = systemTitle;
	}

	/**
	 * Get the security level for dataTransport
	 * @return the securityPolicy
	 */
	public int getSecurityPolicy() {
		return securityPolicy;
	}

	/**
	 * Get the type of encryption used for dataTransport
	 * @return the securitySuite
	 */
	public int getSecuritySuite() {
		return securitySuite;
	}

	/**
	 * Get the authentication level used during the Association Establishment
	 * @return the authenticationLevel
	 */
	public int getAuthenticationLevel() {
		return authenticationLevel;
	}

	/**
	 * Get the securityKeyProvider
	 * @return the securityProvider
	 */
	public SecurityProvider getSecurityProvider() {
		return securityProvider;
	}
	
	/**
	 * @param plainText - the text to encrypt ...
	 * @return the cihperText
	 * @throws IOException when the desired Encryption algorithm isn't supported
	 */
	public byte[] associationEncryption(byte[] plainText) throws IOException{
		try {
			byte[] digest;
			MessageDigest md = MessageDigest.getInstance(this.authenticationAlgorithm);
			md.reset();
			digest = md.digest(plainText);
			return digest;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new IOException("" + this.authenticationAlgorithm + " algorithm isn't a valid algorithm type." + e.getMessage());
		}
	}
	
	/**
	 * @param plainText - the text to encrypt ...
	 * @return the cipherText (or the plainText when no security has to be applied)
	 * @throws ConnectionException
	 */
	public byte[] dataTransportEncryption(byte[] plainText) throws ConnectionException{
		
		try {
			// TODO complete
			switch (this.securityPolicy) {
			case 0: {
				return plainText;
			} // no encryption/authentication
			case 1: {
				throw new ConnectionException("Current securityLevel ("
						+ this.securityPolicy
						+ " - authenticate all messages) is not supported yet.");
			} // authenticated
			case 2: {
				throw new ConnectionException("Current securityLevel ("
						+ this.securityPolicy
						+ " - encrypt all messages) is not supported yet.");
			} // encrypted
			case 3: {
				throw new ConnectionException(
						"Current securityLevel ("
								+ this.securityPolicy
								+ " - authenticate AND encrypt all messages) is not supported yet.");
			} // authenticated and encrypted
			default:
				throw new ConnectionException("Unknown securityPolicy: "
						+ this.securityPolicy);
			}
		} finally {
			this.frameCounter++;
		}
	}
	
	/**
	 * Generate the initializationVector, based on:
	 * <pre>
	 * - the SysTitle, which is the ASCII representation of the first 3 chars of the logical device name, concatenated with the hex value of his trailing serialnumber
	 * - the hex representation of the frameCounter
	 * </pre> 
	 * @return a byteArray containing the frameCounter
	 */
	protected byte[] getInitializationVector(){
		String manufacturer = this.systemTitle.substring(0, 3);
		long uniqueNumber = Long.valueOf(getLargestIntFromString(this.systemTitle));
		
		byte[] iv = manufacturer.getBytes();
		byte[] un = new byte[5];
		byte[] fc = new byte[4];
		
		for(int i = 0; i < un.length; i++){
			un[un.length-1-i] = (byte)((uniqueNumber>>(i*8))&0xff);
		}
		
		for(int i = 0; i < fc.length; i++){
			fc[fc.length-1-i] = (byte)((this.frameCounter>>(i*8))&0xff);
		}
		
		iv = ProtocolUtils.concatByteArrays(iv, un);
		iv = ProtocolUtils.concatByteArrays(iv, fc);
		return iv;
	}
	
	/**
	 * HelperMethod to check for the largest trailing number in the logical device name
	 * <pre>
	 * ex.
	 * - ISKT372M40581297 -> 40581297
	 * - KAMM1436321499 -> 1436321499
	 * </pre>
	 * @param str is the String which contains the number
	 * @return a string containing only a number
	 */
	protected String getLargestIntFromString(String str){
		for(int i = 0; i < str.length(); i++){
			if(ProtocolUtils.isInteger(str.substring(i))){
				return str.substring(i);
			}
		}
		return "";
	}
}
