package com.energyict.encryption;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;


/**
 * @author jme
 */
public class XDlmsEncryption {

    private static final int TAG_SIZE = 12;
    private static final int SYSTEM_TITLE_LENGTH = 8;
    private static final int FRAME_COUNTER_LENGTH = 4;
    private static final int GLOBAL_KEY_LENGTH = 16;
    private static final int AUTHENTICATION_KEY_LENGTH = 16;
    private static final int CONTROL_BYTE_LENGTH = 1;
    private static final int TAG_LENGTH = 1;
    private static final int LEN_LENGTH = 1;
    private final int securitySuite;
    private byte[] systemTitle;
    private byte[] frameCounter;
    private byte[] globalKey;
    private byte[] authenticationKey;
    private byte[] plainText;
    private byte securityControlByte;

    public XDlmsEncryption(int securitySuite) {
        this.securitySuite = securitySuite;
    }

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

    private byte[] generateAuthenticationTag() {
        if (containsNull(getAuthenticationKey(), getGlobalKey(), generateIV(), getPlainText(), generateAssociatedData())) {
            return null;
        } else {
            AesGcm aes = new AesGcm(new BitVector(getAuthenticationKey()));
            aes.setGlobalKey(new BitVector(getGlobalKey()));
            aes.setInitializationVector(new BitVector(generateIV()));
            aes.setPlainText(new BitVector(getPlainText()));
            aes.setAdditionalAuthenticationData(new BitVector(generateAssociatedData()));
            aes.setTagSize(TAG_SIZE);
            aes.encrypt();
            return aes.getTag().getValue().clone();
        }
    }

    private byte[] generateCipherText() {
        if (containsNull(getAuthenticationKey(), getGlobalKey(), generateIV(), getPlainText(), generateAssociatedData())) {
            return null;
        } else {
            AesGcm aes = new AesGcm(new BitVector(getAuthenticationKey()));
            aes.setGlobalKey(new BitVector(getGlobalKey()));
            aes.setInitializationVector(new BitVector(generateIV()));
            aes.setPlainText(new BitVector(getPlainText()));
            aes.setAdditionalAuthenticationData(new BitVector(generateAssociatedData()));
            aes.setTagSize(TAG_SIZE);
            aes.encrypt();
            return aes.getCipherText().getValue().clone();
        }
    }

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

    private byte[] getSystemTitle() {
        return systemTitle;
    }

    public void setSystemTitle(byte[] systemTitle) {
        checkArgument(systemTitle, SYSTEM_TITLE_LENGTH);
        this.systemTitle = systemTitle.clone();
    }

    private byte[] getFrameCounter() {
        return frameCounter;
    }

    public void setFrameCounter(byte[] frameCounter) {
        checkArgument(frameCounter, FRAME_COUNTER_LENGTH);
        this.frameCounter = frameCounter.clone();
    }

    private byte[] getGlobalKey() {
        return globalKey;
    }

    /**
     * @param globalKey
     */
    public void setGlobalKey(byte[] globalKey) {
        if (globalKey == null || globalKey.length != getGlobalKeyLength()) {
            String value = (globalKey == null ? "null" : ProtocolTools.getHexStringFromBytes(globalKey, ""));
            throw DeviceConfigurationException.invalidPropertyFormat(SecurityPropertySpecName.ENCRYPTION_KEY.toString(), value, "Should be a hex string of " + (getGlobalKeyLength() * 2) + " characters");
        } else {
            this.globalKey = globalKey.clone();
        }
    }

    /**
     * For suite 0 and 1: 16 bytes
     * For suite 2: 32 bytes
     */
    private int getGlobalKeyLength() {
        if (securitySuite == 2) {
            return GLOBAL_KEY_LENGTH * 2;
        } else {
            return GLOBAL_KEY_LENGTH;
        }
    }

    private byte[] getAuthenticationKey() {
        return authenticationKey;
    }

    /**
     * @param authenticationKey
     */
    public void setAuthenticationKey(byte[] authenticationKey) {
        if (authenticationKey == null || authenticationKey.length != getAuthenticationKeyLength()) {
            String value = (authenticationKey == null ? "null" : ProtocolTools.getHexStringFromBytes(authenticationKey, ""));
            throw DeviceConfigurationException.invalidPropertyFormat(SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), value, "Should be a hex string of " + (getAuthenticationKeyLength() * 2) + " characters");
        } else {
            this.authenticationKey = authenticationKey.clone();
        }
    }

    /**
     * For suite 0 and 1: 16 bytes
     * For suite 2: 32 bytes
     */
    private int getAuthenticationKeyLength() {
        if (securitySuite == 2) {
            return AUTHENTICATION_KEY_LENGTH * 2;
        } else {
            return AUTHENTICATION_KEY_LENGTH;
        }
    }

    private byte[] getPlainText() {
        return plainText;
    }

    /**
     * @param plainText
     */
    public void setPlainText(byte[] plainText) {
        checkArgument(plainText, -1);
        this.plainText = plainText.clone();
    }

    private byte getSecurityControlByte() {
        return securityControlByte;
    }

    /**
     * @param securityControlByte
     */
    public void setSecurityControlByte(byte securityControlByte) {
        this.securityControlByte = securityControlByte;
    }

    private void checkArgument(byte[] argument, int length) {
        if ((argument == null) || ((argument.length != length) && (length != -1))) {
            Throwable th = new Throwable();
            String message = "Invalid argument while calling ";
            message += th.getStackTrace()[1].getMethodName() + "(). ";
            message += argument == null ? "argument==null!" : "Length " + argument.length + "!=" + length;
            throw new IllegalArgumentException(message);
        }
    }

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
        sb.append(" > SC = ").append(ProtocolUtils.getResponseData(new byte[]{getSecurityControlByte()})).append(crlf);
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
