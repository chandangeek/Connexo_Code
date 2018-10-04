package com.elster.jupiter.pki;


import java.util.LinkedList;

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

    @Test
    @Ignore
    public void testPopulateKeyStoreSingleCertificate() {
        // this cannot be implemented while Keystore setKeyEntry method is final and cannot be mocked with current mockito version and changing version is unfeasible since on 10.4 tests are broken aanyway... may God help us all!
    }

}
