package com.energyict.dlms.aso;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.encryption.AesGcm128;
import com.energyict.encryption.BitVector;
import com.energyict.protocol.ProtocolUtils;

/**
 * The securityContext manages the different securityLevels for establishing
 * associations and dataTransport
 * 
 * @author gna
 * 
 */
public class SecurityContext {

	/**
	 * Holds the securityLevel for the DataTransport. Possible values are:
	 * 
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
	 * Holds the securityLevel for the Authentication mechanism used during
	 * Association Establishment
	 */
	private int authenticationLevel;

	/**
	 * The provider containing all the keys that may be used during an
	 * Authenticated/Encrypted communication
	 */
	private SecurityProvider securityProvider;

	private long frameCounter;
	private String systemIdentifier;

	private String[] authenticationEncryptions = new String[] { "", "", "",
			"MD5", "SHA-1", "GMAC" };
	private String authenticationAlgorithm;

	/**
	 * @param dataTransportSecurityLevel
	 *            - SecurityLevel during data transport
	 * @param associationAuthenticationLevel
	 *            - SecurityLevel during associationEstablishment
	 * @param dataTransportEncryptionType
	 *            - Which type of security to use during data transport
	 * @param systemIdentifier
	 *            - the server his logicalDeviceName, used for the construction
	 *            of the initializationVector (ex. KAMM1436321499)
	 * @param securityProvider
	 *            - The securityProvider holding the keys
	 */
	public SecurityContext(int dataTransportSecurityLevel,
			int associationAuthenticationLevel,
			int dataTransportEncryptionType, String systemIdentifier,
			SecurityProvider securityProvider) {
		this.securityPolicy = dataTransportSecurityLevel;
		this.authenticationLevel = associationAuthenticationLevel;
		this.securitySuite = dataTransportEncryptionType;
		this.securityProvider = securityProvider;
		this.authenticationAlgorithm = authenticationEncryptions[this.authenticationLevel];
		this.frameCounter = 0;
		this.systemIdentifier = systemIdentifier;
	}

	/**
	 * Get the security level for dataTransport
	 * 
	 * @return the securityPolicy
	 */
	public int getSecurityPolicy() {
		return securityPolicy;
	}

	/**
	 * Get the type of encryption used for dataTransport
	 * 
	 * @return the securitySuite
	 */
	public int getSecuritySuite() {
		return securitySuite;
	}

	/**
	 * Get the authentication level used during the Association Establishment
	 * 
	 * @return the authenticationLevel
	 */
	public int getAuthenticationLevel() {
		return authenticationLevel;
	}

	/**
	 * Get the securityKeyProvider
	 * 
	 * @return the securityProvider
	 */
	public SecurityProvider getSecurityProvider() {
		return securityProvider;
	}

	/**
	 * @param plainText
	 *            - the text to encrypt ...
	 * @return the cihperText
	 * @throws IOException
	 *             when the desired Encryption algorithm isn't supported
	 */
	public byte[] associationEncryption(byte[] plainText) throws IOException {
		try {
			byte[] digest;
			MessageDigest md = MessageDigest
					.getInstance(this.authenticationAlgorithm);
			md.reset();
			digest = md.digest(plainText);
			return digest;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new IOException("" + this.authenticationAlgorithm
					+ " algorithm isn't a valid algorithm type."
					+ e.getMessage());
		}
	}

	/**
	 * @param plainText
	 *            - the text to encrypt ...
	 * @return the cipherText (or the plainText when no security has to be
	 *         applied)
	 * @throws IOException 
	 */
	public byte[] dataTransportEncryption(byte[] plainText)
			throws IOException {

		try {
			// TODO complete
			switch (this.securityPolicy) {
			case 0: {
				return plainText;
			} // no encryption/authentication
			case 1: {
				
				try {
					AesGcm128 ag128 = new AesGcm128(getSecurityProvider().getGlobalKey());
					
					/* the associatedData is a concatenation of:
					 * - the securityControlByte
					 * - the authenticationKey
					 * - the plainText */
					byte[] associatedData = new byte[plainText.length + getSecurityProvider().getAuthenticationKey().length + 1];
					associatedData[0] = getSecurityControlByte();
					System.arraycopy(getSecurityProvider().getAuthenticationKey(), 0, associatedData, 1, getSecurityProvider().getAuthenticationKey().length);
					System.arraycopy(plainText, 0, associatedData, 1+getSecurityProvider().getAuthenticationKey().length, plainText.length);
					
					
					ag128.setAdditionalAuthenticationData(new BitVector(associatedData));
					ag128.setInitializationVector(new BitVector(getInitializationVector()));

					ag128.encrypt();
					
					/* 1 for length, 1 for controlByte, 4 for frameCounter, length of plainText
					 * and 12 for the AuthenticationTag (normally this is 16byte, but the securitySpec said it had to be 12)*/
					byte[] securedApdu = new byte[1 + 1 + 4 + plainText.length + 12];
					securedApdu[0] = (byte) (securedApdu.length-1);
					securedApdu[1] = getSecurityControlByte();
					System.arraycopy(getFrameCounterInBytes(), 0, securedApdu, 2, getFrameCounterInBytes().length);
					System.arraycopy(plainText, 0, securedApdu, 2+getFrameCounterInBytes().length, plainText.length);
					System.arraycopy(ProtocolUtils.getSubArray(ag128.getTag().getValue(), 0, 11), 0, securedApdu, 2+getFrameCounterInBytes().length+plainText.length, 12);
					return securedApdu;
					
				} catch (IOException e) {
					e.printStackTrace();
					throw new IOException("Could not retrieve the encryption keys.");
				}
				
			} // authenticated
			case 2: {
				
				try {
					AesGcm128 ag128 = new AesGcm128(getSecurityProvider().getGlobalKey());
					
					ag128.setInitializationVector(new BitVector(getInitializationVector()));
					ag128.setPlainText(new BitVector(plainText));
					ag128.encrypt();
					
					/* 1 for length, 1 for controlByte, 4 for frameCounter, length of cipherText */
					byte[] securedApdu = new byte[1 + 1 + 4 + plainText.length];
					securedApdu[0] = (byte) (securedApdu.length-1);
					securedApdu[1] = getSecurityControlByte();
					System.arraycopy(getFrameCounterInBytes(), 0, securedApdu, 2, getFrameCounterInBytes().length);
					System.arraycopy(ag128.getCipherText().getValue(), 0, securedApdu, 2+getFrameCounterInBytes().length, ag128.getCipherText().getValue().length);
					return securedApdu;
					
				} catch (IOException e) {
					e.printStackTrace();
					throw new IOException("Could not retrieve the encryption keys.");
				}
			} // encrypted
			case 3: {
				
				try {
					AesGcm128 ag128 = new AesGcm128(getSecurityProvider().getGlobalKey());
					
					/* the associatedData is a concatenation of:
					 * - the securityControlByte
					 * - the authenticationKey */
					byte[] associatedData = new byte[getSecurityProvider().getAuthenticationKey().length + 1];
					associatedData[0] = getSecurityControlByte();
					System.arraycopy(getSecurityProvider().getAuthenticationKey(), 0, associatedData, 1, getSecurityProvider().getAuthenticationKey().length);
					
					ag128.setAdditionalAuthenticationData(new BitVector(associatedData));
					ag128.setInitializationVector(new BitVector(getInitializationVector()));
					ag128.setPlainText(new BitVector(plainText));
					
					ag128.encrypt();
					
					/* 1 for length, 1 for controlByte, 4 for frameCounter, length of cipherText
					 * and 12 for the AuthenticationTag (normally this is 16byte, but the securitySpec said it had to be 12)*/
					byte[] securedApdu = new byte[1 + 1 + 4 + plainText.length + 12];
					securedApdu[0] = (byte) (securedApdu.length-1);
					securedApdu[1] = getSecurityControlByte();
					System.arraycopy(getFrameCounterInBytes(), 0, securedApdu, 2, getFrameCounterInBytes().length);
					System.arraycopy(ag128.getCipherText().getValue(), 0, securedApdu, 2+getFrameCounterInBytes().length, ag128.getCipherText().getValue().length);
					System.arraycopy(ProtocolUtils.getSubArray(ag128.getTag().getValue(), 0, 11), 0, securedApdu, 2+getFrameCounterInBytes().length+ag128.getCipherText().getValue().length, 12);
					return securedApdu;
					
				} catch (IOException e) {
					e.printStackTrace();
					throw new IOException("Could not retrieve the encryption keys.");
				}
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
	 * The securityControlByte is a byte of the securityHeader that is sent with
	 * every encrypted/authenticated message.
	 * <pre>
	 * Bit 3…0: Security_Suite_Id; 
	 * Bit 4: “A” subfield: indicate that the APDU is authenticated; 
	 * Bit 5: “E” subfield: indicates that the APDU is encrypted; 
	 * Bit 6: Key_set subfield 0 = Unicast; 1 = Broadcast,
	 * Bit 7: Reserved, must be set to 0.
	 * </pre>
	 * 
	 * @return the constructed SecurityControlByte
	 */
	public byte getSecurityControlByte() {
		byte scByte = 0;
		scByte |= (this.securitySuite & 0x0F); // add the securitySuite to bits
												// 0 to 3
		scByte |= (this.securityPolicy << 4); // set the
												// encryption/authentication
		return scByte;
	}

	/**
	 * Generate the initializationVector, based on:
	 * 
	 * <pre>
	 * - the SysTitle, which is the ASCII representation of the first 3 chars of the logical device name, concatenated with the hex value of his trailing serialnumber
	 * - the hex representation of the frameCounter
	 * </pre>
	 * 
	 * @return a byteArray containing the frameCounter
	 */
	protected byte[] getInitializationVector() {
		String manufacturer = this.systemIdentifier.substring(0, 3);
		long uniqueNumber = Long
				.valueOf(getLargestIntFromString(this.systemIdentifier));

		byte[] iv = manufacturer.getBytes();
		byte[] un = new byte[5];
		byte[] fc = new byte[4];

		for (int i = 0; i < un.length; i++) {
			un[un.length - 1 - i] = (byte) ((uniqueNumber >> (i * 8)) & 0xff);
		}

		for (int i = 0; i < fc.length; i++) {
			fc[fc.length - 1 - i] = (byte) ((this.frameCounter >> (i * 8)) & 0xff);
		}

		iv = ProtocolUtils.concatByteArrays(iv, un);
		iv = ProtocolUtils.concatByteArrays(iv, fc);
		return iv;
	}
	
	/**
	 * @return the frameCounter
	 */
	public long getFrameCounter(){
		return this.frameCounter;
	}
	public void setFrameCounter(long frameCounter){
		this.frameCounter = frameCounter;
	}
	
	public byte[] getFrameCounterInBytes(){
		byte[] b = new byte[4];
		b[3] = (byte) (getFrameCounter()&0xFF);
		b[2] = (byte) ((getFrameCounter()>>8)&0xFF);
		b[1] = (byte) ((getFrameCounter()>>16)&0xFF);
		b[0] = (byte) ((getFrameCounter()>>24)&0xFF);
		return b;
	}
 //	
//	securityHeader[0] = (byte) (this.aso.getSecurityContext().getFrameCounter()&0xFF);
//	securityHeader[1] = (byte) ((this.aso.getSecurityContext().getFrameCounter()>>8)&0xFF);
//	securityHeader[2] = (byte) ((this.aso.getSecurityContext().getFrameCounter()>>16)&0xFF);
//	securityHeader[3] = (byte) ((this.aso.getSecurityContext().getFrameCounter()>>24)&0xFF);

	/**
	 * HelperMethod to check for the largest trailing number in the logical
	 * device name
	 * 
	 * <pre>
	 * ex.
	 * - ISKT372M40581297 -&gt; 40581297
	 * - KAMM1436321499 -&gt; 1436321499
	 * </pre>
	 * 
	 * @param str
	 *            is the String which contains the number
	 * @return a string containing only a number
	 */
	protected String getLargestIntFromString(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (ProtocolUtils.isInteger(str.substring(i))) {
				return str.substring(i);
			}
		}
		return "";
	}
}
