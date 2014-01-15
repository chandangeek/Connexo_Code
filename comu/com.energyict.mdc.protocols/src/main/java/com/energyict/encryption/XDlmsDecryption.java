package com.energyict.encryption;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocols.util.ProtocolUtils;


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
	 * @param cipheredText
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
	public byte[] generatePlainText() throws ConnectionException {
		if (containsNull(getGlobalKey(), getCipherText(), generateInitialisationVector(), generateAssociatedData(), getAuthenticationTag())) {
			return null;
		} else {
			AesGcm128 aes = new AesGcm128();
			aes.setGlobalKey(new BitVector(getGlobalKey()));
			aes.setCipherText(new BitVector(getCipherText()));
			aes.setInitializationVector(new BitVector(generateInitialisationVector()));
			aes.setAdditionalAuthenticationData(new BitVector(generateAssociatedData()));
			aes.setTag(new BitVector(getAuthenticationTag()));
			aes.setTagSize(AUTHENTICATION_TAG_LENGTH);
			if(aes.decrypt()){
                return aes.getPlainText().getValue();
            } else {
                throw new ConnectionException("Received an invalid cipher frame.");
            }
		}
	}

	/**
	 * @return
	 */
	private byte[] generateInitialisationVector() {
		if (containsNull(getSystemTitle(), getFrameCounter())) {
			return null;
		} else {
			byte[] iv = new byte[getSystemTitle().length + getFrameCounter().length];
			System.arraycopy(getSystemTitle(), 0, iv, 0, getSystemTitle().length);
			System.arraycopy(getFrameCounter(), 0, iv, getSystemTitle().length, getFrameCounter().length);
			return iv;
		}
	}

	/**
	 * @return
	 */
	private byte[] generateAssociatedData() {
		if (containsNull(getAuthenticationKey())) {
			return null;
		} else {
			byte[] a = new byte[1 + getAuthenticationKey().length];
			a[0] = getSecurityControlByte();
			System.arraycopy(getAuthenticationKey(), 0, a, 1, getAuthenticationKey().length);
			return a;
		}
	}

	/**
	 * @return
	 */
	private byte[] generateSecurityHeader() {
		if (containsNull(getFrameCounter())) {
			return null;
		} else {
			byte[] sh = new byte[CONTROL_BYTE_LENGTH + getFrameCounter().length];
			sh[0] = getSecurityControlByte();
			System.arraycopy(getFrameCounter(), 0, sh, 1, getFrameCounter().length);
			return sh;
		}
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
	 * @param object
	 * @return
	 */
	private boolean containsNull(Object... object) {
		for (Object obj : object) {
			if (obj == null) {
				return true;
			}
		}
		return false;
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
		sb.append(" > ST = ").append(getSystemTitle() != null ? ProtocolUtils.getResponseData(getSystemTitle()) : null).append(crlf);
		sb.append(" > FC = ").append(getFrameCounter() != null ? ProtocolUtils.getResponseData(getFrameCounter()) : null).append(crlf);
		sb.append(" > AK = ").append(getAuthenticationKey() != null ? ProtocolUtils.getResponseData(getAuthenticationKey()) : null).append(crlf);
		sb.append(" > GK = ").append(getGlobalKey() != null ? ProtocolUtils.getResponseData(getGlobalKey()) : null).append(crlf);
		sb.append(" > SC = ").append(ProtocolUtils.getResponseData(new byte[] { getSecurityControlByte() })).append(crlf);
		sb.append(" > C  = ").append(getCipherText() != null ? ProtocolUtils.getResponseData(getCipherText()) : null).append(crlf);
		sb.append(" > T  = ").append(getAuthenticationTag() != null ? ProtocolUtils.getResponseData(getAuthenticationTag()) : "null").append(crlf);
		sb.append(crlf);
		sb.append(" > IV = ").append(generateInitialisationVector() != null ? ProtocolUtils.getResponseData(generateInitialisationVector()) : null).append(crlf);
		sb.append(" > SH = ").append(generateSecurityHeader() != null ? ProtocolUtils.getResponseData(generateSecurityHeader()) : null).append(crlf);
		sb.append(" > A  = ").append(generateAssociatedData() != null ? ProtocolUtils.getResponseData(generateAssociatedData()) : null).append(crlf);
        try {
            sb.append(" > PT = ").append(generatePlainText() != null ? ProtocolUtils.getResponseData(generatePlainText()) : null).append(crlf);
        } catch (ConnectionException e) {
            sb.append(" > PT = ").append("Could NOT generate plainText with invalid keys.");
        }
        return sb.toString();
	}

}
