package com.energyict.encryption;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.protocols.util.ProtocolUtils;


/**
 * @author jme
 *
 */
public class XDlmsEncryption {

	private static final int	TAG_SIZE					= 12;
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
		if (containsNull(generateSH(), generateCipherText(), generateAuthenticationTag())) {
			return null;
		} else {
			int length = 0;
			length += TAG_LENGTH;
			length += LEN_LENGTH;
			length += generateSH().length;
			length += generateCipherText().length;
			length += generateAuthenticationTag().length;

			int t = 0;
			byte[] apdu = new byte[length];
			apdu[t++] = DLMSCOSEMGlobals.AARE_GLOBAL_INITIATE_REQUEST_TAG;
			apdu[t++] = (byte) (generateSH().length + generateCipherText().length + generateAuthenticationTag().length);

			System.arraycopy(generateSH(), 0, apdu, t, generateSH().length);
			t += generateSH().length;

			System.arraycopy(generateCipherText(), 0, apdu, t, generateCipherText().length);
			t += generateCipherText().length;

			System.arraycopy(generateAuthenticationTag(), 0, apdu, t, generateAuthenticationTag().length);
			t += generateAuthenticationTag().length;

			return apdu;
		}
	}

	/**
	 * @return
	 */
	private byte[] generateIV() {
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
	private byte[] generateSH() {
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
	private byte[] generateAuthenticationTag() {
		if (containsNull(getAuthenticationKey(), getGlobalKey(), generateIV(), getPlainText(), generateAssociatedData())) {
			return null;
		} else {
			AesGcm128 aes = new AesGcm128(new BitVector(getAuthenticationKey()));
			aes.setGlobalKey(new BitVector(getGlobalKey()));
			aes.setInitializationVector(new BitVector(generateIV()));
			aes.setPlainText(new BitVector(getPlainText()));
			aes.setAdditionalAuthenticationData(new BitVector(generateAssociatedData()));
			aes.setTagSize(TAG_SIZE);
			aes.encrypt();
			return aes.getTag().getValue().clone();
		}
	}

	/**
	 * @return
	 */
	private byte[] generateCipherText() {
		if (containsNull(getAuthenticationKey(), getGlobalKey(), generateIV(), getPlainText(), generateAssociatedData())) {
			return null;
		} else {
			AesGcm128 aes = new AesGcm128(new BitVector(getAuthenticationKey()));
			aes.setGlobalKey(new BitVector(getGlobalKey()));
			aes.setInitializationVector(new BitVector(generateIV()));
			aes.setPlainText(new BitVector(getPlainText()));
			aes.setAdditionalAuthenticationData(new BitVector(generateAssociatedData()));
			aes.setTagSize(TAG_SIZE);
			aes.encrypt();
			return aes.getCipherText().getValue().clone();
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

	@Override
	public String toString() {
		final String crlf = "\r\n";
		StringBuffer sb = new StringBuffer();
		sb.append("xDLMSEncryption").append(crlf);
		sb.append(" > ST = ").append(getSystemTitle() != null ? ProtocolUtils.getResponseData(getSystemTitle()) : null).append(crlf);
		sb.append(" > FC = ").append(getFrameCounter() != null ? ProtocolUtils.getResponseData(getFrameCounter()) : null).append(crlf);
		sb.append(" > AK = ").append(getAuthenticationKey() != null ? ProtocolUtils.getResponseData(getAuthenticationKey()) : null).append(crlf);
		sb.append(" > GK = ").append(getGlobalKey() != null ? ProtocolUtils.getResponseData(getGlobalKey()) : null).append(crlf);
		sb.append(" > PT = ").append(getPlainText() != null ? ProtocolUtils.getResponseData(getPlainText()) : null).append(crlf);
		sb.append(" > SC = ").append(ProtocolUtils.getResponseData(new byte[] { getSecurityControlByte() })).append(crlf);
		sb.append(crlf);
		sb.append(" > IV = ").append(generateIV() != null ? ProtocolUtils.getResponseData(generateIV()) : null).append(crlf);
		sb.append(" > SH = ").append(generateSH() != null ? ProtocolUtils.getResponseData(generateSH()) : null).append(crlf);
		sb.append(" > A  = ").append(generateAssociatedData() != null ? ProtocolUtils.getResponseData(generateAssociatedData()) : null).append(crlf);
		sb.append(" > C  = ").append(generateCipherText() != null ? ProtocolUtils.getResponseData(generateCipherText()) : null).append(crlf);
		sb.append(" > T  = ").append(generateAuthenticationTag() != null ? ProtocolUtils.getResponseData(generateAuthenticationTag()) : null).append(crlf);
		sb.append(crlf);
		sb.append(" > Ciphered APDU = ").append(generateCipheredAPDU() != null ? ProtocolUtils.getResponseData(generateCipheredAPDU()) : null).append(crlf);
		return sb.toString();
	}

}
