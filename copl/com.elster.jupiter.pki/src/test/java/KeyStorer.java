/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by bvn on 3/22/17.
 */
public class KeyStorer {
    static final String filename = "keys.jks";
    public static final String PASSWORD = "foo123";

    public static void main(String[] args) throws
            KeyStoreException,
            CertificateException,
            NoSuchAlgorithmException,
            IOException, InvalidKeySpecException {
        Security.addProvider(new BouncyCastleProvider());

        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(null);

        addSecretKey(keyStore, "31313232333334343535363637373838", "MK", "aes");
        addSecretKey(keyStore, "000102030405060708090a0b0c0d0e0f", "EK", "aes");
        addSecretKey(keyStore, "000102030405060708090a0b0c0d0e0f", "AK", "aes");
        try (FileOutputStream outputStream = new FileOutputStream(filename)) {
            keyStore.store(outputStream, PASSWORD.toCharArray());
        }
    }

    private static void addSecretKey(KeyStore keyStore, String encodedKey, String alias, String algorithm) throws
            KeyStoreException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] key = DatatypeConverter.parseHexBinary(encodedKey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, algorithm);
        keyStore.setKeyEntry(alias, secretKeySpec, PASSWORD.toCharArray(), null);
    }
}
