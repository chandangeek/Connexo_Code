package com.elster.jupiter.hsm.integration.helpers;

import com.elster.jupiter.hsm.integration.helpers.keys.AsymmetricKey;
import com.elster.jupiter.hsm.integration.helpers.keys.HsmKeySpecs;
import com.elster.jupiter.hsm.integration.helpers.keys.OurKeySpecs;
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
import java.util.Base64;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This is not a proper test. This class was created during integration test of HSM importer while nobody could provide a valid import file
 * and therefore we had to encrypt our own keys using this helper.
 */
@Ignore
public class EncryptImportSampleTest {

    private static final AsymmetricKey KEY = HsmKeySpecs.asymmetricKeyB64Spec;

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

        // including IV and encrypted key
        Message encryptedDeviceKey = encryptDeviceKey(OurKeySpecs.deviceKey, OurKeySpecs.initVector, OurKeySpecs.wrapperKey);
        String encryptedWrapperKey = encryptWrapperKey(OurKeySpecs.wrapperKey);

        writeToFile(encryptedWrapperKey, encryptedDeviceKey);
    }

    private String encryptWrapperKey(Message encryptionPassword) throws
            NoSuchPaddingException,
            NoSuchAlgorithmException,
            BadPaddingException,
            IllegalBlockSizeException,
            InvalidKeyException,
            InvalidKeySpecException {
        RSAEncryptionHelper rsaEncryptionHelper = new RSAEncryptionHelper();
        Message encrypt = rsaEncryptionHelper.encrypt(encryptionPassword, AsymmetricAlgorithm.RSA_15, KEY);
        return Base64.getEncoder().encodeToString(encrypt.getBytes());

    }

    private Message encryptDeviceKey(Message message, Message initVector, Message encryptionKey ) throws
            IOException,
            InvalidKeyException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidAlgorithmParameterException {
        AESEncryptionHelper aesEncryptionHelper = new AESEncryptionHelper();
        Message encryptedPassword = aesEncryptionHelper.encrypt(message, encryptionKey, initVector, SymmetricAlgorithm.AES_256_CBC);
        Message ivAndEncrypted = getFullDeviceKey(initVector, encryptedPassword);

        return ivAndEncrypted;
    }

    private synchronized void writeToFile(String wrappedKey, Message fullDeviceKey) throws FileNotFoundException {
        File f = new File(ENCRYPT_OUT);
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), Charset.defaultCharset()))) {
            String wrapperKeyTxt = "Wrapper key:" + wrappedKey;
            System.out.println(wrapperKeyTxt);
            pw.println(wrapperKeyTxt);
            String deviceKeyTxt = "Device Password:" + fullDeviceKey;
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
