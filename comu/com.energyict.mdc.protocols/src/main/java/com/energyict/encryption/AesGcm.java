/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.encryption;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of Galois/Counter mode of Operation(GCM)
 * <p/>
 * <pre>
 * Galois/Counter Mode (GCM) is an algorithm for authenticated encryption with associated data.
 * GCM is constructed from an approved symmetric key block cipher with a block size of 128 bits,
 * such as the Advanced Encryption Standard (AES) algorithm. Thus, GCM is a mode of operation of the AES algorithm.
 *
 * GCM provides assurance of the confidentiality of data using a variation of the Counter mode of operation for encryption.
 *
 * GCM provides assurance of the authenticity of the confidential data (up to about 64 gigabytes per invocation)
 * using a universal hash function that is defined over a binary Galois (i.e., finite) field.
 *
 * GCM can also provide authentication assurance for additional data (of practically unlimited length per invocation) that is not encrypted.
 * If the GCM input is restricted to data that is not to be encrypted, the resulting specialization of GCM, called GMAC,
 * is simply an authentication mode on the input data.
 *
 * GCM provides stronger authentication assurance than a (non-cryptographic) checksum or error detecting code; in particular, GCM can detect both
 * 	1) accidental modifications of the data and
 * 	2) intentional, unauthorized modifications.
 * </pre>
 */
public class AesGcm {

    /**
     * Logger instance.
     */
    private static final Logger logger = Logger.getLogger(AesGcm.class.getName());
    /**
     * The key algo.
     */
    private static final String KEY_ALGO = "AES";
    /**
     * Using AES/ECB/NoPadding for standard AES.
     */
    private static final String CIPHER_AES_ECB = "AES/ECB/NoPadding";
    /**
     * The algo used when doing GCM.
     */
    private static final String CIPHER_AES_GCM = "AES/GCM/NoPadding";
    /**
     * Indicates whether we support "native" AES/GCM.
     */
    private static final boolean JVM_SUPPORTS_AES_GCM = haveNativeSupportForAESGCM();
    /**
     * Internally used by GCM, we use it when decrypting stuff that has no tag (GCM without the GMAC).
     */
    private static final String CIPHER_AES_CTR = "AES/CTR/NoPadding";

    /**
     * Default tag size, in bytes.
     */
    private static final int DEFAULT_TAG_SIZE = 16;

    /**
     * The encryption key.
     */
    private SecretKey key;

    /**
     * The tag size, in bytes.
     */
    private int tagSize;

    /**
     * The IV for GCM/CTR.
     */
    private byte[] iv;

    /**
     * Additional auth data for GCM.
     */
    private byte[] aad;

    /**
     * The plain text.
     */
    private byte[] plainText;

    /**
     * The cipher text.
     */
    private byte[] cipherText;

    /**
     * The authentication tag.
     */
    private byte[] tag;

    /**
     * Create a new instance.
     *
     * @param key Key to be used.
     * @param iv IV to be used.
     * @param plainText Plain text.
     * @param aad AAD to be used.
     * @param cipherText Cipher text.
     * @param tag Tag.
     * @param    tagSize            Size of the tag.
     */
    private AesGcm(final byte[] key, final byte[] iv, final byte[] plainText, final byte[] aad, final byte[] cipherText, final byte[] tag, final int tagSize) {
        this.key = new SecretKeySpec(key, KEY_ALGO);
        this.iv = iv;
        this.plainText = plainText;
        this.aad = aad;
        this.cipherText = cipherText;
        this.tag = tag;
        this.tagSize = tagSize;
    }

    /**
     * Creates a new instance of the AES Galois/Counter mode with an empty global encryption key
     */
    public AesGcm() {
        this(new byte[16], new byte[0], new byte[0], new byte[0], new byte[0], new byte[0], DEFAULT_TAG_SIZE);
    }

    /**
     * Creates a new instance of the AES Galois/Counter mode with a globalKey BitVector
     *
     * @param key - the global encryption Key
     */
    public AesGcm(final BitVector key) {
        this(key.getValue(), new byte[0], new byte[0], new byte[0], new byte[0], new byte[0], DEFAULT_TAG_SIZE);
    }

    /**
     * Creates a new instance of the AES Galois/Counter mode with a globalKey byteArray
     *
     * @param globalKey - the global encryption Key
     * @param tagSize - the size of the authenticationTag
     */
    public AesGcm(final byte[] globalKey, final int tagSize) {
        this(globalKey, new byte[0], new byte[0], new byte[0], new byte[0], new byte[0], tagSize);
    }

    /**
     * Manipulates the IV so it can be used to decrypt using AES/CTR. Courtesy of the OpenJDK sources.
     *
     * @param gcmIV The original (GCM) IV.
     */
    private static final byte[] getCTRIV(final byte[] gcmIV) {
        final byte[] paddedIV = new byte[16];
        System.arraycopy(gcmIV, 0, paddedIV, 0, gcmIV.length);
        paddedIV[15] = 1;

        int n = paddedIV.length - 1;

        while ((n >= paddedIV.length - 4) && (++paddedIV[n] == 0)) {
            n--;
        }

        return paddedIV;
    }

    /**
     * Checks if we have native support for AES/GCM in our JVM (Java 8). If we do, we'll be using that, since it is waaaaay
     * faster than the old implementation. Sorry for those stuck on 1.7 though.
     *
     * @return <code>true</code> if the JDK supports AES/GCM/NoPadding, <code>false</code> if it does not.
     */
    private static final boolean haveNativeSupportForAESGCM() {
        try {
            Cipher.getInstance(CIPHER_AES_GCM);

            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    /**
     * Encrypt the plain text.
     *
     * @param plain The plain text.
     * @return The cipher text.
     */
    public final BitVector aesEncrypt(final BitVector plain) {
        try {
            final Cipher aesCipher = Cipher.getInstance(CIPHER_AES_ECB);
            aesCipher.init(Cipher.ENCRYPT_MODE, this.key);

            final byte[] cipherText = aesCipher.doFinal(plain.getValue());

            return new BitVector(cipherText);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Error encrypting block of data : [" + e.getMessage() + "]", e);
        }
    }

    public final void encrypt() {
        if (JVM_SUPPORTS_AES_GCM) {
            this.encryptNative();
        } else {
            this.encryptLegacy();
        }
    }

    /**
     * Encrypts the plainText according to the Galois/Counter mode of Operation,
     * using the provided input parameters.
     */
    private final void encryptLegacy() {

        // y0 = iv||0000 0000 0000 0000 0000 0000 0000 0001
        final BitVector p = new BitVector(this.plainText);
        final BitVector a = new BitVector(this.aad);
        final BitVector iv = new BitVector(this.iv);
        final BitVector h = aesEncrypt(new BitVector(16));
        BitVector c = null;
        BitVector t = null;

        int counter = 1;
        BitVector y0 = BitVector.concatenate(iv, BitVector.convertFromInt(counter, 4));
        int n = p.length() / 16 + ((p.length() % 16 > 0) ? 1 : 0);
        int u = p.length() % 16;
        int m = a.length() / 16 + ((a.length() % 16 > 0) ? 1 : 0);
        int v = a.length() % 16;

        c = new BitVector(p.length());

        for (int i = 0; i < n; i++) {
            counter++;
            BitVector yi = BitVector.concatenate(iv, BitVector.convertFromInt(counter, 4));
            BitVector pi = p.get128Segment(i);
            BitVector ci = BitVector.addition(pi, aesEncrypt(yi));
            c.set128Segment(i, ci);
        }

        BitVector x = new BitVector(16);

        for (int i = 0; i < m; i++) {
            BitVector ai = a.get128Segment(i);
            x = BitVector.multiplication(BitVector.addition(x, ai), h);
        }
        for (int i = 0; i < n; i++) {
            BitVector ci = c.get128Segment(i);
            x = BitVector.multiplication(BitVector.addition(x, ci), h);
        }
        BitVector len = BitVector.concatenate(BitVector.convertFromInt(a.length() * 8, 8),
                BitVector.convertFromInt(c.length() * 8, 8));
        x = BitVector.multiplication(BitVector.addition(x, len), h);

        t = BitVector.addition(x, aesEncrypt(y0)).Msb2(this.tagSize);

        this.tag = t.getValue();
        this.cipherText = c.getValue();
    }

    /**
     * Encrypts the plainText according to the Galois/Counter mode of Operation,
     * using the provided input parameters.
     */
    private final void encryptNative() {
        try {
            final GCMParameterSpec parameterSpec = new GCMParameterSpec(DEFAULT_TAG_SIZE * 8, this.iv);
            final Cipher aesGCMCipher = Cipher.getInstance(CIPHER_AES_GCM);

            aesGCMCipher.init(Cipher.ENCRYPT_MODE, this.key, parameterSpec);
            aesGCMCipher.updateAAD(this.aad);

            // This is cipher text + tag, need to split these.
            final byte[] cipherText = aesGCMCipher.doFinal(this.plainText);

            final int tagStart = cipherText.length - 16;
            final int tagEnd = tagStart + this.tagSize;

            this.tag = Arrays.copyOfRange(cipherText, tagStart, tagEnd);
            this.cipherText = Arrays.copyOfRange(cipherText, 0, tagStart);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Error encrypting : [" + e.getMessage() + "]", e);
        }
    }

    /**
     * Decrypts the cipherText. Will also check validity of the data.
     *
     * @return true if it's a valid encrypted frame, false otherwise
     */
    public final boolean decrypt() {
        if (JVM_SUPPORTS_AES_GCM) {
            return this.decryptNative();
        } else {
            return this.decryptLegacy();
        }
    }

    /**
     * Decrypts the cipherText. Will also check validity of the data.
     *
     * @return true if it's a valid encrypted frame, false otherwise
     */
    private final boolean decryptNative() {
        if (this.tag == null || this.tag.length == 0) {
            // Use CTR here.
            try {
                final IvParameterSpec parameterSpec = new IvParameterSpec(getCTRIV(this.iv));
                final Cipher aesCTRCipher = Cipher.getInstance(CIPHER_AES_CTR);

                aesCTRCipher.init(Cipher.DECRYPT_MODE, this.key, parameterSpec);

                this.plainText = aesCTRCipher.doFinal(this.cipherText);

                return true;
            } catch (GeneralSecurityException e) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "Error decrypting : [" + e.getMessage() + "]", e);
                }

                return false;
            }
        } else {
            /*byte[] paddedTag = null;

			// Pad the tag to 16 bytes.
			if (this.tag.length < 16) {
				paddedTag = new byte[16];

				System.arraycopy(this.tag, 0, paddedTag, 0, this.tag.length);
			} else {
				paddedTag = tag;
			}*/

            try {
                final GCMParameterSpec parameterSpec = new GCMParameterSpec(this.tagSize * 8, iv);

                final Cipher aesGCMCipher = Cipher.getInstance(CIPHER_AES_GCM);
                aesGCMCipher.init(Cipher.DECRYPT_MODE, this.key, parameterSpec);
                aesGCMCipher.updateAAD(this.aad);

                final byte[] data = new byte[this.cipherText.length + this.tag.length];

                System.arraycopy(this.cipherText, 0, data, 0, this.cipherText.length);
                System.arraycopy(this.tag, 0, data, this.cipherText.length, this.tag.length);

                this.plainText = aesGCMCipher.doFinal(data);

                return true;
            } catch (GeneralSecurityException e) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "Error decrypting : [" + e.getMessage() + "]", e);
                }

                return false;
            }
        }
    }

    // Getters - Setters

    /**
     * Decrypts the cipherText. Will also check validity of the data.
     *
     * @return true if it's a valid encrypted frame, false otherwise
     */
    private final boolean decryptLegacy() {
        int counter = 1;

        final BitVector c = new BitVector(this.cipherText);
        final BitVector a = new BitVector(this.aad);
        final BitVector h = aesEncrypt(new BitVector(16));
        BitVector p = null;

        BitVector y0 = BitVector.concatenate(new BitVector(iv), BitVector.convertFromInt(counter, 4));
        int n = c.length() / 16 + ((c.length() % 16 > 0) ? 1 : 0);
        int u = c.length() % 16;
        int m = a.length() / 16 + ((a.length() % 16 > 0) ? 1 : 0);
        int v = a.length() % 16;

        BitVector x = new BitVector(16);

        for (int i = 0; i < m; i++) {
            BitVector ai = a.get128Segment(i);
            x = BitVector.multiplication(BitVector.addition(x, ai), h);
        }
        for (int i = 0; i < n; i++) {
            BitVector ci = c.get128Segment(i);
            x = BitVector.multiplication(BitVector.addition(x, ci), h);
        }
        BitVector len = BitVector.concatenate(BitVector.convertFromInt(a.length() * 8, 8),
                BitVector.convertFromInt(c.length() * 8, 8));
        x = BitVector.multiplication(BitVector.addition(x, len), h);

        BitVector t2 = BitVector.addition(x, aesEncrypt(y0)).Msb2(this.tagSize);

//		System.out.println(t2.toString());
//		System.out.println(t.toString());

        if ((this.tag.length != 0) && (!Arrays.equals(t2.getValue(), this.tag))) {
            return false;
        }

        p = new BitVector(c.length());

        for (int i = 0; i < n; i++) {
            counter++;
            BitVector yi = BitVector.concatenate(new BitVector(this.iv), BitVector.convertFromInt(counter, 4));
            BitVector ci = c.get128Segment(i);
            BitVector pi = BitVector.addition(ci, aesEncrypt(yi));
            p.set128Segment(i, pi);
        }

        this.plainText = p.getValue();

        return true;
    }

    /**
     * Getter for the global encryption key
     *
     * @return - the globalEncryptionKey
     */
    public final BitVector getKey() {
        return new BitVector(this.key.getEncoded());
    }

    /**
     * Setter for the global encryption Key
     *
     * @param key - encryption key
     */
    public final void setGlobalKey(final BitVector key) {
        this.key = new SecretKeySpec(key.getValue(), KEY_ALGO);
    }

    /**
     * Getter for the initializationVector
     *
     * @return - the initializationVector
     */
    public final BitVector getInitializationVector() {
        return new BitVector(this.iv);
    }

    /**
     * Setter for the initializationVector
     *
     * @param iv - initializationVector
     */
    public final void setInitializationVector(final BitVector iv) {
        this.iv = iv.getValue();
    }

    /**
     * Getter for the plainText
     *
     * @return the plainText
     */
    public final BitVector getPlainText() {
        return new BitVector(this.plainText);
    }

    /**
     * Setter for the plainText
     *
     * @param p - the plainText
     */
    public final void setPlainText(final BitVector p) {
        this.plainText = p.getValue();
    }

    /**
     * Getter for the additional authenticationData.
     *
     * @return the additionalAuthenticationData
     */
    public final BitVector getAdditionalAuthenticationData() {
        return new BitVector(this.aad);
    }

    /**
     * Setter for the additional authenticationData
     *
     * @param a - the additionalAuthenticationData
     */
    public final void setAdditionalAuthenticationData(final BitVector a) {
        this.aad = a.getValue();
    }

    /**
     * Getter for the cipherText
     *
     * @return the cipherText
     */
    public final BitVector getCipherText() {
        return new BitVector(this.cipherText);
    }

    /**
     * Setter for the cipherText
     *
     * @param c - the cipherText
     */
    public final void setCipherText(final BitVector c) {
        this.cipherText = c.getValue();
    }

    /**
     * Getter for the authenticationTag
     *
     * @return the authenticationTag
     */
    public final BitVector getTag() {
        return new BitVector(this.tag);
    }

    /**
     * Setter for the authenticationTag
     *
     * @param t - the authenticationTag
     */
    public final void setTag(final BitVector t) {
        this.tag = t.getValue();
    }

    /**
     * Setter for the size of the authenticationTag.
     * Most common tagSizes are : 128, 120, 112, 104, or 96 bits
     * Default the size is 128bits
     *
     * @param tagSize - the size of the tag.
     */
    public final void setTagSize(final int tagSize) {
        this.tagSize = tagSize;
    }
}
