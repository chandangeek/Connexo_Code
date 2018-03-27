package com.elster.jupiter.users.impl;

import java.security.KeyStore;

public class SslSecurityProperties {
    private KeyStore trustedStore;
    private KeyStore keyStore;
    private char[] keyStorePassword;

    public KeyStore getTrustedStore() {
        return trustedStore;
    }

    public void setTrustedStore(KeyStore trustedStore) {
        this.trustedStore = trustedStore;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public char[] getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(char[] keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }
}
