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

        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes());
        c.init(Cipher.ENCRYPT_MODE, keySpec, iv);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, c);
        cipherOutputStream.write(plainMsg.getBytes());
        cipherOutputStream.flush();
        cipherOutputStream.close();

        return new Message(outputStream.toByteArray());

    }

}
