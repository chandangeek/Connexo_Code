package com.energyict.mdc.engine.offline.persist;

import com.elster.jupiter.orm.Encrypter;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.engine.impl.tools.ProtocolUtils;
import com.energyict.mdc.engine.offline.gui.util.ProtocolTools;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 14/10/2014 - 14:15
 */
public class FileEncrypter implements Encrypter {

    private static final int TAG_SIZE = 12;
    public static final Charset UTF8 = Charset.forName("UTF8");
    private static final byte[] AUTH_AND_ENCRYPTION = new byte[]{0x30};

    protected final String defaultUncaughtExceptionHandlerName;
    private byte[] encryptionKey;
    private byte[] authenticationKey;
    private MessageDigest md5;
    private BitVector initialVector;

    public FileEncrypter() {
        this(Thread.getDefaultUncaughtExceptionHandler() == null ? "" : Thread.getDefaultUncaughtExceptionHandler().getClass().getName());
    }

    protected FileEncrypter(String defaultUncaughtExceptionHandlerName) {
        this.defaultUncaughtExceptionHandlerName = defaultUncaughtExceptionHandlerName;
    }

    private BitVector getInitialVector() {
        if (initialVector == null) {
            byte[] hash = hash(ProtocolTools.concatByteArrays(getInitialVectorAK().getBytes(UTF8), getInitialVectorEK().getBytes(UTF8)));
            initialVector = new BitVector(ProtocolTools.getSubArray(hash, 0, 12));
        }
        return initialVector;
    }

    private String getInitialVectorEK() {
        return FileEncrypter.class.getName() + getDefaultUncaughtExceptionHandlerName();
    }

    private String getInitialVectorAK() {
        return getDefaultUncaughtExceptionHandlerName() + FileEncrypter.class.getName();
    }

    private String getDefaultUncaughtExceptionHandlerName() {
        return defaultUncaughtExceptionHandlerName;
    }

    private byte[] getEncryptionKey() {
        if (encryptionKey == null) {
            encryptionKey = hash(getInitialVectorEK().getBytes(UTF8));
        }
        return encryptionKey;
    }

    private byte[] getAuthenticationKey() {
        if (authenticationKey == null) {
            authenticationKey = hash(getInitialVectorAK().getBytes(UTF8));
        }
        return authenticationKey;
    }

    private byte[] hash(byte[] initialVector) {
        return getMessageDigest().digest(initialVector);
    }

    private MessageDigest getMessageDigest() {
        if (md5 == null) {
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {  //Will never happen
                throw new ApplicationException(e.getMessage());
            }
        }
        return md5;
    }

    private BitVector getAdditionalAuthenticationData() {
        return new BitVector(ProtocolTools.concatByteArrays(AUTH_AND_ENCRYPTION, getAuthenticationKey()));
    }

    public String encrypt(String plain) {
        return encrypt(plain.getBytes(UTF8));
    }

    public String encrypt(byte[] decrypted) {
        AesGcm ag128 = new AesGcm(getEncryptionKey(), TAG_SIZE);
        ag128.setAdditionalAuthenticationData(getAdditionalAuthenticationData());
        ag128.setInitializationVector(getInitialVector());
        ag128.setPlainText(new BitVector(decrypted));
        ag128.encrypt();
        byte[] tag = ag128.getTag().getValue();
        byte[] cipher = ag128.getCipherText().getValue();
        return ProtocolTools.getHexStringFromBytes(ProtocolTools.concatByteArrays(cipher, tag), "");
    }

    public byte[] decrypt(String hexCipher) {
        byte[] fullContent = ProtocolTools.getBytesFromHexString(hexCipher, "");
        byte[] cipher = ProtocolUtils.getSubArray(fullContent, 0, fullContent.length - TAG_SIZE - 1);
        byte[] tag = ProtocolUtils.getSubArray(fullContent, fullContent.length - TAG_SIZE);

        AesGcm ag128 = new AesGcm(getEncryptionKey(), TAG_SIZE);
        ag128.setAdditionalAuthenticationData(getAdditionalAuthenticationData());
        ag128.setInitializationVector(new BitVector(getInitialVector()));
        ag128.setTag(new BitVector(tag));
        ag128.setCipherText(new BitVector(cipher));

        if (ag128.decrypt()) {
            return ag128.getPlainText().getValue();
        } else {
            throw new ApplicationException("Could not decrypt the collected data! File might be corrupted.");
        }
    }
}