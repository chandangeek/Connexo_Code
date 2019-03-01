package com.elster.jupiter.hsm.integration.helpers;

import com.elster.jupiter.hsm.integration.helpers.keys.AsymmetricKey;
import com.elster.jupiter.hsm.integration.helpers.keys.HsmKeySpecs;
import com.elster.jupiter.hsm.integration.helpers.keys.OurKeySpecs;
import com.elster.jupiter.hsm.model.Message;
import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.junit.Assert;
import org.junit.Test;

/**
 * This is not a proper test. This class was created during integration test of HSM importer while nobody could provide a valid import file
 * and therefore we had to encrypt our own keys using this helper. Of course one needs to provide proper public key to work with this impl.
 */
public class RSAEncryptionHelper {

    public Message encrypt(Message plainTxt, AsymmetricAlgorithm alg, AsymmetricKey key) throws
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            BadPaddingException,
            IllegalBlockSizeException, InvalidKeySpecException {

        Cipher c = Cipher.getInstance(alg.getAlgorithm());


        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(key.getPublicKey());
        KeyFactory kf = KeyFactory.getInstance(alg.getAlgorithm());
        PublicKey publicKey = kf.generatePublic(publicKeySpec);
        c.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] bytes = c.doFinal(plainTxt.getBytes());

        return new Message(bytes);
    }

    public Message decrypt(Message encMsg, AsymmetricAlgorithm alg, AsymmetricKey key) throws
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            BadPaddingException,
            IllegalBlockSizeException, InvalidKeySpecException {

        Cipher c = Cipher.getInstance(alg.getAlgorithm());

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(key.getPrivateKey());
        KeyFactory kf = KeyFactory.getInstance(alg.getAlgorithm());
        PrivateKey privateKey = kf.generatePrivate(privateKeySpec);
        c.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] bytes = c.doFinal(encMsg.getBytes());

        return new Message(bytes);
    }

    @Test
    public void test() throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
        Message testMsg = OurKeySpecs.wrapperKey;
        System.out.println("Plain: " + testMsg);
        AsymmetricKey asymmetricKey = HsmKeySpecs.asymmetricKeyHexSpec;
        Message encryptedMsg = this.encrypt(testMsg, AsymmetricAlgorithm.RSA_15, asymmetricKey);
        System.out.println("Encrypted B64:" + encryptedMsg.toBase64());
        System.out.println("Encrypted HEX:" + encryptedMsg.toHex());
        Assert.assertEquals(testMsg, this.decrypt(encryptedMsg, AsymmetricAlgorithm.RSA_15, asymmetricKey));
    }

}
