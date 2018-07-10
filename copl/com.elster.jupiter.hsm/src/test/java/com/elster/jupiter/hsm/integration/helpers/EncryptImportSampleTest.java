package com.elster.jupiter.hsm.integration.helpers;

import com.elster.jupiter.hsm.model.Message;
import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;

import sun.misc.BASE64Encoder;

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

        Message encryptionPassword = new Message("PasswordPasswordPasswordPassword");
        Message initVector = new Message("0123456789ABCDEF");
        Message devicePassword = new Message("DevicePasswor");


        // including IV and encrypted key
        String fullDeviceKey = doDeviceKeys(devicePassword, initVector, encryptionPassword);
        String wrappedKey = doWrapperKey(encryptionPassword);

        writeToFile(wrappedKey, fullDeviceKey);
    }

    private String doWrapperKey(Message encryptionPassword) throws
            NoSuchPaddingException,
            NoSuchAlgorithmException,
            BadPaddingException,
            IllegalBlockSizeException,
            InvalidKeyException,
            InvalidKeySpecException {
        RSAEncryptionHelper rsaEncryptionHelper = new RSAEncryptionHelper();
        Message encrypt = rsaEncryptionHelper.encrypt(encryptionPassword, AsymmetricAlgorithm.RSA_15);
        BASE64Encoder base64Encoder = new BASE64Encoder();
        return base64Encoder.encode(encrypt.getBytes());

    }

    private String doDeviceKeys(Message devicePassword, Message initVector,Message encryptionPassword ) throws
            IOException,
            InvalidKeyException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidAlgorithmParameterException {
        AESEncryptionHelper aesEncryptionHelper = new AESEncryptionHelper();
        Message encryptedPassword = aesEncryptionHelper.encrypt(devicePassword, encryptionPassword, initVector, SymmetricAlgorithm.AES_256_CBC);
        String ivAndEncrypted = getFullDeviceKey(initVector, encryptedPassword);

        return ivAndEncrypted;
    }

    private synchronized void writeToFile(String wrappedKey, String fullDeviceKey) throws FileNotFoundException {
        File f = new File(ENCRYPT_OUT);
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), Charset.defaultCharset()))) {
            pw.println("Wrapper key:" + wrappedKey);
            pw.println("Device Password:" + fullDeviceKey);
        }
    }

    private String getFullDeviceKey(Message initVector, Message encryptedPassword) {
        if (!initVector.getCharSet().equals(encryptedPassword.getCharSet()))  {
            throw new RuntimeException("Incompatible charset");
        }
        byte[] iv = initVector.getBytes();
        byte[] epb = encryptedPassword.getBytes();
        byte[] allBytes = new byte[iv.length + epb.length];
        System.arraycopy(iv, 0, allBytes, 0, iv.length);
        System.arraycopy(epb, 0, allBytes, iv.length, epb.length);

        BASE64Encoder base64Encoder = new BASE64Encoder();
        return base64Encoder.encode(allBytes);

    }
}
