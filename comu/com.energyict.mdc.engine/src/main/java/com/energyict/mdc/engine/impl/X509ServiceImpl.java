package com.energyict.mdc.engine.impl;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.crypto.X509Service;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link X509Service} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-20 (08:51)
 */
@Component(name = "com.energyict.mdc.upl.crypto.x509", service = {X509Service.class}, immediate = true)
@SuppressWarnings("unused")
public class X509ServiceImpl implements X509Service {

    private static final char[] DLMS_KEYSTORE_PARAMETERS = DLMSKeyStoreUserFile.PARAMETERS;

    @Activate
    public void activate() {
        Services.x509Service(this);
    }

    @Deactivate
    public void deactivate() {
        Services.x509Service(null);
    }

    @Override
    public Optional<X509KeyManager> getKeyManager(KeyStore keyStore) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmfactory.init(keyStore, DLMS_KEYSTORE_PARAMETERS);
        return Stream
                .of(kmfactory.getKeyManagers())
                .filter(keyManager -> keyManager instanceof X509KeyManager)
                .map(X509KeyManager.class::cast)
                .findAny();
    }

    @Override
    public Optional<X509TrustManager> getTrustManager(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return Stream
                .of(trustManagerFactory.getTrustManagers())
                .filter(keyManager -> keyManager instanceof X509TrustManager)
                .map(X509TrustManager.class::cast)
                .findAny();
    }

}