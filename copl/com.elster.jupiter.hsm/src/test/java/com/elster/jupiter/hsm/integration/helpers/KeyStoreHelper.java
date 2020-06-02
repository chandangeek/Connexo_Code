package com.elster.jupiter.hsm.integration.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;


public class KeyStoreHelper {

    private final KeyStore ks;

    public KeyStoreHelper(String jksFile, char[] pwdArray) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        ks = KeyStore.getInstance("JKS");
        try(InputStream jksInputStream = this.getClass().getClassLoader().getResourceAsStream(jksFile)) {
            ks.load(jksInputStream, pwdArray);
        }
    }


    public Key getKey(String alias, char[] pwdArray) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        Key key = ks.getKey(alias, pwdArray);
        if (Objects.isNull(key)) {
            throw new KeyStoreException("Could not find key with alias:" + alias);
        }

        return key;
    }

    public X509Certificate getCertificate(String alias, char[] pwd) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {
        KeyStore.PrivateKeyEntry key = (KeyStore.PrivateKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(pwd));
        return (X509Certificate) key.getCertificate();
    }



}
