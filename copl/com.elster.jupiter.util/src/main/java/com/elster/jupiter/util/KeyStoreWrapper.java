/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */


package com.elster.jupiter.util;


import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeyStoreWrapper {
    ConcurrentHashMap<Pair<String, char[]>, Key> keys = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Certificate> certificates = new ConcurrentHashMap<>();
    ConcurrentHashMap<Pair<String, KeyStore.ProtectionParameter>, KeyStore.Entry> entries = new ConcurrentHashMap<>();
    private KeyStore keyStore;
    private boolean mapsInitialized = false;

    public KeyStoreWrapper(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    private void cleanMaps() {
        keys.clear();
        certificates.clear();
        entries.clear();
        mapsInitialized = false;
    }

    private void loadMaps(char[] password) throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException {
        Enumeration<String> enumeration = keyStore.aliases();
        while (enumeration.hasMoreElements()) {
            String alias = enumeration.nextElement();
            if (keyStore.isCertificateEntry(alias)) {
                certificates.put(alias, keyStore.getCertificate(alias));
                Pair<String, KeyStore.ProtectionParameter> pair = Pair.of(alias, null);
                entries.put(pair, keyStore.getEntry(alias, pair.getLast()));
            } else if (keyStore.isKeyEntry(alias)) {
                keys.put(Pair.of(alias, password), keyStore.getKey(alias, password));
                Pair<String, KeyStore.ProtectionParameter> pair = Pair.of(alias, new KeyStore.PasswordProtection(password));
                entries.put(pair, keyStore.getEntry(alias, pair.getLast()));
            }
        }
    }

    public final void load(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        keyStore.load(stream, password);
        try {
            cleanMaps();
            loadMaps(password);
            mapsInitialized = true;
        } catch (KeyStoreException | UnrecoverableEntryException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception loading keystore in map", ex);
        }
    }

    public final Certificate getCertificate(String alias) throws KeyStoreException {
        if (mapsInitialized) {
            return certificates.get(alias);
        }
        return keyStore.getCertificate(alias);
    }

    public final KeyStore.Entry getEntry(String alias, KeyStore.ProtectionParameter protParam)
            throws NoSuchAlgorithmException, UnrecoverableEntryException,
            KeyStoreException {
        if (mapsInitialized) {
            return entries.get(new javafx.util.Pair<>(alias, protParam));
        }
        return keyStore.getEntry(alias, protParam);
    }

    public final Key getKey(String alias, char[] password) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        if (mapsInitialized) {
            return keys.get(Pair.of(alias, password));
        }
        return keyStore.getKey(alias, password);
    }

    public final void setCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        keyStore.setCertificateEntry(alias, cert);
    }

    public final void setEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException {
        keyStore.setEntry(alias, entry, protParam);
    }

    public final void setKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        keyStore.setKeyEntry(alias, key, chain);
    }

    public final void setKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        keyStore.setKeyEntry(alias, key, password, chain);
    }
}
