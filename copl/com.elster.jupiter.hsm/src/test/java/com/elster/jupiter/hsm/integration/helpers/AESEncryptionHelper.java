package com.elster.jupiter.hsm.integration.helpers;

import com.elster.jupiter.hsm.model.Message;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;

/**
 * This is not a proper test. This class was created during integration test of HSM importer while nobody could provide a valid import file
 * and therefore we had to encrypt our own keys using this helper.
 */
public class AESEncryptionHelper {

    public Message encrypt(Message plainMsg, Message encryptionKey, Message initVector, SymmetricAlgorithm alg) throws
            NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            IOException,
            InvalidAlgorithmParameterException {
        Cipher c = Cipher.getInstance(alg.getCipher());
        SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), alg.getAlgorithm());

        initCipher(initVector, c, keySpec, Cipher.ENCRYPT_MODE);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, c);
        cipherOutputStream.write(plainMsg.getBytes());
        cipherOutputStream.flush();
        cipherOutputStream.close();

        return new Message(outputStream.toByteArray());

    }

    public Message decrypt(Message encryptedMsg, Message encryptionKey, Message initVector, SymmetricAlgorithm alg) throws
            NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            IOException,
            InvalidAlgorithmParameterException {
        Cipher c = Cipher.getInstance(alg.getCipher());
        SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), alg.getAlgorithm());

        initCipher(initVector, c, keySpec, Cipher.DECRYPT_MODE);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, c);
        cipherOutputStream.write(encryptedMsg.getBytes());
        cipherOutputStream.flush();
        cipherOutputStream.close();

        return new Message(outputStream.toByteArray());

    }

    private void initCipher(Message initVector, Cipher c, SecretKeySpec keySpec, int mode) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (Objects.isNull(initVector)) {
            c.init(mode, keySpec);
            return;
        }
        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes());
        c.init(mode, keySpec, iv);
    }

    @Test
    public void test() throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IOException {

        Message initVector = new Message("0123456789ABCDEF");
        Message key = new Message("PasswordPassword");
        Message plainMsg = new Message("plainMsg");

        Message encrypt = encrypt(plainMsg, key, initVector, SymmetricAlgorithm.AES_256_CBC);
        Message decrypt = decrypt(encrypt, key, initVector, SymmetricAlgorithm.AES_256_CBC);

        Assert.assertEquals(plainMsg, decrypt);

    }

}
