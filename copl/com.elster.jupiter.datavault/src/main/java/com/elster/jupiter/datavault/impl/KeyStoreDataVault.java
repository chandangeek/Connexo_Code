package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.util.Checks;
import java.io.IOException;
import java.io.OutputStream;
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
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;

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
public abstract class KeyStoreDataVault implements DataVault {
    private static final int IV_SIZE = 16;
    private static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";

    private KeyStore keyStore;
    private final Random random;
    private final ExceptionFactory exceptionFactory;

    private final char[] chars = {'g', '#', 'V', '+', 'p', 'R', '!', 'S', 'T', 'u', '5', '6'};

    @Inject
    public KeyStoreDataVault(Random random, ExceptionFactory exceptionFactory)  {
        this.random = random;
        this.exceptionFactory = exceptionFactory;
    }

    private KeyStore getKeyStore() {
        if (keyStore==null) {
            keyStore=readKeyStore(getPassword());
        }
        return keyStore;
    }

    abstract protected KeyStore readKeyStore(char[] password);

    @Override
    public String encrypt(byte[] plainText) {
        if (plainText==null) {
            return "";
        }
        try {
            int encryptionKeyId = random.nextInt(getKeyStore().size()) + 1;
            Cipher cipher = getEncryptionCipherForKey(getKeyAlias(encryptionKeyId));
            byte[] cipherText = cipher.doFinal(plainText);
            byte[] iv = cipher.getIV();
            byte[] concatenated = concatenateFields(cipherText, iv, (byte) encryptionKeyId);

            return new String(java.util.Base64.getEncoder().encode(concatenated));
        } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | CertificateException | IOException e) {
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
        } catch (IllegalBlockSizeException | BadPaddingException | UnrecoverableKeyException | InvalidKeyException | NoSuchAlgorithmException | KeyStoreException | NoSuchPaddingException | InvalidAlgorithmParameterException | CertificateException | IOException e) {
            throw  exceptionFactory.newException(MessageSeeds.DECRYPTION_FAILED);
        }
    }

    @Override
    public void createVault(OutputStream tempKeyStore) throws LocalizedException {
        try {
            doCreateVault(tempKeyStore);
        } catch (Exception e) {
            throw exceptionFactory.newException(MessageSeeds.KEYSTORE_CREATION_FAILED, e.getLocalizedMessage());
        }
    }

    private void doCreateVault(OutputStream tempKeyStore) throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException {
        final KeyStore jks = KeyStore.getInstance("JCEKS"); // JCEKS allows storing AES symmetric keys

        jks.load(null); // This initializes the empty keystore

        for (int keyCount=1; keyCount<=16; keyCount++) {
            final KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(createKey());
            jks.setEntry("KEY-" + keyCount, secretKeyEntry, new KeyStore.PasswordProtection(getPassword()));
        }

        jks.store(tempKeyStore, getPassword());

    }

    private static SecretKey createKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES"); // uses SecureRandom out of the box
        keyGenerator.init(128); // longer keys would required unlimited strength JCE
        return keyGenerator.generateKey();

    }

    private Cipher getEncryptionCipherForKey(String keyAlias) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException, IOException, CertificateException {
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        cipher.init(CipherMode.encrypt.asInt(), createKeySpecForKey(keyAlias));
        return cipher;
    }

    private Cipher getDecryptionCipherForKey(String keyAlias, byte[] iv) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException, CertificateException {
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        cipher.init(CipherMode.decrypt.asInt(), createKeySpecForKey(keyAlias), new IvParameterSpec(iv));
        return cipher;
    }

    private SecretKeySpec createKeySpecForKey(String keyAlias) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, IOException, CertificateException {
        final Key key = getKeyStore().getKey(keyAlias, getPassword());
        if (key==null) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_KEY); // Not giving details about key for security reasons
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