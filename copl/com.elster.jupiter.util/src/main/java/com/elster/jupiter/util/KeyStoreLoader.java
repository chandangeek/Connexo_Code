package com.elster.jupiter.util;

import javax.crypto.KeyGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class KeyStoreLoader {

    public static KeyStore generate(OutputStream out, String keyStoreType, KeyStoreAliasGenerator keyStoreAliasGenerator, char[] password) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null);

        for (String alias: keyStoreAliasGenerator.getAll()) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES"); // uses SecureRandom out of the box
            keyGenerator.init(128); // longer keys would required unlimited strength JCE

            final KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(keyGenerator.generateKey());
            keyStore.setEntry(alias, secretKeyEntry, new KeyStore.PasswordProtection(password));
        }

        keyStore.store(out, password);
        return keyStore;
    }

    public static KeyStore load(InputStream is, String keyStoreType, char[] password) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(is, password);
        return keyStore;
    }

}
