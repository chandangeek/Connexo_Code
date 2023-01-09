/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.elster.jupiter.pki.CertificateChainBuilder;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.pki.TrustedCertificate;
import com.energyict.mdc.protocol.pluggable.adapters.upl.CertificateWrapperAdapter;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.security.CertificateWrapper;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
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
import java.security.cert.CRL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
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

    private static final String KEY_STORE = "PKCS12";

    private volatile com.elster.jupiter.hsm.HsmProtocolService hsmProtocolService;

    // For OSGi purposes
    public CertificateWrapperExtractorImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public CertificateWrapperExtractorImpl(com.elster.jupiter.hsm.HsmProtocolService hsmProtocolService) {
        this.hsmProtocolService = hsmProtocolService;
    }

    @Reference
    public void setHsmProtocolService(com.elster.jupiter.hsm.HsmProtocolService hsmProtocolService) {
        this.hsmProtocolService = hsmProtocolService;
    }

    @Activate
    public void activate() {
        Services.certificateWrapperExtractor(this);
    }

    @Deactivate
    public void deactivate() {
        Services.certificateWrapperExtractor(null);
    }

    private com.elster.jupiter.pki.CertificateWrapper toConnexoCertificateWrapper(CertificateWrapper certificateWrapper) {
        return ((CertificateWrapperAdapter) certificateWrapper).getCertificateWrapper();
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
    public Optional<KeyStore> getTrustStore(CertificateWrapper serverCertificateWrapper) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        return ((CertificateWrapperAdapter) serverCertificateWrapper).getTrustStore();
    }

    @Override
    public Optional<X509TrustManager> getTrustManager(CertificateWrapper serverCertificateWrapper) throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException {
        if (serverCertificateWrapper == null) {
            return Optional.empty();
        }
        Optional<KeyStore> trustStore = getTrustStore(serverCertificateWrapper);
        if (trustStore.isPresent()) {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore.get());
            return Stream
                    .of(trustManagerFactory.getTrustManagers())
                    .filter(keyManager -> keyManager instanceof X509TrustManager)
                    .map(X509TrustManager.class::cast)
                    .findAny();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public KeyStore getKeyStore(CertificateWrapper certificateWrapper) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, InvalidKeyException {
        com.elster.jupiter.pki.CertificateWrapper connexoCertificateWrapper = this.toConnexoCertificateWrapper(certificateWrapper);

        if (connexoCertificateWrapper instanceof RequestableCertificateWrapper) {
            RequestableCertificateWrapper requestableCertificateWrapper = (RequestableCertificateWrapper) connexoCertificateWrapper;
            if (requestableCertificateWrapper.getCertificate().isPresent()) {
                KeyStore keyStore = KeyStore.getInstance(KEY_STORE);
                keyStore.load(null); // This initializes the empty key store
                LinkedList<com.elster.jupiter.pki.CertificateWrapper> certificateChain = CertificateChainBuilder.getCertificateChain(requestableCertificateWrapper);
                if (connexoCertificateWrapper instanceof ClientCertificateWrapper) {
                    LinkedList<com.elster.jupiter.pki.CertificateWrapper> clientCertificateWrappers = certificateChain.stream().filter(com.elster.jupiter.pki.CertificateWrapper::hasPrivateKey).collect(Collectors.toCollection(LinkedList::new));
                    //well this cast is horrible but this is the model we have ...
                    CertificateChainBuilder.populateKeyStore((LinkedList<ClientCertificateWrapper>) (LinkedList<?>) clientCertificateWrappers, keyStore, PARAMETERS);
                } else {

                    //TODO: log something? in this case we have a certificate based on a CSR for which we don't have the private key (csr for HSM private key, or for a beacon device private key....)
                }
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
    public Optional<X509KeyManager> getHsmKeyManager(CertificateWrapper clientCertificateWrapper) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, InvalidKeyException, IOException, UnrecoverableKeyException {
        com.elster.jupiter.pki.CertificateWrapper connexoCertificateWrapper = toConnexoCertificateWrapper(clientCertificateWrapper);
        KeyStore keyStore = getKeyStore(clientCertificateWrapper);
        return Optional.of(hsmProtocolService.getKeyManager(keyStore, PARAMETERS, connexoCertificateWrapper.getAlias(), getCertificateChain(clientCertificateWrapper)));
    }

    @Override
    public PrivateKey getPrivateKey(CertificateWrapper clientCertificateWrapper) throws InvalidKeyException {
        com.elster.jupiter.pki.CertificateWrapper connexoCertificateWrapper = toConnexoCertificateWrapper(clientCertificateWrapper);
        if (connexoCertificateWrapper instanceof ClientCertificateWrapper) {
            return ((ClientCertificateWrapper) connexoCertificateWrapper).getPrivateKeyWrapper().getPrivateKey().get();
        } else {
            throw new IllegalArgumentException("The given CertificateWrapper (alias '" + connexoCertificateWrapper.getAlias() + "') must be of type ClientCertificateWrapper");
        }
    }

    @Override
    public Optional<CRL> getCRL(CertificateWrapper trustedCertificateWrapper) {
        com.elster.jupiter.pki.CertificateWrapper connexoCertificateWrapper = toConnexoCertificateWrapper(trustedCertificateWrapper);
        if (connexoCertificateWrapper instanceof TrustedCertificate) {
            return ((TrustedCertificate) connexoCertificateWrapper).getCRL();
        } else {
            throw new IllegalArgumentException("The given CertificateWrapper (alias '" + connexoCertificateWrapper.getAlias() + "') must be of type TrustedCertificate");
        }
    }

    @Override
    public X509Certificate[] getCertificateChain(CertificateWrapper serverCertificateWrapper) {
        LinkedList<com.elster.jupiter.pki.CertificateWrapper> certificateChain = CertificateChainBuilder.getCertificateChain(((CertificateWrapperAdapter) serverCertificateWrapper).getCertificateWrapper());
        return certificateChain
                .stream()
                .map(com.elster.jupiter.pki.CertificateWrapper::getCertificate)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .skip(1)//skip root CA....should not be included
                .toArray(X509Certificate[]::new);
    }

    @Override
    public String getRootCAAlias(CertificateWrapper serverCertificateWrapper) {
        com.elster.jupiter.pki.CertificateWrapper clientCertificateWrapper = ((CertificateWrapperAdapter) serverCertificateWrapper).getCertificateWrapper();
        while (!clientCertificateWrapper.getSubject().isEmpty() && !clientCertificateWrapper.getSubject().equalsIgnoreCase(clientCertificateWrapper.getIssuer())) {
            clientCertificateWrapper = clientCertificateWrapper.getParent();
        }
        return clientCertificateWrapper.getAlias();

    }
}