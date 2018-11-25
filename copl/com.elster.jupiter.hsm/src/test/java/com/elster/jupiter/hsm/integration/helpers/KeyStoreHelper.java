package com.elster.jupiter.hsm.integration.helpers;

import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Objects;


public class KeyStoreHelper {

    private final KeyStore ks;

    public KeyStoreHelper(String jksFile, char[] pwdArray) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        ks = KeyStore.getInstance("JKS");
        ks.load(this.getClass().getClassLoader().getResourceAsStream(jksFile),pwdArray);
    }


    public Key getKey(String alias, char[] pwdArray) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        Key key = ks.getKey(alias, pwdArray);
        if (Objects.isNull(key)) {
            throw new KeyStoreException("Could not find key with alias:" + alias);
        }
        return key;
    }



}
