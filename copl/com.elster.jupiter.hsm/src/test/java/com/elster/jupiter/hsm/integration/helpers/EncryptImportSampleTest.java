package com.elster.jupiter.hsm.integration.helpers;

import com.elster.jupiter.hsm.integration.helpers.keys.TransportedDeviceKey;
import com.elster.jupiter.hsm.integration.helpers.keys.HsmKeySpecs;
import com.elster.jupiter.hsm.model.Message;
import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This is not a proper test. This class was created during integration test of HSM importer while nobody could provide a valid import file
 * and therefore we had to encrypt our own keys using this helper.
 */
@Ignore
public class EncryptImportSampleTest {

    public static final String ENCRYPT_OUT = "encrypt.out";
    public static final AsymmetricAlgorithm RSA_15 = AsymmetricAlgorithm.RSA_15;

    @Before
    public void setUp() throws IOException {
        File f = new File(ENCRYPT_OUT);
        if (f.exists()) {
            f.delete();
        }
        f.createNewFile();
    }

    @Test
    public void generateImportSample() throws
            InvalidKeyException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            IOException,
            InvalidAlgorithmParameterException,
            BadPaddingException,
            IllegalBlockSizeException,
            InvalidKeySpecException {
        // including IV and encrypted key
        TransportedDeviceKey dKey = TransportedDeviceKey.fromPlain(HsmKeySpecs.asymmetricKeyB64Spec, new Message("PasswordPasswordPasswordPassword"), new Message("PasswordPassword"), new Message("0123456789ABCDEF"));

        Message encryptedDeviceKey = encryptDeviceKey(dKey);
        Message encryptedWrapperKey = encryptWrapperKey(dKey);

        writeToFile(encryptedWrapperKey, encryptedDeviceKey);
    }

    private Message encryptWrapperKey(TransportedDeviceKey dKey) throws
            NoSuchPaddingException,
            NoSuchAlgorithmException,
            BadPaddingException,
            IllegalBlockSizeException,
            InvalidKeyException,
            InvalidKeySpecException {
        RSAEncryptionHelper rsaEncryptionHelper = new RSAEncryptionHelper();
        return rsaEncryptionHelper.encrypt(dKey.getWrappingKey(), AsymmetricAlgorithm.RSA_15, dKey.getHsmKey());

    }

    private Message encryptDeviceKey(TransportedDeviceKey dKey) throws
            IOException,
            InvalidKeyException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidAlgorithmParameterException {
        AESEncryptionHelper aesEncryptionHelper = new AESEncryptionHelper();
        Message encryptedPassword = aesEncryptionHelper.encrypt(dKey.getDeviceKey(), dKey.getWrappingKey(), dKey.getIv(), SymmetricAlgorithm.AES_256_CBC);
        Message ivAndEncrypted = getFullDeviceKey(dKey.getIv(), encryptedPassword);

        return ivAndEncrypted;
    }

    private synchronized void writeToFile(Message wrappedKey, Message fullDeviceKey) throws FileNotFoundException {
        File f = new File(ENCRYPT_OUT);
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), Charset.defaultCharset()))) {
            String wrapperKeyTxt = "Wrapper key:" + wrappedKey.toBase64();
            System.out.println(wrapperKeyTxt);
            pw.println(wrapperKeyTxt);
            String deviceKeyTxt = "Device Password:" + fullDeviceKey.toBase64();
            System.out.println(deviceKeyTxt);
            pw.println(deviceKeyTxt);
        }
    }

    public static Message getFullDeviceKey(Message initVector, Message encryptedPassword) {
        if (!initVector.getCharSet().equals(encryptedPassword.getCharSet()))  {
            throw new RuntimeException("Incompatible charset");
        }
        byte[] iv = initVector.getBytes();
        byte[] epb = encryptedPassword.getBytes();
        byte[] allBytes = new byte[iv.length + epb.length];
        System.arraycopy(iv, 0, allBytes, 0, iv.length);
        System.arraycopy(epb, 0, allBytes, iv.length, epb.length);


        return new Message(allBytes);

    }
}
