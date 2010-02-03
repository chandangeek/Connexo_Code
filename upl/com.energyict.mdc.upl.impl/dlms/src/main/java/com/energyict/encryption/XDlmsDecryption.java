package com.energyict.encryption;

import com.energyict.protocol.ProtocolUtils;


/**
 * @author jme
 *
 */
public class XDlmsDecryption {

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
	 * @param title
	 */
	public void setSystemTitle(byte[] title) {
		checkArgument(title, -1);
		this.systemTitle = new byte[SYSTEM_TITLE_LENGTH];
		int copyLength = title.length < SYSTEM_TITLE_LENGTH ? title.length : this.systemTitle.length;
		System.arraycopy(title, 0, this.systemTitle, 0, copyLength);
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
		final String crlf = "\r\n";
		StringBuffer sb = new StringBuffer();
		sb.append("xDLMSDecryption").append(crlf);
		sb.append(" > ST = ").append(ProtocolUtils.getResponseData(getSystemTitle())).append(crlf);
		sb.append(" > FC = ").append(ProtocolUtils.getResponseData(getFrameCounter())).append(crlf);
		sb.append(" > AK = ").append(ProtocolUtils.getResponseData(getAuthenticationKey())).append(crlf);
		sb.append(" > GK = ").append(ProtocolUtils.getResponseData(getGlobalKey())).append(crlf);
		sb.append(" > SC = ").append(ProtocolUtils.getResponseData(new byte[] { getSecurityControlByte() })).append(crlf);
		sb.append(" > C  = ").append(ProtocolUtils.getResponseData(getCipherText())).append(crlf);
		sb.append(" > T  = ").append(getAuthenticationTag() != null ? ProtocolUtils.getResponseData(getAuthenticationTag()) : "null").append(crlf);
		sb.append(crlf);
		sb.append(" > IV = ").append(ProtocolUtils.getResponseData(generateInitialisationVector())).append(crlf);
		sb.append(" > SH = ").append(ProtocolUtils.getResponseData(generateSecurityHeader())).append(crlf);
		sb.append(" > A  = ").append(ProtocolUtils.getResponseData(generateAssociatedData())).append(crlf);
		sb.append(" > PT = ").append(ProtocolUtils.getResponseData(generatePlainText())).append(crlf);
		return sb.toString();
	}

}
