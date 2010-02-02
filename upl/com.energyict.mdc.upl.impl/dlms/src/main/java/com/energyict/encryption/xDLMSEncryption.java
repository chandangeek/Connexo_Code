package com.energyict.encryption;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.protocol.ProtocolUtils;


/**
 * @author jme
 *
 */
public class xDLMSEncryption {

	private static final String	CRLF	= "\r\n";

	private static final int	SYSTEM_TITLE_LENGTH			= 8;
	private static final int	FRAME_COUNTER_LENGTH		= 4;
	private static final int	GLOBAL_KEY_LENGTH			= 16;
	private static final int	AUTHENTICATION_KEY_LENGTH	= 16;
	private static final int	CONTROL_BYTE_LENGTH			= 1;
	private static final int	TAG_LENGTH					= 1;
	private static final int	LEN_LENGTH					= 1;

	private byte[] systemTitle;
	private byte[] frameCounter;
	private byte[] globalKey;
	private byte[] authenticationKey;
	private byte[] plainText;
	private byte securityControlByte;

	/**
	 * @param systemTitle
	 */
	public void setSystemTitle(byte[] systemTitle) {
		checkArgument(systemTitle, SYSTEM_TITLE_LENGTH);
		this.systemTitle = systemTitle.clone();
	}

	/**
	 * @param frameCounter
	 */
	public void setFrameCounter(byte[] frameCounter) {
		checkArgument(frameCounter, FRAME_COUNTER_LENGTH);
		this.frameCounter = frameCounter.clone();
	}

	/**
	 * @param globalKey
	 */
	public void setGlobalKey(byte[] globalKey) {
		checkArgument(globalKey, GLOBAL_KEY_LENGTH);
		this.globalKey = globalKey.clone();
	}

	/**
	 * @param authenticationKey
	 */
	public void setAuthenticationKey(byte[] authenticationKey) {
		checkArgument(authenticationKey, AUTHENTICATION_KEY_LENGTH);
		this.authenticationKey = authenticationKey.clone();
	}

	/**
	 * @param plainText
	 */
	public void setPlainText(byte[] plainText) {
		checkArgument(plainText, -1);
		this.plainText = plainText.clone();
	}

	/**
	 * @param securityControlByte
	 */
	public void setSecurityControlByte(byte securityControlByte) {
		this.securityControlByte = securityControlByte;
	}

	/**
	 * @return
	 */
	public byte[] generateCipheredAPDU() {
		int length = 0;
		length += TAG_LENGTH;
		length += LEN_LENGTH;
		length += generateSecurityHeader().length;
		length += generateCipherText().length;
		length += generateAuthenticationTag().length;

		int t = 0;
		byte[] apdu = new byte[length];
		apdu[t++] = DLMSCOSEMGlobals.AARE_GLOBAL_INITIATE_REQUEST_TAG;
		apdu[t++] = (byte) (generateSecurityHeader().length + generateCipherText().length + generateAuthenticationTag().length);

		System.arraycopy(generateSecurityHeader(), 0, apdu, t, generateSecurityHeader().length);
		t += generateSecurityHeader().length;

		System.arraycopy(generateCipherText(), 0, apdu, t, generateCipherText().length);
		t += generateCipherText().length;

		System.arraycopy(generateAuthenticationTag(), 0, apdu, t, generateAuthenticationTag().length);
		t += generateAuthenticationTag().length;

		return apdu;
	}

	/**
	 * @return
	 */
	private byte[] generateInitialisationVector() {
		byte[] iv = new byte[getSystemTitle().length + getFrameCounter().length];
		System.arraycopy(getSystemTitle(), 0, iv, 0, getSystemTitle().length);
		System.arraycopy(getFrameCounter(), 0, iv, getSystemTitle().length, getFrameCounter().length);
		return iv;
	}

	/**
	 * @return
	 */
	private byte[] generateSecurityHeader() {
		byte[] sh = new byte[CONTROL_BYTE_LENGTH + getFrameCounter().length];
		sh[0] = getSecurityControlByte();
		System.arraycopy(getFrameCounter(), 0, sh, 1, getFrameCounter().length);
		return sh;
	}

	/**
	 * @return
	 */
	private byte[] generateAuthenticationTag() {
		AesGcm128 aes = new AesGcm128(new BitVector(getAuthenticationKey()));
		aes.setGlobalKey(new BitVector(getGlobalKey()));
		aes.setInitializationVector(new BitVector(generateInitialisationVector()));
		aes.setPlainText(new BitVector(getPlainText()));
		aes.setAdditionalAuthenticationData(new BitVector(generateAssociatedData()));
		aes.setTagSize(12);
		aes.encrypt();
		return aes.getTag().getValue().clone();
	}

	/**
	 * @return
	 */
	private byte[] generateCipherText() {
		AesGcm128 aes = new AesGcm128(new BitVector(getAuthenticationKey()));
		aes.setGlobalKey(new BitVector(getGlobalKey()));
		aes.setInitializationVector(new BitVector(generateInitialisationVector()));
		aes.setPlainText(new BitVector(getPlainText()));
		aes.setAdditionalAuthenticationData(new BitVector(generateAssociatedData()));
		aes.setTagSize(12);
		aes.encrypt();
		return aes.getCipherText().getValue().clone();
	}

	/**
	 * @return
	 */
	private byte[] generateAssociatedData() {
		byte[] a = new byte[1 + getAuthenticationKey().length];
		a[0] = getSecurityControlByte();
		System.arraycopy(getAuthenticationKey(), 0, a, 1, getAuthenticationKey().length);
		return a;
	}

	/**
	 * @return
	 */
	private byte[] getSystemTitle() {
		return systemTitle;
	}

	/**
	 * @return
	 */
	private byte[] getFrameCounter() {
		return frameCounter;
	}

	/**
	 * @return
	 */
	private byte[] getGlobalKey() {
		return globalKey;
	}

	/**
	 * @return
	 */
	private byte[] getAuthenticationKey() {
		return authenticationKey;
	}

	/**
	 * @return
	 */
	private byte[] getPlainText() {
		return plainText;
	}

	/**
	 * @return
	 */
	private byte getSecurityControlByte() {
		return securityControlByte;
	}

	/**
	 * @param argument
	 * @param length
	 */
	private void checkArgument(byte[] argument, int length) {
		if ((argument == null) || ((argument.length != length) && (length != -1))) {
			Throwable th = new Throwable();
			String message = "Invalid argument while calling ";
			message += th.getStackTrace()[1].getMethodName() + "(). ";
			message += argument == null ? "argument==null!" : "Length " + argument.length + "!=" + length;
			throw new IllegalArgumentException(message);
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("xDLMSEncryption").append(CRLF);
		sb.append(" > ST = ").append(ProtocolUtils.getResponseData(getSystemTitle())).append(CRLF);
		sb.append(" > FC = ").append(ProtocolUtils.getResponseData(getFrameCounter())).append(CRLF);
		sb.append(" > AK = ").append(ProtocolUtils.getResponseData(getAuthenticationKey())).append(CRLF);
		sb.append(" > GK = ").append(ProtocolUtils.getResponseData(getGlobalKey())).append(CRLF);
		sb.append(" > PT = ").append(ProtocolUtils.getResponseData(getPlainText())).append(CRLF);
		sb.append(" > SC = ").append(ProtocolUtils.getResponseData(new byte[] { getSecurityControlByte() })).append(CRLF);
		sb.append(CRLF);
		sb.append(" > IV = ").append(ProtocolUtils.getResponseData(generateInitialisationVector())).append(CRLF);
		sb.append(" > SH = ").append(ProtocolUtils.getResponseData(generateSecurityHeader())).append(CRLF);
		sb.append(" > A  = ").append(ProtocolUtils.getResponseData(generateAssociatedData())).append(CRLF);
		sb.append(" > C  = ").append(ProtocolUtils.getResponseData(generateCipherText())).append(CRLF);
		sb.append(" > T  = ").append(ProtocolUtils.getResponseData(generateAuthenticationTag())).append(CRLF);
		sb.append(CRLF);
		sb.append(" > Ciphered APDU = ").append(ProtocolUtils.getResponseData(generateCipheredAPDU())).append(CRLF);
		return sb.toString();
	}

	public static void main(String[] args) {

		final byte[] SYSTEM_TITLE = new byte[] { 0x4D, 0x4D, 0x4D, 0x00, 0x00, (byte) 0xBC, 0x61, 0x4E };
		final byte[] FRAME_COUNTER = new byte[] { 0x01, 0x23, 0x45, 0x67 };

		final byte[] GLOBAL_KEY = new byte[] {
			(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
			(byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
			(byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B,
			(byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F
		};

		final byte[] AUTHENTICATION_KEY = new byte[] {
			(byte) 0xD0, (byte) 0xD1, (byte) 0xD2, (byte) 0xD3,
			(byte) 0xD4, (byte) 0xD5, (byte) 0xD6, (byte) 0xD7,
			(byte) 0xD8, (byte) 0xD9, (byte) 0xDA, (byte) 0xDB,
			(byte) 0xDC, (byte) 0xDD, (byte) 0xDE, (byte) 0xDF
		};

		final byte[] PLAIN_TEXT = new byte[] {
			(byte) 0x01, (byte) 0x01, (byte) 0x10, (byte) 0x00,
			(byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44,
			(byte) 0x55, (byte) 0x66, (byte) 0x77, (byte) 0x88,
			(byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC,
			(byte) 0xDD, (byte) 0xEE, (byte) 0xFF, (byte) 0x00,
			(byte) 0x00, (byte) 0x06, (byte) 0x5F, (byte) 0x1F,
			(byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x7E,
			(byte) 0x1F, (byte) 0x04, (byte) 0xB0
		};


		xDLMSEncryption xdlms = new xDLMSEncryption();
		xdlms.setSystemTitle(SYSTEM_TITLE);
		xdlms.setFrameCounter(FRAME_COUNTER);
		xdlms.setGlobalKey(GLOBAL_KEY);
		xdlms.setAuthenticationKey(AUTHENTICATION_KEY);
		xdlms.setPlainText(PLAIN_TEXT);
		xdlms.setSecurityControlByte((byte) 0x30);
		xdlms.generateCipheredAPDU();

		byte[] ciphered = new byte[] {
				(byte) 0x34, (byte) 0x86, (byte) 0xE9, (byte) 0x77,
				(byte) 0x9A, (byte) 0x65, (byte) 0x51, (byte) 0x3B,
				(byte) 0x30, (byte) 0x5A, (byte) 0x91, (byte) 0xEE,
				(byte) 0x1F, (byte) 0x86
		};

		byte[] tag = new byte[] {
				(byte) 0xD1, (byte) 0xEE, (byte) 0x28, (byte) 0xA7,
				(byte) 0xD4, (byte) 0xAC, (byte) 0x27, (byte) 0xC1,
				(byte) 0xAD, (byte) 0x56, (byte) 0xB8, (byte) 0x02
		};

		byte[] authData = new byte[] {
				(byte) 0x30, (byte) 0xD0, (byte) 0xD1, (byte) 0xD2,
				(byte) 0xD3, (byte) 0xD4, (byte) 0xD5, (byte) 0xD6,
				(byte) 0xD7, (byte) 0xD8, (byte) 0xD9, (byte) 0xDA,
				(byte) 0xDB, (byte) 0xDC, (byte) 0xDD, (byte) 0xDE,
				(byte) 0xDF
		};



	}

}
