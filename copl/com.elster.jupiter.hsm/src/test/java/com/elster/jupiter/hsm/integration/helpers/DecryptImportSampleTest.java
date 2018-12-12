package com.elster.jupiter.hsm.integration.helpers;

import com.elster.jupiter.hsm.integration.helpers.keys.AsymetricKey;
import com.elster.jupiter.hsm.integration.helpers.keys.Encoder;
import com.elster.jupiter.hsm.model.Message;
import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * This is just a helper to decrypt keys in import file therefore not a proper test -> ignore it and run it manually
 */
@Ignore
public class DecryptImportSampleTest {


    public static final int IV_LENGTH = 16;

    @Test
    public void decrypt() throws
            NoSuchPaddingException,
            NoSuchAlgorithmException,
            IllegalBlockSizeException,
            BadPaddingException,
            InvalidKeyException,
            InvalidKeySpecException,
            IOException,
            InvalidAlgorithmParameterException {

        String base64PublicWrapperKey ="AINpuxDlmNVn0ckKlkGM7nGm2AFQ9EaMLoZ03XhyUCTdCRwDS/zbQiIW9fVNtqBJKwNEGdPALltosQs/EdryUdL/J/QNOHe1coK0a1zgNfSAuRDZqdLUR42T2l5HFpmqxLtHSzyfu8e5Axy60nkeV+K/qQMtLUFdb2Aq0IQaljHZR4taXheG3OxO60dzajP2ESQHBGY2nc1VbDw3zR6I6dRTtycFGdT/RRP3VdXxaNT8bHklMsEttlsK0L8+qJdulouEm8Q/+bFjiDf2faszEhqQuJyu6GlFR1eqlIyUCiHiFzLcK/Kw6FZxwod6jyO29keP/7C3DiRqMIhZcbGgPoE=";
        String base64PrivateWrapperKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCDabsQ5ZjVZ9HJ" +
                "CpZBjO5xptgBUPRGjC6GdN14clAk3QkcA0v820IiFvX1TbagSSsDRBnTwC5baLEL" +
                "PxHa8lHS/yf0DTh3tXKCtGtc4DX0gLkQ2anS1EeNk9peRxaZqsS7R0s8n7vHuQMc" +
                "utJ5Hlfiv6kDLS1BXW9gKtCEGpYx2UeLWl4XhtzsTutHc2oz9hEkBwRmNp3NVWw8" +
                "N80eiOnUU7cnBRnU/0UT91XV8WjU/Gx5JTLBLbZbCtC/PqiXbpaLhJvEP/mxY4g3" +
                "9n2rMxIakLicruhpRUdXqpSMlAoh4hcy3CvysOhWccKHeo8jtvZHj/+wtw4kajCI" +
                "WXGxoD6BAgMBAAECggEAcU7tGPie+wL1g985B3Q49I3jmd7vgRTF2PFTNNN7/w0H" +
                "GbxN/MwhDbu2f7huS25LRBmoG0iNsAto/EV5Y8ukecW/8VKk6bLt9X0TRZWdcCC9" +
                "caX8I+kdeRmPI9MvzXfNAZ3LZY5lHySLZm4/pGdhaAXR+QcvCjlCGy1PdVhnQLrT" +
                "U3Z3KrcU3MY3qyhB0r5pxHLkyYjOLjUMQ9P/9/fq+hzOjCGlXzLqF5Dwk2+ZoQYS" +
                "DKyJTBhLi/H+bSDxg4vMtuZH3Q+6ZGiWybfNhWktV3uKdpjTlNAGh/HC6Gv2v/RQ" +
                "t6k9ZQCNgRBc6iqqiKRPbUAk7BFh4HTcY6N7poqV8QKBgQDZ2RaAD6nqLhssBIMc" +
                "lcTN+cj4BhOf+bKXUmQ9u4CwVrOogUCgMlJhGGCg9DEQTKqCWQNiJ4Sy9ylipvAk" +
                "LZe5c2hyLu34Q7XfBMwyV4C3q+CONlh885g3KrRzhucNpM/Fg39mUq3LRyNszBWG" +
                "DRObJ+y/eibbGxbCoH4g7sQvBwKBgQCabXNHZqCfzWRYtOdv+wJA2cM3ZMS+aBJE" +
                "Q9V2uU7rr8k5048nxw/KeY3vaXqzY9fV2nrokmeK4dpjY6PSCwEjuAkIK6ge3/4I" +
                "XNUgoKnzWvM0qjSF5CneJI1EHQDD0tNvXiuDb5Cl2bHUg/ajH3NHnjLxcFLZkCd/" +
                "1/JeeEe8NwKBgQCloWx+CWrC5jM6kGOvRB/SC2xFRzl8lDi43KfiV8FMUV9faoZc" +
                "RmZj9Ejjl0YGgoPRfyQXaLx35XvNyecjWpbVJW3wAsZjz/djjR5D2EvBWijtd1xo" +
                "BD1jFFEG1TavJPPj1Er03T/OIlRI0BI9TYvul7hlDNamGD6B08yR9Xer2QKBgEIC" +
                "sxo8RkDI3TIF8kAtvaLW8tmE24zAk75WeLLNN20LsSpkwnawYpGcMA28utRfw20I" +
                "fS1ZF6WIMX/2oZoxVgVcr4+siarY+I++juPpiXwazocekmEBTNxGJ5SMCT2rqvGa" +
                "AWOnlt8uFRPQbOcBlJdyijgmWWPieN/vAQ1cwN01AoGAdtwXGstT3EIEqBFyMmmw" +
                "7LujUdL03nh1A5ydzaWGosVVMRcRo2LiargJU/pmg0q6YUSFU5u5Mo9Z/vE+r2Ng" +
                "9bkD9LdvArfKQQk9lo7CIIlXcBrFFNEQXwVjhSfkdbEBYZdEtjO/dOZH0ebVUbrJ" +
                "1fGxRIewmdn7psJQ1XkPb04=";
        AsymetricKey key = new AsymetricKey(base64PublicWrapperKey, base64PrivateWrapperKey, Encoder.BASE64);

        RSAEncryptionHelper rsaEncryptionHelper = new RSAEncryptionHelper();
        Message encryptedWrapperKey = new Message(org.bouncycastle.util.encoders.Base64.decode("Sul0KBRGrXMjpdDKPHm2s+SVAIqOl62EoOV/xZ0r2k8x5bulmyTXCaE8KGk8oU852tfWZKo7pA0ryFaLA6DyMhx+ams4/cdOqDwMl7zFJfsGv5in/3XVBKgpO2GwgGl4UqEICspRPbibiz3mey9l9oZ9KJRcH5I8NxxeQehYvg4toGE0iRiboA0SdAstE6tMjfPEdqBGhQ5R60GQ+pteqeXfWUtE5a+xCZvvvFh5sZqELAaYL7EKkG5T3cv3hQg5EIO+j3JaNBVdWaGIHdZXhCyaTdQwv2vop460R5vVL8Zbpz07H5eUtIWPWUUgg3oxypoxmxZQWq7yzccoAg8cYw=="));
        Message decryptedWrapperKey = rsaEncryptionHelper.decrypt(encryptedWrapperKey, AsymmetricAlgorithm.RSA_15, key);


        String base64DevicePassword = "CFjDANg5oGte1L7JAATXGg2gdqySBiD0nd6pA4/9n6ljKfjX4N4tgys3hRRB8frJ";
        Message encDevicePassword = new Message(org.bouncycastle.util.encoders.Base64.decode(base64DevicePassword));


        AESEncryptionHelper aesEncryptionHelper = new AESEncryptionHelper();
        Message deviceKeyDecrypted = aesEncryptionHelper.decrypt(getCipher(encDevicePassword), decryptedWrapperKey, getInitializationVector(encDevicePassword), SymmetricAlgorithm.AES_256_CBC);

        System.out.println("Device key:" + deviceKeyDecrypted);
        System.out.println("Device key (HEX):" + deviceKeyDecrypted.toHex());
        System.out.println("Device key (BASE64):" + deviceKeyDecrypted.toBase64());

    }

    public Message getCipher(Message encryptedMessage) {
        byte[] encryptedKey = encryptedMessage.getBytes();
        byte[] cipher = new byte[encryptedKey.length - IV_LENGTH];
        System.arraycopy(encryptedKey, IV_LENGTH, cipher, 0, encryptedKey.length - IV_LENGTH);
        return new Message(cipher);
    }

    public Message getInitializationVector(Message encryptedKey){
        byte[] initializationVector = new byte[IV_LENGTH];
        System.arraycopy(encryptedKey.getBytes(), 0, initializationVector, 0, IV_LENGTH);
        return new Message(initializationVector);
    }

}
