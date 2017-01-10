package com.energyict.mdc.upl.crypto;

import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Optional;

/**
 * Provides services that relate to the X.509 cryptography standard.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-10 (12:03)
 */
public interface X509Service {

    Optional<X509KeyManager> getKeyManager(KeyStore keyStore) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException;

    Optional<X509TrustManager> getTrustManager(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException;

}