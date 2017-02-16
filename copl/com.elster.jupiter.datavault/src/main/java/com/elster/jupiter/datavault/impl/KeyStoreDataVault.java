/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.KeyStoreService;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.util.Checks;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Random;

/**
 * KeyStore data vault provides data encryption by means of a set of AES128 keys store in a java KeyStore. This
 * key store is packaged as a JAR resource. Block mode is CBC, the resulting IV is stored together with the cipherText
 *
 * Any value that can be represented as a byte[] can be encrypted.
 *
 * The encryption entails:
 * -choosing a random key for encryption (from a set of keys in the key store)
 * -appending the initialization vector to the cipherText
 * -appending that key's id to the cipherText
 * -base64 encoding the encrypted value for easy storage as varchar
 */
class KeyStoreDataVault implements ServerDataVault {

    private static final int IV_SIZE = 16;
    private static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";
    private static final String KEYSTORE_TYPE = "JCEKS"; // JCEKS allows storing AES symmetric keys

    private KeyStore keyStore;
    private final Random random;
    private final ExceptionFactory exceptionFactory;
    private final ServerKeyStoreService keyStoreService;

    private final char[] chars = {'1','#','g','W','X','i','A','E','y','9','R','n','b','6','M','%','C','o','j','E'};

    @Inject
    KeyStoreDataVault(Random random, ExceptionFactory exceptionFactory, ServerKeyStoreService keyStoreService)  {
        this.random = random;
        this.exceptionFactory = exceptionFactory;
        this.keyStoreService = keyStoreService;
    }

    void readKeyStore(DataVaultKeyStore dataVaultKeyStore) {
        try (InputStream inputStream = new ByteArrayInputStream(dataVaultKeyStore.getKeyStoreBytes())) {
            this.readKeyStore(inputStream);
        } catch (IOException e) {
            throw exceptionFactory.newException(MessageSeeds.KEYSTORE_LOAD_FILE);
        }
    }

    void readKeyStore(InputStream keyStoreBytes) {
        try {
            this.keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStore.load(keyStoreBytes, getPassword());
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw exceptionFactory.newException(MessageSeeds.KEYSTORE_LOAD_FILE);
        }
    }

    @Override
    public String encrypt(byte[] plainText) {
        if (this.keyStore==null) {
            throw exceptionFactory.newException(MessageSeeds.NO_KEYSTORE);
        }
        if (plainText==null) {
            return "";
        }
        try {
            int encryptionKeyId = random.nextInt(this.keyStore.size()) + 1;
            Cipher cipher = getEncryptionCipherForKey(getKeyAlias(encryptionKeyId));
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
        concatenated[concatenated.length-1]= encryptionKeyId;
        return concatenated;
    }

    @Override
    public byte[] decrypt(String encrypted) {
        if (this.keyStore==null) {
            throw exceptionFactory.newException(MessageSeeds.NO_KEYSTORE);
        }
        if (Checks.is(encrypted).emptyOrOnlyWhiteSpace()) {
            return new byte[0];
        }
        try {
            byte[] encryptedString = java.util.Base64.getDecoder().decode(encrypted);
            byte encryptionKeyId = encryptedString[encryptedString.length-1];
            byte[] cipherText = Arrays.copyOf(encryptedString, encryptedString.length-IV_SIZE-1);
            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(encryptedString, cipherText.length, iv, 0, iv.length);

            final Cipher cipher = getDecryptionCipherForKey(getKeyAlias(encryptionKeyId), iv);
            return cipher.doFinal(cipherText);
        } catch (IllegalBlockSizeException | BadPaddingException | UnrecoverableKeyException | InvalidKeyException | NoSuchAlgorithmException | KeyStoreException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            throw  exceptionFactory.newException(MessageSeeds.DECRYPTION_FAILED);
        }
    }

    @Override
    public void createVault() throws LocalizedException {
        try {
            doCreateVault();
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw exceptionFactory.newException(MessageSeeds.KEYSTORE_CREATION_FAILED, e.getLocalizedMessage());
        }
    }

    private void doCreateVault() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException {
        KeyStoreService.Builder builder = this.keyStoreService.newDataVaultKeyStore(KEYSTORE_TYPE);
        for (int keyCount = 1; keyCount <= 16; keyCount++) {
            final KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(createKey());
            builder.setEntry("KEY-" + keyCount, secretKeyEntry, new KeyStore.PasswordProtection(getPassword()));
        }
        builder.build(getPassword());
    }

    private static SecretKey createKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES"); // uses SecureRandom out of the box
        keyGenerator.init(128); // longer keys would required unlimited strength JCE
        return keyGenerator.generateKey();

    }

    private Cipher getEncryptionCipherForKey(String keyAlias) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        cipher.init(CipherMode.encrypt.asInt(), createKeySpecForKey(keyAlias));
        return cipher;
    }

    private Cipher getDecryptionCipherForKey(String keyAlias, byte[] iv) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        cipher.init(CipherMode.decrypt.asInt(), createKeySpecForKey(keyAlias), new IvParameterSpec(iv));
        return cipher;
    }

    private SecretKeySpec createKeySpecForKey(String keyAlias) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        final Key key = this.keyStore.getKey(keyAlias, getPassword());
        if (key==null) {
            throw exceptionFactory.newException(MessageSeeds.DECRYPTION_FAILED); // Not giving details about key for security reasons
        }
        byte[] raw = key.getEncoded();

        return new SecretKeySpec(raw, "AES");
    }

    private String getKeyAlias(int keyId) {
        return "key-"+keyId;
    }

    private char[] getPassword() {
        return chars;
    }

}