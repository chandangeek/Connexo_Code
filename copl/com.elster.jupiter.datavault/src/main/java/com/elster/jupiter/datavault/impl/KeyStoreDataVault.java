/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.KeyStoreAliasGenerator;
import com.elster.jupiter.util.KeyStoreCache;
import com.elster.jupiter.util.KeyStoreLoader;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Random;

/**
 * KeyStore data vault provides data encryption by means of a set of AES128 keys store in a java KeyStore. This
 * key store is packaged as a JAR resource. Block mode is CBC, the resulting IV is stored together with the cipherText
 * <p>
 * Any value that can be represented as a byte[] can be encrypted.
 * <p>
 * The encryption entails:
 * -choosing a random key for encryption (from a set of keys in the key store)
 * -appending the initialization vector to the cipherText
 * -appending that key's id to the cipherText
 * -base64 encoding the encrypted value for easy storage as varchar
 */
class KeyStoreDataVault implements DataVault {

    private static final int IV_SIZE = 16;
    private static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";
    private static final String KEYSTORE_TYPE = "JCEKS"; // JCEKS allows storing AES symmetric keys
    private static final KeyStoreAliasGenerator KEY_STORE_ALIAS_GENERATOR = new KeyStoreAliasGenerator("key-", 16);

    private KeyStoreCache keyStoreWrapper;
    private final Random random;
    private final ExceptionFactory exceptionFactory;

    // we use same password for both store and keys within
    private final char[] password = {'1', '#', 'g', 'W', 'X', 'i', 'A', 'E', 'y', '9', 'R', 'n', 'b', '6', 'M', '%', 'C', 'o', 'j', 'E'};

    @Inject
    KeyStoreDataVault(Random random, ExceptionFactory exceptionFactory) {
        this.random = random;
        this.exceptionFactory = exceptionFactory;
    }

    void readKeyStore(InputStream keyStoreBytes) {
        try {
            this.keyStoreWrapper = new KeyStoreCache(KeyStoreLoader.load(keyStoreBytes, KEYSTORE_TYPE, password));
        } catch (Exception e) {
            throw exceptionFactory.newException(MessageSeeds.KEYSTORE_LOAD_FILE);
        }
    }

    @Override
    public String encrypt(byte[] plainText) {
        if (this.keyStoreWrapper == null) {
            throw exceptionFactory.newException(MessageSeeds.NO_KEYSTORE);
        }
        if (plainText == null) {
            return "";
        }
        try {
            int encryptionKeyId = random.nextInt(this.keyStoreWrapper.size()) + 1;
            Cipher cipher = getEncryptionCipherForKey(encryptionKeyId);
            byte[] cipherText = cipher.doFinal(plainText);
            byte[] iv = cipher.getIV();
            byte[] concatenated = concatenateFields(cipherText, iv, (byte) encryptionKeyId);

            return new String(java.util.Base64.getEncoder().encode(concatenated));
        } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw exceptionFactory.newException(MessageSeeds.ENCRYPTION_FAILED, e.getLocalizedMessage());
        }
    }

    private byte[] concatenateFields(byte[] cipherText, byte[] iv, byte encryptionKeyId) {

        byte[] concatenated = Arrays.copyOf(cipherText, cipherText.length + iv.length + 1);
        System.arraycopy(iv, 0, concatenated, cipherText.length, iv.length);
        concatenated[concatenated.length - 1] = encryptionKeyId;
        return concatenated;
    }

    @Override
    public byte[] decrypt(String encrypted) {
        if (this.keyStoreWrapper == null) {
            throw exceptionFactory.newException(MessageSeeds.NO_KEYSTORE);
        }
        if (Checks.is(encrypted).emptyOrOnlyWhiteSpace()) {
            return new byte[0];
        }
        try {
            byte[] encryptedString = java.util.Base64.getDecoder().decode(encrypted);
            byte encryptionKeyId = encryptedString[encryptedString.length - 1];
            byte[] cipherText = Arrays.copyOf(encryptedString, encryptedString.length - IV_SIZE - 1);
            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(encryptedString, cipherText.length, iv, 0, iv.length);

            final Cipher cipher = getDecryptionCipherForKey(encryptionKeyId, iv);
            return cipher.doFinal(cipherText);
        } catch (IllegalBlockSizeException | BadPaddingException | UnrecoverableKeyException | InvalidKeyException | NoSuchAlgorithmException | KeyStoreException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            throw exceptionFactory.newException(MessageSeeds.DECRYPTION_FAILED, e);
        }
    }

    @Override
    public void createVault(OutputStream stream) throws LocalizedException {
        try {
            KeyStoreLoader.generate(stream, KEYSTORE_TYPE, KEY_STORE_ALIAS_GENERATOR, password);
        } catch (Exception e) {
            throw exceptionFactory.newException(MessageSeeds.KEYSTORE_CREATION_FAILED, e.getLocalizedMessage());
        }
    }

    private Cipher getEncryptionCipherForKey(int keyAlias) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        cipher.init(CipherMode.encrypt.asInt(), createKeySpecForKey(keyAlias));
        return cipher;
    }

    private Cipher getDecryptionCipherForKey(int keyAlias, byte[] iv) throws
            NoSuchAlgorithmException,
            KeyStoreException,
            UnrecoverableKeyException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        cipher.init(CipherMode.decrypt.asInt(), createKeySpecForKey(keyAlias), new IvParameterSpec(iv));
        return cipher;
    }

    private SecretKeySpec createKeySpecForKey(int keyAlias) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        final Key key = this.keyStoreWrapper.getKey(KEY_STORE_ALIAS_GENERATOR.getAlias(keyAlias), password);
        if (key == null) {
            throw exceptionFactory.newException(MessageSeeds.DECRYPTION_FAILED); // Not giving details about key for security reasons
        }
        byte[] raw = key.getEncoded();

        return new SecretKeySpec(raw, "AES");
    }

}