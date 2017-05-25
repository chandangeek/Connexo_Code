package com.energyict.mdc.engine.impl;

import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;
import org.junit.Test;

import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/05/2017 - 15:07
 */
public class CertificateWrapperExtractorImplTest {

    /**
     * The ECC algo.
     */
    private static final String ECC_ALGORITHM = "EC";

    /**
     * The Sun EC provider.
     */
    private static final String SUN_EC_PROVIDER = "SunEC";

    @Test
    public void testKeyManager() throws GeneralSecurityException, IOException {
        String alias = "alias";
        CertificateWrapperExtractorImpl certificateWrapperExtractor = new CertificateWrapperExtractorImpl();
        ClientCertificateWrapper certificateWrapper = mock(ClientCertificateWrapper.class);
        X509Certificate x509Certificate = mock(X509Certificate.class);
        when(certificateWrapper.getCertificate()).thenReturn(Optional.of(x509Certificate));
        when(certificateWrapper.getAlias()).thenReturn(alias);
        PrivateKeyWrapper privateKeyWrapper = mock(PrivateKeyWrapper.class);

        PrivateKey originalPrivateKey = generateECCKeyPair().getPrivate();

        when(privateKeyWrapper.getPrivateKey()).thenReturn(originalPrivateKey);
        when(certificateWrapper.getPrivateKeyWrapper()).thenReturn(privateKeyWrapper);

        //Business method
        KeyStore keyStore = certificateWrapperExtractor.getKeyStore(certificateWrapper);
        Key key = keyStore.getKey(alias, CertificateWrapperExtractorImpl.PARAMETERS);

        //Asserts
        assertNotNull(key);
        assertTrue(key instanceof PrivateKey);
        PrivateKey resultingPrivateKey = (PrivateKey) key;
        assertArrayEquals(resultingPrivateKey.getEncoded(), originalPrivateKey.getEncoded());

        //Business method
        Optional<X509KeyManager> keyManager = certificateWrapperExtractor.getKeyManager(certificateWrapper);

        //Asserts
        assertTrue(keyManager.isPresent());
        resultingPrivateKey = keyManager.get().getPrivateKey(alias);
        assertNotNull(resultingPrivateKey);
        assertArrayEquals(resultingPrivateKey.getEncoded(), originalPrivateKey.getEncoded());
        X509Certificate[] certificateChain = keyManager.get().getCertificateChain(alias);
        assertTrue(certificateChain.length == 1);
        assertTrue(certificateChain[0].equals(x509Certificate));
    }

    @Test
    public void testTrustManager() throws GeneralSecurityException, IOException {
        String alias2 = "alias2";
        CertificateWrapperExtractorImpl certificateWrapperExtractor = new CertificateWrapperExtractorImpl();

        TrustedCertificate certificateWrapper = mock(TrustedCertificate.class);

        TrustedCertificate trustedCertificateWrapper = mock(TrustedCertificate.class);
        X509Certificate x509Certificate2 = mock(X509Certificate.class);
        when(trustedCertificateWrapper.getCertificate()).thenReturn(Optional.of(x509Certificate2));
        when(trustedCertificateWrapper.getAlias()).thenReturn(alias2);

        TrustStore connexoTrustStore = mock(TrustStore.class);
        List<TrustedCertificate> trustedCertificates = new ArrayList<>();
        trustedCertificates.add(trustedCertificateWrapper);
        when(connexoTrustStore.getCertificates()).thenReturn(trustedCertificates);
        when(certificateWrapper.getTrustStore()).thenReturn(connexoTrustStore);

        //Business method
        KeyStore trustStore = certificateWrapperExtractor.getTrustStore(certificateWrapper);

        //Asserts
        Certificate resultingCertificate = trustStore.getCertificate(alias2);
        assertNotNull(resultingCertificate);
        assertEquals(resultingCertificate, x509Certificate2);
        assertEquals(trustStore.getCertificateAlias(x509Certificate2), alias2);

        //Business method
        Optional<X509TrustManager> trustManager = certificateWrapperExtractor.getTrustManager(certificateWrapper);

        //Asserts
        assertTrue(trustManager.isPresent());
        X509Certificate[] acceptedIssuers = trustManager.get().getAcceptedIssuers();
        assertEquals(acceptedIssuers.length, 1);
        assertEquals(acceptedIssuers[0], x509Certificate2);
    }

    /**
     * Generates a new ECC key pair.
     *
     * @return The key pair.
     * @throws GeneralSecurityException If an error occurs during the key pair generation.
     */
    private KeyPair generateECCKeyPair() throws GeneralSecurityException {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance(ECC_ALGORITHM, SUN_EC_PROVIDER);
        final ECGenParameterSpec parameterSpec = new ECGenParameterSpec("secp256r1");

        generator.initialize(parameterSpec);

        return generator.generateKeyPair();
    }
}