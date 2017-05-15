/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.TrustedCertificate;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.security.CertificateWrapper;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link CertificateWrapperExtractor} interface
 *
 * @author stijn
 * @since 09.05.17 - 17:03
 */
@Component(name = "com.energyict.mdc.upl.messages.legacy.certificate.wrapper.extractor", service = {CertificateWrapperExtractor.class}, immediate = true)
public class CertificateWrapperExtractorImpl implements CertificateWrapperExtractor {

    static final char[] PARAMETERS = new char[]{'i', '#', '?', 'r', 'P', '1', '_', 'L', 'v', '/', 'T', '@', '>', 'k', 'h', '*'};

    private static final String TRUST_STORE = "JCEKS";
    private static final String KEY_STORE = "PKCS12";

    @Activate
    public void activate() {
        Services.certificateWrapperExtractor(this);
    }

    @Deactivate
    public void deactivate() {
        Services.certificateWrapperExtractor(null);
    }

    private com.elster.jupiter.pki.CertificateWrapper toConnexoCertificateWrapper(CertificateWrapper certificateWrapper) {
        return ((com.elster.jupiter.pki.CertificateWrapper) certificateWrapper);    //Downcast to Connexo interface
    }

    @Override
    public Optional<X509Certificate> getCertificate(CertificateWrapper certificateWrapper) {
        return this.toConnexoCertificateWrapper(certificateWrapper).getCertificate();
    }

    @Override
    public String getAlias(CertificateWrapper certificateWrapper) {
        return this.toConnexoCertificateWrapper(certificateWrapper).getAlias();
    }

    @Override
    public KeyStore getTrustStore(CertificateWrapper serverCertificateWrapper) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        com.elster.jupiter.pki.CertificateWrapper connexoCertificateWrapper = this.toConnexoCertificateWrapper(serverCertificateWrapper);

        if (connexoCertificateWrapper instanceof TrustedCertificate) {
            KeyStore trustStore = KeyStore.getInstance(TRUST_STORE);
            trustStore.load(null); // This initializes the empty key store
            for (TrustedCertificate trustedCertificate : ((TrustedCertificate) connexoCertificateWrapper).getTrustStore().getCertificates()) {
                if (trustedCertificate.getCertificate().isPresent()) {
                    trustStore.setCertificateEntry(trustedCertificate.getAlias(), trustedCertificate.getCertificate().get());
                }
            }
            return trustStore;
        } else {
            throw new IllegalArgumentException("The given CertificateWrapper (alias '" + connexoCertificateWrapper.getAlias() + "') must be of type TrustedCertificate");
        }
    }

    @Override
    public Optional<X509TrustManager> getTrustManager(CertificateWrapper serverCertificateWrapper) throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException {
        KeyStore trustStore = getTrustStore(serverCertificateWrapper);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return Stream
                .of(trustManagerFactory.getTrustManagers())
                .filter(keyManager -> keyManager instanceof X509TrustManager)
                .map(X509TrustManager.class::cast)
                .findAny();
    }

    @Override
    public KeyStore getKeyStore(CertificateWrapper certificateWrapper) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, InvalidKeyException {
        com.elster.jupiter.pki.CertificateWrapper connexoCertificateWrapper = this.toConnexoCertificateWrapper(certificateWrapper);

        if (connexoCertificateWrapper instanceof ClientCertificateWrapper) {
            ClientCertificateWrapper clientCertificateWrapper = (ClientCertificateWrapper) connexoCertificateWrapper;
            if (clientCertificateWrapper.getCertificate().isPresent()) {
                KeyStore keyStore = KeyStore.getInstance(KEY_STORE);
                keyStore.load(null); // This initializes the empty key store
                keyStore.setKeyEntry(
                        clientCertificateWrapper.getAlias(),
                        clientCertificateWrapper.getPrivateKeyWrapper().getPrivateKey(),
                        PARAMETERS,
                        new Certificate[]{clientCertificateWrapper.getCertificate().get()}  //The chain for the client PrivateKey is just 1 certificate
                );
                return keyStore;
            } else {
                throw new IllegalArgumentException("The given client CertificateWrapper (alias '" + connexoCertificateWrapper.getAlias() + "') must contain a private key and a client certificate");
            }
        } else {
            throw new IllegalArgumentException("The given CertificateWrapper (alias '" + connexoCertificateWrapper.getAlias() + "') must be of type ClientCertificateWrapper");
        }
    }

    @Override
    public Optional<X509KeyManager> getKeyManager(CertificateWrapper clientCertificateWrapper) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, InvalidKeyException, IOException, UnrecoverableKeyException {
        KeyStore keyStore = getKeyStore(clientCertificateWrapper);
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, PARAMETERS);
        return Stream
                .of(keyManagerFactory.getKeyManagers())
                .filter(keyManager -> keyManager instanceof X509KeyManager)
                .map(X509KeyManager.class::cast)
                .findAny();
    }

    @Override
    public PrivateKey getPrivateKey(CertificateWrapper clientCertificateWrapper) throws InvalidKeyException {
        com.elster.jupiter.pki.CertificateWrapper connexoCertificateWrapper = toConnexoCertificateWrapper(clientCertificateWrapper);
        if (connexoCertificateWrapper instanceof ClientCertificateWrapper) {
            return ((ClientCertificateWrapper) connexoCertificateWrapper).getPrivateKeyWrapper().getPrivateKey();
        } else {
            throw new IllegalArgumentException("The given CertificateWrapper (alias '" + connexoCertificateWrapper.getAlias() + "') must be of type ClientCertificateWrapper");
        }
    }
}