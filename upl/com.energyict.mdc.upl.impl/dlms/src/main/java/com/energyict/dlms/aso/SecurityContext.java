package com.energyict.dlms.aso;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSUtils;
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

	public static final int		SECURITYPOLICY_NONE				= 0;
	public static final int		SECURITYPOLICY_AUTHENTICATION	= 1;
	public static final int		SECURITYPOLICY_ENCRYPTION		= 2;
	public static final int		SECURITYPOLICY_BOTH				= 3;

	private static final int	INITIALIZATION_VECTOR_SIZE		= 12;
	private static final int	FRAME_COUNTER_SIZE				= 4;
	private static final int	LENGTH_INDEX					= 1;
	private static final int	FRAMECOUNTER_INDEX				= 2;
	private static final int	FRAMECOUNTER_BYTE_LENGTH		= 4;
	private static final int	BITS_PER_BYTE					= 8;

	/**
	 * Holds the securityLevel for the DataTransport.
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
	private long responseFrameCounter;
	private byte[] systemTitle;

	private String[] authenticationEncryptions = new String[] { "NoAlgorithm", "NoAlgorithm", "NoAlgorithm",
			"MD5", "SHA-1", "GMAC" };
	private String authenticationAlgorithm;

	private static int DLMS_AUTHENTICATION_TAG_SIZE = 12;	// 12 bytes is specified for DLMS using GCM

	/**
	 * Creates a new instance of the securityContext.
	 * Note: the frameCounter can't always start from zero for security reasons. The FC is used in the
	 * initializationVector and this one should be unique.
	 *
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
			int dataTransportEncryptionType, byte[] systemIdentifier,
			SecurityProvider securityProvider) {
		this.securityPolicy = dataTransportSecurityLevel;
		this.authenticationLevel = associationAuthenticationLevel;
		this.securitySuite = dataTransportEncryptionType;
		this.securityProvider = securityProvider;
		this.authenticationAlgorithm = authenticationEncryptions[this.authenticationLevel];
		this.frameCounter = getRandomFrameCounter();
		this.systemTitle = systemIdentifier.clone();
		this.responseFrameCounter = 0;
	}

	/**
	 * Generate a random Long value for the frameCounter.
	 * @return a random long value
	 */
	private long getRandomFrameCounter() {
		Random generator = new Random();
		return generator.nextLong();
	}

	public SecurityContext(int datatransportSecurityLevel,
			int authenticationSecurityLevel, int dataTransportEncryptionType,
			SecurityProvider securityProvider) {
		this(datatransportSecurityLevel, authenticationSecurityLevel, dataTransportEncryptionType, null, securityProvider);
	}

	/**
	 * Get the security level for dataTransport
	 * <pre>
	 * - 0 : Security not imposed
	 * - 1 : All messages(APDU's) must be authenticated
	 * - 2 : All messages(APDU's) must be encrypted
	 * - 3 : All messages(APDU's) must be authenticated AND encrypted
	 * </pre>
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
	 * <pre>
	 * Constructs a ciphered xDLMS APDU. The globalCiphering-PDU-Tag is NOT included.
	 * The returned byteArray will contain the:
	 * 	- Length
	 * 	- SecurityHeader
	 * 	- ciphered APDU
	 * 	- (Tag)
	 * </pre>
	 * @param plainText
	 *            - the text to encrypt ...
	 * @return the cipherText (or the plainText when no security has to be
	 *         applied)
	 * @throws IOException when Keys could not be fetched
	 */
	public byte[] dataTransportEncryption(byte[] plainText)
			throws IOException {
		int offset = 0;
		try {
			switch (this.securityPolicy) {
			case 0: {
				return plainText;
			} // no encryption/authentication
			case 1: {
				AesGcm128 ag128 = new AesGcm128(getSecurityProvider().getGlobalKey(), DLMS_AUTHENTICATION_TAG_SIZE);

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

				/* x for length, 1 for controlByte, 4 for frameCounter, length of plainText
				 * and 12 for the AuthenticationTag (normally this is 16byte, but the securitySpec said it had to be 12)*/
				byte[] securedLength = DLMSUtils.getAXDRLengthEncoding(1 + 4 + plainText.length + DLMS_AUTHENTICATION_TAG_SIZE);
				byte[] securedApdu = new byte[(int) (securedLength.length + DLMSUtils.getAXDRLength(securedLength, 0))];
				System.arraycopy(securedLength, 0, securedApdu, offset, securedLength.length);
				offset += securedLength.length;
				securedApdu[offset] = getSecurityControlByte();
				offset++;
				System.arraycopy(getFrameCounterInBytes(), 0, securedApdu, offset, getFrameCounterInBytes().length);
				offset += getFrameCounterInBytes().length;
				System.arraycopy(plainText, 0, securedApdu, offset, plainText.length);
				offset += plainText.length;
				System.arraycopy(ProtocolUtils.getSubArray2(ag128.getTag().getValue(), 0, DLMS_AUTHENTICATION_TAG_SIZE), 0, securedApdu,
						offset, DLMS_AUTHENTICATION_TAG_SIZE);
				return securedApdu;
			} // authenticated
			case 2: {
				AesGcm128 ag128 = new AesGcm128(getSecurityProvider().getGlobalKey(), DLMS_AUTHENTICATION_TAG_SIZE);

				ag128.setInitializationVector(new BitVector(getInitializationVector()));
				ag128.setPlainText(new BitVector(plainText));
				ag128.encrypt();

				/* x for length, 1 for controlByte, 4 for frameCounter, length of cipherText */
				byte[] securedLength = DLMSUtils.getAXDRLengthEncoding(1 + 4 + plainText.length);
				byte[] securedApdu = new byte[(int) (securedLength.length + DLMSUtils.getAXDRLength(securedLength, 0))];
				System.arraycopy(securedLength, 0, securedApdu, offset, securedLength.length);
				offset += securedLength.length;
				securedApdu[offset] = getSecurityControlByte();
				offset++;
				System.arraycopy(getFrameCounterInBytes(), 0, securedApdu, offset, getFrameCounterInBytes().length);
				offset += getFrameCounterInBytes().length;
				System.arraycopy(ag128.getCipherText().getValue(), 0, securedApdu, offset, ag128.getCipherText().getValue().length);
				return securedApdu;
			} // encrypted
			case 3: {
				AesGcm128 ag128 = new AesGcm128(getSecurityProvider().getGlobalKey(), DLMS_AUTHENTICATION_TAG_SIZE);

				/* the associatedData is a concatenation of:
				 * - the securityControlByte
				 * - the authenticationKey */
				byte[] associatedData = new byte[getSecurityProvider().getAuthenticationKey().length + 1];
				associatedData[0] = getSecurityControlByte();
				System.arraycopy(getSecurityProvider().getAuthenticationKey(), 0, associatedData, 1, getSecurityProvider().getAuthenticationKey().length);
				getSecurityProvider().getGlobalKey();
				ag128.setAdditionalAuthenticationData(new BitVector(associatedData));
				ag128.setInitializationVector(new BitVector(getInitializationVector()));
				ag128.setPlainText(new BitVector(plainText));

				ag128.encrypt();

				/* x for length, 1 for controlByte, 4 for frameCounter, length of cipherText
				 * and 12 for the AuthenticationTag (normally this is 16byte, but the securitySpec said it had to be 12)*/
				byte[] securedLength = DLMSUtils.getAXDRLengthEncoding(1 + 4 + plainText.length + DLMS_AUTHENTICATION_TAG_SIZE);
				byte[] securedApdu = new byte[(int) (securedLength.length + DLMSUtils.getAXDRLength(securedLength, 0))];
				System.arraycopy(securedLength, 0, securedApdu, offset, securedLength.length);
				offset += securedLength.length;
				securedApdu[offset] = getSecurityControlByte();
				offset++;
				System.arraycopy(getFrameCounterInBytes(), 0, securedApdu, offset, getFrameCounterInBytes().length);
				offset += getFrameCounterInBytes().length;
				System.arraycopy(ag128.getCipherText().getValue(), 0, securedApdu, offset, ag128.getCipherText().getValue().length);
				offset += ag128.getCipherText().getValue().length;
				System.arraycopy(ProtocolUtils.getSubArray(ag128.getTag().getValue(), 0, DLMS_AUTHENTICATION_TAG_SIZE-1), 0, securedApdu,
						offset, DLMS_AUTHENTICATION_TAG_SIZE);
				return securedApdu;
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
	 * Decrypts the ciphered APDU.
	 * @param cipherFrame
	 *            - the text to decrypt ...
	 * @return the plainText
	 * @throws IOException when Keys could not be fetched
	 * @throws ConnectionException when the decryption fails
	 */
	public byte[] dataTransportDecryption(byte[] cipherFrame) throws IOException {
		switch (this.securityPolicy) {
		case 0: {
			return cipherFrame;
		}
		case 1: {
			AesGcm128 ag128 = new AesGcm128(getSecurityProvider().getGlobalKey(), DLMS_AUTHENTICATION_TAG_SIZE);

			byte[] aTag = ProtocolUtils.getSubArray(cipherFrame, cipherFrame.length-DLMS_AUTHENTICATION_TAG_SIZE);
			int lengthOffset = DLMSUtils.getAXDRLengthOffset(cipherFrame, LENGTH_INDEX);
			byte[] fc = ProtocolUtils.getSubArray2(cipherFrame, FRAMECOUNTER_INDEX+lengthOffset, FRAME_COUNTER_SIZE);
			setResponseFrameCounter(ProtocolUtils.getInt(fc));
			byte[] apdu = ProtocolUtils.getSubArray(cipherFrame, FRAMECOUNTER_INDEX+lengthOffset+FRAME_COUNTER_SIZE, cipherFrame.length-DLMS_AUTHENTICATION_TAG_SIZE-1);
			/* the associatedData is a concatenation of:
			 * - the securityControlByte
			 * - the authenticationKey
			 * - the plainText */
			byte[] associatedData = new byte[apdu.length + getSecurityProvider().getAuthenticationKey().length + 1];
			associatedData[0] = getSecurityControlByte();
			System.arraycopy(getSecurityProvider().getAuthenticationKey(), 0, associatedData, 1, getSecurityProvider().getAuthenticationKey().length);
			System.arraycopy(apdu, 0, associatedData, 1+getSecurityProvider().getAuthenticationKey().length, apdu.length);


			ag128.setAdditionalAuthenticationData(new BitVector(associatedData));
			ag128.setInitializationVector(new BitVector(getRespondingInitializationVector()));
			ag128.setTag(new BitVector(aTag));

			if(ag128.decrypt()){
				return apdu;
			} else {
				throw new ConnectionException("Received an invalid cipher frame.");
			}
		}
		case 2: {
			AesGcm128 ag128 = new AesGcm128(getSecurityProvider().getGlobalKey(), DLMS_AUTHENTICATION_TAG_SIZE);

			int lengthOffset = DLMSUtils.getAXDRLengthOffset(cipherFrame, LENGTH_INDEX);
			byte[] fc = ProtocolUtils.getSubArray2(cipherFrame, FRAMECOUNTER_INDEX+lengthOffset, FRAME_COUNTER_SIZE);
			setResponseFrameCounter(ProtocolUtils.getInt(fc));
			byte[] cipherdAPDU = ProtocolUtils.getSubArray(cipherFrame, FRAMECOUNTER_INDEX+lengthOffset+FRAME_COUNTER_SIZE);

			ag128.setInitializationVector(new BitVector(getRespondingInitializationVector()));
			ag128.setCipherText(new BitVector(cipherdAPDU));

			if(ag128.decrypt()){
				return ag128.getPlainText().getValue();
			} else {
				throw new ConnectionException("Received an invalid cipher frame.");
			}
		}
		case 3: {
			AesGcm128 ag128 = new AesGcm128(getSecurityProvider().getGlobalKey(), DLMS_AUTHENTICATION_TAG_SIZE);

			byte[] aTag = ProtocolUtils.getSubArray(cipherFrame, cipherFrame.length-DLMS_AUTHENTICATION_TAG_SIZE);
			int lengthOffset = DLMSUtils.getAXDRLengthOffset(cipherFrame, LENGTH_INDEX);
			byte[] fc = ProtocolUtils.getSubArray2(cipherFrame, FRAMECOUNTER_INDEX+lengthOffset, FRAME_COUNTER_SIZE);
			setResponseFrameCounter(ProtocolUtils.getInt(fc));
			byte[] cipherdAPDU = ProtocolUtils.getSubArray(cipherFrame, FRAMECOUNTER_INDEX+lengthOffset+FRAME_COUNTER_SIZE, cipherFrame.length-DLMS_AUTHENTICATION_TAG_SIZE-1);
			/* the associatedData is a concatenation of:
			 * - the securityControlByte
			 * - the authenticationKey */
			byte[] associatedData = new byte[getSecurityProvider().getAuthenticationKey().length + 1];
			associatedData[0] = getSecurityControlByte();
			System.arraycopy(getSecurityProvider().getAuthenticationKey(), 0, associatedData, 1, getSecurityProvider().getAuthenticationKey().length);

			ag128.setAdditionalAuthenticationData(new BitVector(associatedData));
			ag128.setInitializationVector(new BitVector(getRespondingInitializationVector()));
			ag128.setTag(new BitVector(aTag));
			ag128.setCipherText(new BitVector(cipherdAPDU));

			if(ag128.decrypt()){
				return ag128.getPlainText().getValue();
			} else {
				throw new ConnectionException("Received an invalid cipher frame.");
			}
		}
		default:
			throw new ConnectionException("Unknown securityPolicy: "
					+ this.securityPolicy);
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
		scByte |= (this.securitySuite & 0x0F); // add the securitySuite to bits 0 to 3
		scByte |= (this.securityPolicy << 4); // set the encryption/authentication
		return scByte;
	}

	/**
	 *
	 * NOTE: you should code your own SystemTitle to send to the server
	 *
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

		if(this.systemTitle == null){
			throw new IllegalArgumentException("The AssociationResponse did NOT have a server SysteTitle - Encryption can not be applied!");
		}
		byte[] iv = new byte[INITIALIZATION_VECTOR_SIZE];
		byte[] fc = new byte[FRAME_COUNTER_SIZE];

		for (int i = 0; i < fc.length; i++) {
			fc[fc.length - 1 - i] = (byte) ((this.frameCounter >> (i * BITS_PER_BYTE)) & 0xff);
		}
		iv = ProtocolUtils.concatByteArrays(this.systemTitle, fc);
		return iv;
	}

	protected byte[] getRespondingInitializationVector() {
		if(this.systemTitle == null){
			throw new IllegalArgumentException("The AssociationResponse did NOT have a server SysteTitle - Encryption can not be applied!");
		}
		byte[] iv = new byte[INITIALIZATION_VECTOR_SIZE];
		byte[] fc = new byte[FRAME_COUNTER_SIZE];

		for (int i = 0; i < fc.length; i++) {
			fc[fc.length - 1 - i] = (byte) ((this.responseFrameCounter >> (i * BITS_PER_BYTE)) & 0xff);
		}
		iv = ProtocolUtils.concatByteArrays(this.systemTitle, fc);
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

	/**
	 * @return the responding frameCounter
	 */
	public long getResponseFrameCounter(){
		return this.responseFrameCounter;
	}
	public void setResponseFrameCounter(long frameCounter){
		this.responseFrameCounter = frameCounter;
	}

	/**
	 * @return the frameCounter as byte array
	 */
	public byte[] getFrameCounterInBytes(){
		byte[] b = new byte[FRAMECOUNTER_BYTE_LENGTH];
		long fc = getFrameCounter();
		int shiftValue = FRAMECOUNTER_BYTE_LENGTH * BITS_PER_BYTE;
		for (int i = 0; i < FRAMECOUNTER_BYTE_LENGTH; i++) {
			shiftValue -= BITS_PER_BYTE;
			b[i] = (byte) ((fc >> shiftValue) & 0x0FF);
		}
		return b;
	}

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

	public void setSystemTitle(byte[] respondingAPTtitle) {
		this.systemTitle = respondingAPTtitle.clone();
	}
}
