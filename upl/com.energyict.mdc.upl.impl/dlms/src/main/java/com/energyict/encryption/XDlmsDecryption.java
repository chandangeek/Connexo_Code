package com.energyict.encryption;

import com.energyict.protocol.ProtocolUtils;


/**
 * @author jme
 *
 */
public class XDlmsDecryption {

	private static final String	CRLF	= "\r\n";

	private static final int	SYSTEM_TITLE_LENGTH			= 8;
	private static final int	FRAME_COUNTER_LENGTH		= 4;
	private static final int	GLOBAL_KEY_LENGTH			= 16;
	private static final int	AUTHENTICATION_KEY_LENGTH	= 16;
	private static final int	AUTHENTICATION_TAG_LENGTH	= 12;
	private static final int	CONTROL_BYTE_LENGTH			= 1;

	private byte[] systemTitle;
	private byte[] frameCounter;
	private byte[] globalKey;
	private byte[] authenticationKey;
	private byte[] cipheredText;
	private byte[] authenticationTag;
	private byte securityControlByte;

	/**
	 * @param systemTitle
	 */
	public void setSystemTitle(byte[] systemTitle) {
		checkArgument(systemTitle, -1);
		this.systemTitle = new byte[SYSTEM_TITLE_LENGTH];
		int copyLength = systemTitle.length < SYSTEM_TITLE_LENGTH ? systemTitle.length : this.systemTitle.length;
		System.arraycopy(systemTitle, 0, this.systemTitle, 0, copyLength);
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
	public void setCipheredText(byte[] cipheredText) {
		checkArgument(cipheredText, -1);
		this.cipheredText = cipheredText.clone();
	}

	/**
	 * @param authenticationTag
	 */
	public void setAuthenticationTag(byte[] authenticationTag) {
		checkArgument(authenticationTag, AUTHENTICATION_TAG_LENGTH);
		this.authenticationTag = authenticationTag.clone();
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
	public byte[] generatePlainText() {
		AesGcm128 aes = new AesGcm128();
		aes.setGlobalKey(new BitVector(getGlobalKey()));
		aes.setCipherText(new BitVector(getCipherText()));
		aes.setInitializationVector(new BitVector(generateInitialisationVector()));
		aes.setAdditionalAuthenticationData(new BitVector(generateAssociatedData()));
		aes.setTag(new BitVector(getAuthenticationTag()));
		aes.setTagSize(AUTHENTICATION_TAG_LENGTH);
		aes.decrypt();
		return aes.getPlainText().getValue();
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
	private byte[] generateAssociatedData() {
		byte[] a = new byte[1 + getAuthenticationKey().length];
		a[0] = getSecurityControlByte();
		System.arraycopy(getAuthenticationKey(), 0, a, 1, getAuthenticationKey().length);
		return a;
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
	private byte[] getAuthenticationTag() {
		return this.authenticationTag;
	}

	/**
	 * @return
	 */
	private byte[] getCipherText() {
		return this.cipheredText;
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
		sb.append("xDLMSDecryption").append(CRLF);
		sb.append(" > ST = ").append(ProtocolUtils.getResponseData(getSystemTitle())).append(CRLF);
		sb.append(" > FC = ").append(ProtocolUtils.getResponseData(getFrameCounter())).append(CRLF);
		sb.append(" > AK = ").append(ProtocolUtils.getResponseData(getAuthenticationKey())).append(CRLF);
		sb.append(" > GK = ").append(ProtocolUtils.getResponseData(getGlobalKey())).append(CRLF);
		sb.append(" > SC = ").append(ProtocolUtils.getResponseData(new byte[] { getSecurityControlByte() })).append(CRLF);
		sb.append(" > C  = ").append(ProtocolUtils.getResponseData(getCipherText())).append(CRLF);
		sb.append(" > T  = ").append(getAuthenticationTag() != null ? ProtocolUtils.getResponseData(getAuthenticationTag()) : "null").append(CRLF);
		sb.append(CRLF);
		sb.append(" > IV = ").append(ProtocolUtils.getResponseData(generateInitialisationVector())).append(CRLF);
		sb.append(" > SH = ").append(ProtocolUtils.getResponseData(generateSecurityHeader())).append(CRLF);
		sb.append(" > A  = ").append(ProtocolUtils.getResponseData(generateAssociatedData())).append(CRLF);
		sb.append(" > PT = ").append(ProtocolUtils.getResponseData(generatePlainText())).append(CRLF);
		return sb.toString();
	}

	public static void main(String[] args) {

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

		final byte[] systemTitle = new byte[] { 0x30, 0x31, 0x35, 0x30, 0x32, 0x33};
		final byte[] FRAME_COUNTER = new byte[] { 0x00, 0x00, 0x00, 0x38 };

		final byte[] globalKey = new byte[] {
			(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
			(byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
			(byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B,
			(byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F
		};

		final byte[] authKey = new byte[] {
			(byte) 0xD0, (byte) 0xD1, (byte) 0xD2, (byte) 0xD3,
			(byte) 0xD4, (byte) 0xD5, (byte) 0xD6, (byte) 0xD7,
			(byte) 0xD8, (byte) 0xD9, (byte) 0xDA, (byte) 0xDB,
			(byte) 0xDC, (byte) 0xDD, (byte) 0xDE, (byte) 0xDF
		};

		XDlmsDecryption xdlms = new XDlmsDecryption();
		xdlms.setCipheredText(ciphered);
		xdlms.setSystemTitle(systemTitle);
		xdlms.setFrameCounter(FRAME_COUNTER);
		xdlms.setGlobalKey(globalKey);
		xdlms.setAuthenticationKey(authKey);
		xdlms.setSecurityControlByte((byte) 0x30);
		xdlms.setAuthenticationTag(tag);
		System.out.println(xdlms);

	}

}
