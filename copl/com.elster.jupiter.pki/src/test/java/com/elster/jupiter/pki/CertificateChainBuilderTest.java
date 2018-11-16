package com.elster.jupiter.pki;


import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CertificateChainBuilderTest {


    @Mock
    public CertificateWrapper clientCertificateWrapper1;
    @Mock
    public CertificateWrapper clientCertificateWrapper2;
    @Mock
    public CertificateWrapper clientCertificateWrapper3;


    @Test
    public void testGetChainNoParentCertificate() {
        Mockito.when(clientCertificateWrapper1.getParent()).thenReturn(null);
        LinkedList<CertificateWrapper> certificateChain = CertificateChainBuilder.getCertificateChain(clientCertificateWrapper1);
        Assert.assertEquals(1, certificateChain.size());
        Assert.assertEquals(clientCertificateWrapper1, certificateChain.getFirst());
    }

    @Test
    public void testGetChainWithParentCertificates() {
        Mockito.when(clientCertificateWrapper3.getParent()).thenReturn(clientCertificateWrapper2);
        Mockito.when(clientCertificateWrapper2.getParent()).thenReturn(clientCertificateWrapper1);
        LinkedList<CertificateWrapper> certificateChain = CertificateChainBuilder.getCertificateChain(clientCertificateWrapper3);
        Assert.assertEquals(3, certificateChain.size());
        Assert.assertEquals(clientCertificateWrapper1, certificateChain.pollFirst());
        Assert.assertEquals(clientCertificateWrapper2, certificateChain.pollFirst());
        Assert.assertEquals(clientCertificateWrapper3, certificateChain.pollFirst());
    }

    // This test does not work while KS does allot of checking and mocking it does not work with our mock/test frameworks..... You can uncomment it and debug ...
    @Test
    @Ignore
    public void testPopulateKeyStoreSingleCertificate() throws KeyStoreException, InvalidKeyException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null);

        LinkedList<ClientCertificateWrapper> list = new LinkedList<>();
        mockAndAddCertificate(list, Mockito.mock(ClientCertificateWrapper.class), Mockito.mock(X509Certificate.class));
        mockAndAddCertificate(list, Mockito.mock(ClientCertificateWrapper.class), Mockito.mock(X509Certificate.class));

        CertificateChainBuilder.populateKeyStore(list, ks, "password".toCharArray());

    }

    private void mockAndAddCertificate(LinkedList<ClientCertificateWrapper> list, ClientCertificateWrapper certificateWrapperMock, X509Certificate certMock) throws InvalidKeyException {
        PrivateKeyWrapper mockedPrvKWrapper = Mockito.mock(PrivateKeyWrapper.class);
        PrivateKey mockedPrivateKey = Mockito.mock(PrivateKey.class);
        Mockito.when(mockedPrvKWrapper.getPrivateKey()).thenReturn(Optional.of(mockedPrivateKey));
        Mockito.when(certificateWrapperMock.getPrivateKeyWrapper()).thenReturn(mockedPrvKWrapper);
        Mockito.when(certificateWrapperMock.getCertificate()).thenReturn(Optional.of(certMock));
        list.add(certificateWrapperMock);
    }

}
