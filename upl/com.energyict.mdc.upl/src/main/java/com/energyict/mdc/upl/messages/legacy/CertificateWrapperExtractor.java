/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl.messages.legacy;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.upl.security.CertificateWrapper;

import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.*;
import java.security.cert.CRL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;

@ProviderType
public interface CertificateWrapperExtractor {

    Optional<X509Certificate> getCertificate(CertificateWrapper certificateWrapper);

    String getAlias(CertificateWrapper certificateWrapper);

    /**
     * Return a java KeyStore representing the TrustStore for the given server CertificateWrapper
     */
    Optional<KeyStore> getTrustStore(CertificateWrapper serverCertificateWrapper) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException;

    /**
     * Return an X509 TrustManager representing the TrustStore of the given server CertificateWrapper
     */
    Optional<X509TrustManager> getTrustManager(CertificateWrapper serverCertificateWrapper) throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException;

    /**
     * Return a java KeyStore containing the given client certificate and its private key
     */
    KeyStore getKeyStore(CertificateWrapper clientCertificateWrapper) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, InvalidKeyException;

    /**
     * Return an X509 KeyManager representing the KeyStore that contains the given client certificate and its private key
     */
    Optional<X509KeyManager> getKeyManager(CertificateWrapper clientCertificateWrapper) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, InvalidKeyException, IOException, UnrecoverableKeyException;

    Optional<X509KeyManager> getHsmKeyManager(CertificateWrapper clientCertificateWrapper) throws Exception;

    PrivateKey getPrivateKey(CertificateWrapper clientCertificateWrapper) throws InvalidKeyException;

    Optional<CRL> getCRL(CertificateWrapper trustedCertificateWrapper);

    X509Certificate[] getCertificateChain(CertificateWrapper serverCertificateWrapper);

    String getRootCAAlias(CertificateWrapper serverCertificateWrapper);

    //TODO also support ClientCertificateWrapper extraction (including its certificate and private key)

}