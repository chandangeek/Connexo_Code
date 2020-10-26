/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateAuthoritySearchFilter;
import com.elster.jupiter.pki.SecurityManagementService;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.ejbca.core.protocol.ws.AlreadyRevokedException_Exception;
import org.ejbca.core.protocol.ws.ApprovalException_Exception;
import org.ejbca.core.protocol.ws.AuthorizationDeniedException_Exception;
import org.ejbca.core.protocol.ws.CADoesntExistsException_Exception;
import org.ejbca.core.protocol.ws.CertificateResponse;
import org.ejbca.core.protocol.ws.EjbcaException_Exception;
import org.ejbca.core.protocol.ws.EjbcaWS;
import org.ejbca.core.protocol.ws.NameAndId;
import org.ejbca.core.protocol.ws.NotFoundException_Exception;
import org.ejbca.core.protocol.ws.RevokeStatus;
import org.ejbca.core.protocol.ws.UserDataVOWS;
import org.ejbca.core.protocol.ws.UserDoesntFullfillEndEntityProfile_Exception;
import org.ejbca.core.protocol.ws.WaitingForApprovalException_Exception;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import javax.naming.InvalidNameException;
import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaServiceImplTest {

    String demoCertificate = "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";


    private static final String PKI_HOST_PROPERTY = "com.elster.jupiter.pki.host";
    private static final String PKI_PORT_PROPERTY = "com.elster.jupiter.pki.port";
    private static final String CXO_TRUSTSTORE_PROPERTY = "com.elster.jupiter.ca.truststore";
    private static final String SUPER_ADMIN_CLIENT_CERTIFICATE_ALIAS_PROPERTY = "com.elster.jupiter.ca.certificate";
    private static final String MANAGEMENT_CLIENT_ALIAS_PROPERTY = "com.elster.jupiter.ca.clientcertificate";

    private static final String PKI_HOST_PROPERTY_VALUE = "127.0.0.1";
    private static final String PKI_PORT_PROPERTY_VALUE = "8443";
    private static final String CXO_TRUSTSTORE_PROPERTY_VALUE = "catruststore";
    private static final String SUPER_ADMIN_CLIENT_CERTIFICATE_ALIAS_PROPERTY_VALUE = "superadmin";
    private static final String MANAGEMENT_CLIENT_ALIAS_PROPERTY_VALUE = "managementca";
    private static final String CA_NAME_PROPERTY_VALUE = "TestCA";
    private static final String CERT_PROFILE_NAME_PROPERTY_VALUE = "SUBCA";
    private static final String EE_PROFILE_NAME_PROPERTY_VALUE = "TEST_EE";

    private static final Boolean NOT_DELTA = false;
    private static final Boolean DELTA = true;

    private static final Integer REVOCATION_REASON_CERTIFICATEHOLD = 6;

    private static final String PKI_VERSION = "6.5.0.5";

    private CaServiceImpl caService;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private SecurityManagementService securityManagementService;
    @Mock
    private NlsService nlsService;
    @Mock
    private EjbcaWS ejbcaWS;
    @Mock
    private volatile Thesaurus thesaurus;
    @Mock
    private CertificateResponse certificateResponse;

    private X509Certificate x509Certificate;

    @Mock
    private CertificateAuthoritySearchFilter certificateAuthoritySearchFilter;
    @Mock
    private RevokeStatus rs;

    @Before
    public void setUp() throws Exception {

        x509Certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(demoCertificate)));

        when(bundleContext.getProperty(PKI_HOST_PROPERTY)).thenReturn(PKI_HOST_PROPERTY_VALUE);
        when(bundleContext.getProperty(PKI_PORT_PROPERTY)).thenReturn(PKI_PORT_PROPERTY_VALUE);
        when(bundleContext.getProperty(CXO_TRUSTSTORE_PROPERTY)).thenReturn(CXO_TRUSTSTORE_PROPERTY_VALUE);
        when(bundleContext.getProperty(SUPER_ADMIN_CLIENT_CERTIFICATE_ALIAS_PROPERTY))
                .thenReturn(SUPER_ADMIN_CLIENT_CERTIFICATE_ALIAS_PROPERTY_VALUE);
        when(bundleContext.getProperty(MANAGEMENT_CLIENT_ALIAS_PROPERTY))
                .thenReturn(MANAGEMENT_CLIENT_ALIAS_PROPERTY_VALUE);
        when(nlsService.getThesaurus(CaService.COMPONENTNAME, Layer.DOMAIN)).thenReturn(thesaurus);
        when(ejbcaWS.getEjbcaVersion()).thenReturn(PKI_VERSION);
        NameAndId nameAndId = new NameAndId();
        nameAndId.setId(1);
        nameAndId.setName("test");
        List<NameAndId> nameAndIds = new ArrayList<>();
        nameAndIds.add(nameAndId);
        when(ejbcaWS.getAvailableCAs()).thenReturn(nameAndIds);
        when(ejbcaWS.getAuthorizedEndEntityProfiles()).thenReturn(nameAndIds);
        when(ejbcaWS.getAvailableCAsInProfile(nameAndId.getId())).thenReturn(nameAndIds);
        when(ejbcaWS.getAvailableCertificateProfiles(nameAndId.getId())).thenReturn(nameAndIds);

        when(certificateResponse.getData()).thenReturn(Base64.getEncoder().encode(x509Certificate.getEncoded()));
        when(certificateResponse.getResponseType()).thenReturn("CERTIFICATE");
        when(ejbcaWS
                .certificateRequest(any(UserDataVOWS.class), any(String.class), any(Integer.class), any(String.class), any(String.class)))
                .thenReturn(certificateResponse);
        when(certificateAuthoritySearchFilter.getIssuerDN()).thenReturn("testIssuerDN");
        when(certificateAuthoritySearchFilter.getSerialNumber()).thenReturn(new BigInteger("1"));
        when(rs.getReason()).thenReturn(REVOCATION_REASON_CERTIFICATEHOLD);
        when(ejbcaWS.checkRevokationStatus(any(String.class), any(String.class))).thenReturn(rs);
        caService = new CaServiceImpl(bundleContext, securityManagementService, nlsService);
    }

    @Test
    public void testActivation_configured() throws Exception {
        assertThat(caService.isConfigured()).isEqualTo(true);
    }

    @Test
    public void testActivation_unConfigured() throws Exception {
        Mockito.reset(bundleContext);
        caService = new CaServiceImpl(bundleContext, securityManagementService, nlsService);

        assertThat(caService.isConfigured()).isEqualTo(false);
    }

    @Test
    public void testGetPkiHostProperty() {
        verify(bundleContext, times(1)).getProperty(PKI_HOST_PROPERTY);
    }

    @Test
    public void testGetPkiPortProperty() {
        verify(bundleContext, times(1)).getProperty(PKI_PORT_PROPERTY);
    }

    @Test
    public void testGetCxoTruststoreProperty() {
        verify(bundleContext, times(1)).getProperty(CXO_TRUSTSTORE_PROPERTY);
    }

    @Test
    public void testGetSuperAdminAliasProperty() {
        verify(bundleContext, times(1)).getProperty(SUPER_ADMIN_CLIENT_CERTIFICATE_ALIAS_PROPERTY);
    }

    @Test
    public void testGetManagementClientAliasProperty() {
        verify(bundleContext, times(1)).getProperty(MANAGEMENT_CLIENT_ALIAS_PROPERTY);
    }

    @Test
    public void testGetPkiCaNames() throws AuthorizationDeniedException_Exception, EjbcaException_Exception {
        caService.init(ejbcaWS);
        caService.getPkiCaNames();
        verify(ejbcaWS, times(1)).getAvailableCAs();
    }

    @Test
    public void testGetPkiInfo() throws AuthorizationDeniedException_Exception, EjbcaException_Exception {
        caService.init(ejbcaWS);
        caService.getPkiInfo();
        verify(ejbcaWS, times(1)).getEjbcaVersion();
        verify(ejbcaWS, times(1)).getAuthorizedEndEntityProfiles();
        verify(ejbcaWS, times(1)).getAvailableCAsInProfile(1);
        verify(ejbcaWS, times(1)).getAvailableCertificateProfiles(1);
    }

    @Test
    public void testSignCsr()
            throws AuthorizationDeniedException_Exception, EjbcaException_Exception, OperatorCreationException, ApprovalException_Exception,
            UserDoesntFullfillEndEntityProfile_Exception, NotFoundException_Exception, WaitingForApprovalException_Exception,
            NoSuchAlgorithmException {
        caService.init(ejbcaWS);
        PKCS10CertificationRequest csr = generateCsr();
        caService.signCsr(csr, Optional.empty());
        verify(ejbcaWS, times(1))
                .certificateRequest(any(UserDataVOWS.class), any(String.class), any(Integer.class), any(String.class), any(String.class));
    }

    @Test
    public void testGetLatestCRL() throws EjbcaException_Exception, CADoesntExistsException_Exception {
        caService.init(ejbcaWS);
        caService.getLatestCRL(CA_NAME_PROPERTY_VALUE);
        verify(ejbcaWS, times(1)).getLatestCRL(CA_NAME_PROPERTY_VALUE, NOT_DELTA);
    }

    @Test
    public void testGetLatestDeltaCRL() throws EjbcaException_Exception, CADoesntExistsException_Exception {
        caService.init(ejbcaWS);
        caService.getLatestDeltaCRL(CA_NAME_PROPERTY_VALUE);
        verify(ejbcaWS, times(1)).getLatestCRL(CA_NAME_PROPERTY_VALUE, DELTA);
    }

    @Test
    public void testRevokeCertificate()
            throws EjbcaException_Exception, CADoesntExistsException_Exception, AlreadyRevokedException_Exception,
            WaitingForApprovalException_Exception, NotFoundException_Exception, ApprovalException_Exception,
            AuthorizationDeniedException_Exception {
        caService.init(ejbcaWS);
        caService.revokeCertificate(certificateAuthoritySearchFilter, REVOCATION_REASON_CERTIFICATEHOLD);
        verify(ejbcaWS, times(1)).revokeCert("testIssuerDN", "1", REVOCATION_REASON_CERTIFICATEHOLD);
    }

    @Test
    public void testCheckRevocationStatus()
            throws EjbcaException_Exception, CADoesntExistsException_Exception, AuthorizationDeniedException_Exception {
        caService.init(ejbcaWS);
        com.elster.jupiter.pki.RevokeStatus revokeStatus = caService.checkRevocationStatus(certificateAuthoritySearchFilter);
        verify(ejbcaWS, times(1)).checkRevokationStatus("testIssuerDN", "1");
        assertThat(revokeStatus).isEqualTo(com.elster.jupiter.pki.RevokeStatus.REVOCATION_REASON_CERTIFICATEHOLD);
    }

    private PKCS10CertificationRequest generateCsr() throws NoSuchAlgorithmException, OperatorCreationException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048, new SecureRandom());
        KeyPair pair = keyGen.genKeyPair();
        PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
                new X500Principal("CN=Requested Test Certificate"), pair.getPublic());
        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
        ContentSigner signer = csBuilder.build(pair.getPrivate());
        return p10Builder.build(signer);
    }

    @Test
    public void testExtractSubjectDN() throws InvalidNameException {
        String originalSubjectDN = "CN=4B464D1010006D3B,OU=KFM,O=SZ Kaifa Technology Co.\\,Ltd.,C=CN";

        String subjectDN = caService.extractSubjectDN(originalSubjectDN,"CN");
        assertEquals("CN=4B464D1010006D3B", subjectDN);

        subjectDN = caService.extractSubjectDN(originalSubjectDN,"CN OU ");
        assertEquals("CN=4B464D1010006D3B,OU=KFM", subjectDN);

        subjectDN = caService.extractSubjectDN(originalSubjectDN,"CN,OU,C ");
        assertEquals("CN=4B464D1010006D3B,OU=KFM,C=CN", subjectDN);


        subjectDN = caService.extractSubjectDN(originalSubjectDN,"");
        assertEquals(originalSubjectDN, subjectDN);

        subjectDN = caService.extractSubjectDN(originalSubjectDN,"   ");
        assertEquals(originalSubjectDN, subjectDN);


        subjectDN = caService.extractSubjectDN(originalSubjectDN,null);
        assertEquals(originalSubjectDN, subjectDN);
    }
}
