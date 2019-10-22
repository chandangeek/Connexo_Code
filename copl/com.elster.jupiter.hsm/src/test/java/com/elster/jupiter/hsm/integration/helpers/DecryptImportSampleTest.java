package com.elster.jupiter.hsm.integration.helpers;

import com.elster.jupiter.hsm.integration.helpers.keys.TransportedDeviceKey;
import com.elster.jupiter.hsm.integration.helpers.keys.HsmKeySpecs;
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


        String encryptedWrappingKeyB64 = "TeNLNMh54TUPNQ/ezoeUdDNEGJJSmUTWexJQQDRbQ9p2iQgL3xEjppDDTh2q19ZUsOZHlxQAZuLYELqRmUY2sDfYBfQxfef985R2IUoHgv8mcC6ObxaDNqMeJXTCJmiq7A85HdO5oUYEqnk5SZG+iNp5KvioVWFPGsGGDbc59dS0aARiVkNwG7Br8QhIpWv+KXWWoY9D/s+Xt4vYxLX2DRuPy9OIgb4iAyS/lGjSNjNC5+H1aeg9zPgtEUDD2fSmXSdQOmB5OK3hwSAWrKj3KjjZWJ/vX8/qjMjcEnADKAUh7jK9X6/vU6uhKRQGniw9Us8M9xBleVKNEg3OWClakg==";
        String encryptedDeviceKeyB64 = "MDEyMzQ1Njc4OUFCQ0RFRvb34cKxnOndxM3ElgEpi1mSnZ9WQO04/9S2IzEWNFDj";

        TransportedDeviceKey encDevicePassword = TransportedDeviceKey.fromEncrypted(HsmKeySpecs.asymmetricKeyB64Spec, encryptedWrappingKeyB64, encryptedDeviceKeyB64);

        RSAEncryptionHelper rsaEncryptionHelper = new RSAEncryptionHelper();
        Message decryptedWrapperKey = rsaEncryptionHelper.decrypt(encDevicePassword.getWrappingKey(), AsymmetricAlgorithm.RSA_15, encDevicePassword.getHsmKey());

        AESEncryptionHelper aesEncryptionHelper = new AESEncryptionHelper();
        Message deviceKeyDecrypted = aesEncryptionHelper.decrypt(encDevicePassword.getDeviceKey(), decryptedWrapperKey, encDevicePassword.getIv(), SymmetricAlgorithm.AES_256_CBC);

        System.out.println("Device key:" + deviceKeyDecrypted);
        System.out.println("Device key (HEX):" + deviceKeyDecrypted.toHex());
        System.out.println("Device key (BASE64):" + deviceKeyDecrypted.toBase64());

    }
}
