package com.energyict.mdc.engine.impl;

import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.TrustedCertificate;
import com.energyict.mdc.protocol.pluggable.adapters.upl.CertificateWrapperAdapter;
import org.junit.Test;

import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Optional;

import static org.junit.Assert.*;
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
        when(certificateWrapper.hasPrivateKey()).thenReturn(true);
        PrivateKeyWrapper privateKeyWrapper = mock(PrivateKeyWrapper.class);

        PrivateKey originalPrivateKey = generateECCKeyPair().getPrivate();

        when(privateKeyWrapper.getPrivateKey()).thenReturn(Optional.of(originalPrivateKey));
        when(certificateWrapper.getPrivateKeyWrapper()).thenReturn(privateKeyWrapper);
        CertificateWrapperAdapter uplCertificateWrapper = new CertificateWrapperAdapter(certificateWrapper, Optional.empty());

        //Business method
        KeyStore keyStore = certificateWrapperExtractor.getKeyStore(uplCertificateWrapper);
        Key key = keyStore.getKey(alias, CertificateWrapperExtractorImpl.PARAMETERS);

        //Asserts
        assertNotNull(key);
        assertTrue(key instanceof PrivateKey);
        PrivateKey resultingPrivateKey = (PrivateKey) key;
        assertArrayEquals(resultingPrivateKey.getEncoded(), originalPrivateKey.getEncoded());

        //Business method
        Optional<X509KeyManager> keyManager = certificateWrapperExtractor.getKeyManager(uplCertificateWrapper);

        /*
        // build proper certificate chain when inserting a key intio key store:  CertificateChainBuilder.populateKeyStore

        //Asserts


        assertTrue(keyManager.isPresent());
        resultingPrivateKey = keyManager.get().getPrivateKey(alias);
        assertNotNull(resultingPrivateKey);
        assertArrayEquals(resultingPrivateKey.getEncoded(), originalPrivateKey.getEncoded());
        X509Certificate[] certificateChain = keyManager.get().getCertificateChain(alias);
        assertTrue(certificateChain.length == 1);
        assertTrue(certificateChain[0].equals(x509Certificate));
        */
    }

    @Test
    public void testTrustManager() throws GeneralSecurityException, IOException {
        String alias2 = "alias2";
        CertificateWrapperExtractorImpl certificateWrapperExtractor = new CertificateWrapperExtractorImpl();

        TrustedCertificate certificateWrapper = mock(TrustedCertificate.class);

        X509Certificate x509Certificate2 = mock(X509Certificate.class);
        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(null); // This initializes the empty key store
        keyStore.setCertificateEntry(alias2, x509Certificate2);

        CertificateWrapperAdapter uplCertificateWrapper = new CertificateWrapperAdapter(certificateWrapper, Optional.of(keyStore));

        //Business method
        Optional<KeyStore> optionalTrustStore = certificateWrapperExtractor.getTrustStore(uplCertificateWrapper);

        //Asserts
        assertTrue(optionalTrustStore.isPresent());
        KeyStore trustStore = optionalTrustStore.get();
        Certificate resultingCertificate = trustStore.getCertificate(alias2);
        assertNotNull(resultingCertificate);
        assertEquals(resultingCertificate, x509Certificate2);
        assertEquals(trustStore.getCertificateAlias(x509Certificate2), alias2);

        //Business method
        Optional<X509TrustManager> trustManager = certificateWrapperExtractor.getTrustManager(uplCertificateWrapper);

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