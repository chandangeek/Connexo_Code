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
import org.ejbca.core.protocol.ws.client.gen.AlreadyRevokedException_Exception;
import org.ejbca.core.protocol.ws.client.gen.ApprovalException_Exception;
import org.ejbca.core.protocol.ws.client.gen.AuthorizationDeniedException_Exception;
import org.ejbca.core.protocol.ws.client.gen.CADoesntExistsException_Exception;
import org.ejbca.core.protocol.ws.client.gen.CertificateResponse;
import org.ejbca.core.protocol.ws.client.gen.EjbcaException_Exception;
import org.ejbca.core.protocol.ws.client.gen.EjbcaWS;
import org.ejbca.core.protocol.ws.client.gen.NameAndId;
import org.ejbca.core.protocol.ws.client.gen.NotFoundException_Exception;
import org.ejbca.core.protocol.ws.client.gen.RevokeStatus;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.core.protocol.ws.client.gen.UserDoesntFullfillEndEntityProfile_Exception;
import org.ejbca.core.protocol.ws.client.gen.WaitingForApprovalException_Exception;
import org.osgi.framework.BundleContext;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaServiceImplTest {

    private static final String PKI_HOST_PROPERTY = "com.elster.jupiter.pki.host";
    private static final String PKI_PORT_PROPERTY = "com.elster.jupiter.pki.port";
    private static final String CXO_TRUSTSTORE_PROPERTY = "com.elster.jupiter.ca.truststore";
    private static final String SUPER_ADMIN_CLIENT_CERTIFICATE_ALIAS_PROPERTY = "com.elster.jupiter.ca.certificate";
    private static final String MANAGEMENT_CLIENT_ALIAS_PROPERTY = "com.elster.jupiter.ca.clientcertificate";
    private static final String CA_NAME_PROPERTY = "com.elster.jupiter.ca.name";
    private static final String CERT_PROFILE_NAME_PROPERTY = "com.elster.jupiter.ca.certprofilename";
    private static final String EE_PROFILE_NAME_PROPERTY = "com.elster.jupiter.ca.eeprofilename";

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
    @Mock
    private X509Certificate x509Certificate;
    @Mock
    private CertificateAuthoritySearchFilter certificateAuthoritySearchFilter;
    @Mock
    private RevokeStatus rs;

    @Before
    public void setUp() throws Exception {
        when(bundleContext.getProperty(PKI_HOST_PROPERTY)).thenReturn(PKI_HOST_PROPERTY_VALUE);
        when(bundleContext.getProperty(PKI_PORT_PROPERTY)).thenReturn(PKI_PORT_PROPERTY_VALUE);
        when(bundleContext.getProperty(CXO_TRUSTSTORE_PROPERTY)).thenReturn(CXO_TRUSTSTORE_PROPERTY_VALUE);
        when(bundleContext.getProperty(SUPER_ADMIN_CLIENT_CERTIFICATE_ALIAS_PROPERTY))
                .thenReturn(SUPER_ADMIN_CLIENT_CERTIFICATE_ALIAS_PROPERTY_VALUE);
        when(bundleContext.getProperty(MANAGEMENT_CLIENT_ALIAS_PROPERTY))
                .thenReturn(MANAGEMENT_CLIENT_ALIAS_PROPERTY_VALUE);
        when(bundleContext.getProperty(CA_NAME_PROPERTY)).thenReturn(CA_NAME_PROPERTY_VALUE);
        when(bundleContext.getProperty(CERT_PROFILE_NAME_PROPERTY)).thenReturn(CERT_PROFILE_NAME_PROPERTY_VALUE);
        when(bundleContext.getProperty(EE_PROFILE_NAME_PROPERTY)).thenReturn(EE_PROFILE_NAME_PROPERTY_VALUE);
        when(nlsService.getThesaurus(CaService.COMPONENTNAME, Layer.DOMAIN)).thenReturn(thesaurus);
        when(ejbcaWS.getEjbcaVersion()).thenReturn(PKI_VERSION);
        NameAndId nameAndId = new NameAndId("test", 1);
        List<NameAndId> nameAndIds = new ArrayList<>();
        nameAndIds.add(nameAndId);
        when(ejbcaWS.getAvailableCAs()).thenReturn(nameAndIds);
        when(ejbcaWS.getAuthorizedEndEntityProfiles()).thenReturn(nameAndIds);
        when(ejbcaWS.getAvailableCAsInProfile(nameAndId.getId())).thenReturn(nameAndIds);
        when(ejbcaWS.getAvailableCertificateProfiles(nameAndId.getId())).thenReturn(nameAndIds);
        when(certificateResponse.getCertificate()).thenReturn(x509Certificate);
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
    public void testActivation_configured() throws Exception{
        assertThat(caService.isConfigured()).isEqualTo(true);
    }

    @Test
    public void testActivation_unConfigured() throws Exception{
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
    public void testGetCaNameProperty() {
        verify(bundleContext, times(1)).getProperty(CA_NAME_PROPERTY);
    }

    @Test
    public void testGetCertProfileNameProperty() {
        verify(bundleContext, times(1)).getProperty(CERT_PROFILE_NAME_PROPERTY);
    }

    @Test
    public void testGetEndEntityProfileNameProperty() {
        verify(bundleContext, times(1)).getProperty(EE_PROFILE_NAME_PROPERTY);
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
}
