/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateAuthorityRuntimeException;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.EnumLookupValueException;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.PropertyValueRequiredException;
import com.elster.jupiter.pki.RevokeStatus;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.cesecore.util.Base64;
import org.cesecore.util.CryptoProviderTools;
import org.ejbca.core.protocol.ws.client.gen.AlreadyRevokedException_Exception;
import org.ejbca.core.protocol.ws.client.gen.ApprovalException_Exception;
import org.ejbca.core.protocol.ws.client.gen.AuthorizationDeniedException_Exception;
import org.ejbca.core.protocol.ws.client.gen.CADoesntExistsException_Exception;
import org.ejbca.core.protocol.ws.client.gen.CertificateResponse;
import org.ejbca.core.protocol.ws.client.gen.EjbcaException_Exception;
import org.ejbca.core.protocol.ws.client.gen.EjbcaWS;
import org.ejbca.core.protocol.ws.client.gen.EjbcaWSService;
import org.ejbca.core.protocol.ws.client.gen.NameAndId;
import org.ejbca.core.protocol.ws.client.gen.NotFoundException_Exception;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.core.protocol.ws.client.gen.UserDoesntFullfillEndEntityProfile_Exception;
import org.ejbca.core.protocol.ws.client.gen.WaitingForApprovalException_Exception;
import org.ejbca.core.protocol.ws.common.CertificateHelper;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "CaService", service = {CaService.class}, property = "name=" + CaService.COMPONENTNAME, immediate = true)
public class CaServiceImpl implements CaService {
    private static final Integer SN_HEX = 16;
    private static final String PROTOCOL = "TLS";
    // felix config properties
    private static final String PKI_HOST_PROPERTY = "com.elster.jupiter.pki.host";
    private static final String PKI_PORT_PROPERTY = "com.elster.jupiter.pki.port";
    private static final String PKI_CXO_TRUSTSTORE_PROPERTY = "com.elster.jupiter.ca.truststore";
    private static final String PKI_SUPER_ADMIN_CLIENT_ALIAS_PROPERTY = "com.elster.jupiter.ca.certificate";
    private static final String PKI_CA_NAME_PROPERTY = "com.elster.jupiter.ca.name";
    private static final String PKI_CERTIFICATE_PROFILE_NAME_PROPERTY = "com.elster.jupiter.ca.certprofilename";
    private static final String PKI_END_ENTITY_PROFILE_NAME_PROPERTY = "com.elster.jupiter.ca.eeprofilename";

    private static final String MANAGEMENT_CA_ALIAS = "managementca";

    private String pkiHost;
    private Integer pkiPort;
    private String pkiTrustStore;
    private String pkiSuperAdminClientAlias;
    private String pkiCaName;
    private String pkiCertificateProfileName;
    private String pkiEndEntityProfileName;

    private volatile SecurityManagementService securityManagementService;
    private volatile Thesaurus thesaurus;

    private EjbcaWS ejbcaWS;

    @SuppressWarnings("unused") // OSGI
    public CaServiceImpl() {

    }

    @Inject
    public CaServiceImpl(BundleContext bundleContext, SecurityManagementService securityManagementService, NlsService nlsService) {
        this();
        setNlsService(nlsService);
        setSecurityManagementService(securityManagementService);
        activate(bundleContext);
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        ejbcaWS = null;
        Security.addProvider(new BouncyCastleProvider());
        getPkiProperties(bundleContext);
    }

    @Deactivate
    public void deactivate() {
        pkiHost = null;
        pkiPort = null;
        pkiTrustStore = null;
        pkiSuperAdminClientAlias = null;
        pkiCaName = null;
        pkiCertificateProfileName = null;
        pkiEndEntityProfileName = null;
        ejbcaWS = null;
    }

    private void getPkiProperties(BundleContext bundleContext) {
        pkiHost = getPkiProperty(bundleContext, PKI_HOST_PROPERTY)
                .orElseThrow(() -> new PropertyValueRequiredException(thesaurus, MessageSeeds.PROPERTY_VALUE_REQUIRED, PKI_HOST_PROPERTY));
        String port = getPkiProperty(bundleContext, PKI_PORT_PROPERTY)
                .orElseThrow(() -> new PropertyValueRequiredException(thesaurus, MessageSeeds.PROPERTY_VALUE_REQUIRED, PKI_PORT_PROPERTY));
        pkiPort = Integer.parseInt(port);

        pkiTrustStore = getPkiProperty(bundleContext, PKI_CXO_TRUSTSTORE_PROPERTY).orElseThrow(
                () -> new PropertyValueRequiredException(thesaurus, MessageSeeds.PROPERTY_VALUE_REQUIRED, PKI_CXO_TRUSTSTORE_PROPERTY));
        pkiSuperAdminClientAlias = getPkiProperty(bundleContext, PKI_SUPER_ADMIN_CLIENT_ALIAS_PROPERTY).orElseThrow(
                () -> new PropertyValueRequiredException(thesaurus, MessageSeeds.PROPERTY_VALUE_REQUIRED,
                        PKI_SUPER_ADMIN_CLIENT_ALIAS_PROPERTY));
        pkiCaName = getPkiProperty(bundleContext, PKI_CA_NAME_PROPERTY)
                .orElseThrow(() -> new PropertyValueRequiredException(thesaurus, MessageSeeds.PROPERTY_VALUE_REQUIRED, PKI_CA_NAME_PROPERTY));
        pkiCertificateProfileName = getPkiProperty(bundleContext, PKI_CERTIFICATE_PROFILE_NAME_PROPERTY).orElseThrow(
                () -> new PropertyValueRequiredException(thesaurus, MessageSeeds.PROPERTY_VALUE_REQUIRED,
                        PKI_CERTIFICATE_PROFILE_NAME_PROPERTY));
        pkiEndEntityProfileName = getPkiProperty(bundleContext, PKI_END_ENTITY_PROFILE_NAME_PROPERTY).orElseThrow(
                () -> new PropertyValueRequiredException(thesaurus, MessageSeeds.PROPERTY_VALUE_REQUIRED,
                        PKI_END_ENTITY_PROFILE_NAME_PROPERTY));
    }

    private Optional<String> getPkiProperty(BundleContext context, String property) {
        return Optional.of(context.getProperty(property));
    }

    @Override
    public X509Certificate signCsr(PKCS10CertificationRequest pkcs10) {
        lazyInit();
        X509Certificate x509Cert;
        CertificateResponse certificateResponse;
        UserDataVOWS userData = new UserDataVOWS();
        userData.setCaName(pkiCaName);
        userData.setEndEntityProfileName(pkiEndEntityProfileName);
        userData.setCertificateProfileName(pkiCertificateProfileName);
        userData.setSubjectDN(pkcs10.getSubject().toString());
        userData.setUsername(getUsernameFromCn(pkcs10.getSubject()));
        try {
            certificateResponse = ejbcaWS
                    .certificateRequest(userData, new String(Base64.encode(pkcs10.getEncoded())), CertificateHelper.CERT_REQ_TYPE_PKCS10, null,
                            CertificateHelper.RESPONSETYPE_CERTIFICATE);
            x509Cert = certificateResponse.getCertificate();
        } catch (ApprovalException_Exception | AuthorizationDeniedException_Exception | EjbcaException_Exception | NotFoundException_Exception | UserDoesntFullfillEndEntityProfile_Exception | WaitingForApprovalException_Exception | IOException | CertificateException e) {
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getMessage());
        }
        return x509Cert;
    }

    @Override
    public void revokeCertificate(CertificateSearchFilter certificateTemplate, int reason) {
        lazyInit();
        if (RevokeStatus.fromValue(reason) == null) {
            throw new EnumLookupValueException(thesaurus, MessageSeeds.INVALID_REVOCATION_REASON, String.valueOf(reason));
        }
        try {
            ejbcaWS.revokeCert(certificateTemplate.getIssuerDN(), certificateTemplate.getSerialNumber().toString(SN_HEX), reason);
        } catch (AlreadyRevokedException_Exception | ApprovalException_Exception | AuthorizationDeniedException_Exception | CADoesntExistsException_Exception | EjbcaException_Exception | NotFoundException_Exception | WaitingForApprovalException_Exception e) {
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getMessage());
        }
    }

    @Override
    public RevokeStatus checkRevocationStatus(CertificateSearchFilter searchFilter) {
        lazyInit();
        RevokeStatus revokeStatus;
        org.ejbca.core.protocol.ws.client.gen.RevokeStatus rs;
        try {
            rs = ejbcaWS.checkRevokationStatus(searchFilter.getIssuerDN(), searchFilter.getSerialNumber().toString(SN_HEX));
            revokeStatus = RevokeStatus.fromValue(rs.getReason());
        } catch (AuthorizationDeniedException_Exception | CADoesntExistsException_Exception | EjbcaException_Exception | EnumLookupValueException e) {
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getMessage());
        }
        return revokeStatus;
    }

    @Override
    public Optional<X509CRL> getLatestCRL(String caname) {
        lazyInit();
        return getCrl(caname, false);
    }

    @Override
    public Optional<X509CRL> getLatestDeltaCRL(String caname) {
        lazyInit();
        return getCrl(caname, true);
    }

    @Override
    public List<String> getPkiCaNames() {
        lazyInit();
        try {
            return ejbcaWS.getAvailableCAs().stream().map(NameAndId::getName).collect(Collectors.toList());
        } catch (AuthorizationDeniedException_Exception | EjbcaException_Exception e) {
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getMessage());
        }
    }

    @Override
    public String getPkiInfo() {
        StringBuilder result = new StringBuilder();
        lazyInit();
        result.append("Version: ");
        String version = ejbcaWS.getEjbcaVersion();
        result.append(version).append('\n');
        try {
            Map<Integer, String> authorizedEEProfiles = ejbcaWS.getAuthorizedEndEntityProfiles().stream()
                    .collect(Collectors.toMap(NameAndId::getId, NameAndId::getName));
            for (Map.Entry<Integer, String> authorizedEEProfilesEntry : authorizedEEProfiles.entrySet()) {
                int profileId = authorizedEEProfilesEntry.getKey();
                String profileName = authorizedEEProfilesEntry.getValue();
                String caInProfile = ejbcaWS.getAvailableCAsInProfile(profileId).stream().map(NameAndId::getName)
                        .collect(Collectors.toList()).toString();
                String cpInProfile = ejbcaWS.getAvailableCertificateProfiles(profileId).stream().map(NameAndId::getName)
                        .collect(Collectors.toList()).toString();
                result.append("EE Profile: ").append(profileName).append('\n').append("CAs in profile: ").append(caInProfile).append('\n')
                        .append("CPs in profile: ").append(cpInProfile).append('\n');
            }
        } catch (AuthorizationDeniedException_Exception | EjbcaException_Exception e) {
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getMessage());
        }
        return result.toString();

    }

    private Optional<X509CRL> getCrl(String caName, boolean isDelta) {
        byte[] crlBytes;
        try {
            crlBytes = ejbcaWS.getLatestCRL(caName, isDelta);
            return Optional.ofNullable(null != crlBytes ? getX509CRL(crlBytes) : null);
        } catch (CADoesntExistsException_Exception | EjbcaException_Exception | CertificateException | CRLException e) {
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getMessage());
        }
    }

    private X509CRL getX509CRL(byte[] crlBytes) throws CertificateException, CRLException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509CRL) cf.generateCRL(new ByteArrayInputStream(crlBytes));
    }

    private String getUsernameFromCn(X500Name name) {
        RDN cn = name.getRDNs(BCStyle.CN)[0];
        return IETFUtils.valueToString(cn.getFirst().getValue());
    }

    /*
     * Management CA self-signed certificate should be in CXO truststore.
     * Superadmin client certificate and private key should be in CXO keystore (importSuperAdmin).
     * */
    private void setNewDefaultSSLSocketFactory() {
        TrustStore requiredTrustStore = securityManagementService.getAllTrustStores().stream()
                .filter(p -> p.getName().trim().equalsIgnoreCase(pkiTrustStore)).findFirst().orElseThrow(
                        () -> new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR,
                                "No connexo truststore with name " + pkiTrustStore + " found"));
        TrustedCertificate mgmtCaCertificate = requiredTrustStore.getCertificates().stream()
                .filter(p -> p.getAlias().trim().equalsIgnoreCase(MANAGEMENT_CA_ALIAS)).findFirst().orElseThrow(
                        () -> new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR,
                                "No self signed management ca certificate with alias " + MANAGEMENT_CA_ALIAS + " found"));
        X509Certificate x509MgmtCaCertificate = mgmtCaCertificate.getCertificate().orElseThrow(
                () -> new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR,
                        "No self signed management ca certificate with alias " + MANAGEMENT_CA_ALIAS + " found"));

        CertificateWrapper certificateWrapper = securityManagementService.findCertificateWrapper(pkiSuperAdminClientAlias).orElseThrow(
                () -> new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR,
                        "No superadmin client certificate with alias " + pkiSuperAdminClientAlias + " found"));

        X509Certificate superAdminCertificate = certificateWrapper.getCertificate().orElseThrow(
                () -> new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR,
                        "No superadmin client certificate with alias " + pkiSuperAdminClientAlias + " found"));

        PrivateKeyWrapper privateKeyWrapper = ((ClientCertificateWrapper) certificateWrapper).getPrivateKeyWrapper();
        if (privateKeyWrapper == null) {
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, "No private key for superadmin found");
        }

        try {
            PrivateKey privateKey = privateKeyWrapper.getPrivateKey();
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            trustStore.load(null, null);
            trustStore.setCertificateEntry(pkiSuperAdminClientAlias, x509MgmtCaCertificate);
            keyStore.setCertificateEntry(pkiSuperAdminClientAlias, superAdminCertificate);
            X509Certificate[] certChain = new X509Certificate[2];
            certChain[0] = superAdminCertificate;
            certChain[1] = x509MgmtCaCertificate;
            keyStore.setKeyEntry(pkiSuperAdminClientAlias, privateKey, "".toCharArray(), certChain);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, "".toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            SSLContext.setDefault(sslContext);
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> hostname.equals(pkiHost));
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyManagementException | InvalidKeyException e) {
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getMessage());
        }
    }

    private EjbcaWS createWSBackend() {
        EjbcaWSService service;
        setNewDefaultSSLSocketFactory();
        CryptoProviderTools.installBCProvider();
        QName Q_NAME = new QName("http://ws.protocol.core.ejbca.org/", "EjbcaWSService");
        String WSDL_LOCATION = "https://" + pkiHost.trim() + ':' + pkiPort + "/ejbca/ejbcaws/ejbcaws?wsdl";
        try {
            service = new EjbcaWSService(new URL(WSDL_LOCATION), Q_NAME);
        } catch (MalformedURLException e) {
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getMessage());
        }
        return service.getEjbcaWSPort();
    }

    private void lazyInit() {
        if (ejbcaWS == null) {
            ejbcaWS = createWSBackend();
        }
    }

    public void init(EjbcaWS ejbcaWS) {
        this.ejbcaWS = ejbcaWS;
    }

}
