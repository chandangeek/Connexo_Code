/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateAuthoritySearchFilter;
import com.elster.jupiter.pki.CertificateRequestData;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.RevokeStatus;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.ejbca.core.protocol.ws.AlreadyRevokedException_Exception;
import org.ejbca.core.protocol.ws.ApprovalException_Exception;
import org.ejbca.core.protocol.ws.AuthorizationDeniedException_Exception;
import org.ejbca.core.protocol.ws.CADoesntExistsException_Exception;
import org.ejbca.core.protocol.ws.CertificateResponse;
import org.ejbca.core.protocol.ws.EjbcaException_Exception;
import org.ejbca.core.protocol.ws.EjbcaWS;
import org.ejbca.core.protocol.ws.EjbcaWSService;
import org.ejbca.core.protocol.ws.NameAndId;
import org.ejbca.core.protocol.ws.NotFoundException_Exception;
import org.ejbca.core.protocol.ws.UserDataVOWS;
import org.ejbca.core.protocol.ws.UserDoesntFullfillEndEntityProfile_Exception;
import org.ejbca.core.protocol.ws.WaitingForApprovalException_Exception;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component(name = "CaService", service = {CaService.class}, property = "name=" + CaService.COMPONENTNAME, immediate = true)
public class CaServiceImpl implements CaService {
    private static final Logger LOGGER = Logger.getLogger(CaServiceImpl.class.getName());

    private static final Integer SN_HEX = 16;
    private static final String PROTOCOL = "TLS";
    // felix config properties
    private static final String PKI_HOST_PROPERTY = "com.elster.jupiter.pki.host";
    private static final String PKI_PORT_PROPERTY = "com.elster.jupiter.pki.port";
    private static final String PKI_CXO_TRUSTSTORE_PROPERTY = "com.elster.jupiter.ca.truststore";
    private static final String PKI_SUPER_ADMIN_CLIENT_ALIAS_PROPERTY = "com.elster.jupiter.ca.certificate"; // this is the client authentication certificate (key-store entry)
    private static final String PKI_MANAGEMENT_CLIENT_ALIAS_PROPERTY = "com.elster.jupiter.ca.clientcertificate"; // this is the trust-store entry, contains client's CA entry

    // from Constants Required for EJBCA-WS
    public static final String RESPONSETYPE_CERTIFICATE = "CERTIFICATE";
    public static final String RESPONSETYPE_PKCS7 = "PKCS7";
    public static final String RESPONSETYPE_PKCS7WITHCHAIN = "PKCS7WITHCHAIN";
    public static final int CERT_REQ_TYPE_PKCS10 = 0;
    public static final int CERT_REQ_TYPE_CRMF = 1;
    public static final int CERT_REQ_TYPE_SPKAC = 2;
    public static final int CERT_REQ_TYPE_PUBLICKEY = 3;

    private static final String X509 = "X.509";
    public static final String BEGIN_CERTIFICATE_REQUEST = "-----BEGIN CERTIFICATE REQUEST-----\n";
    public static final String END_CERTIFICATE_REQUEST = "\n-----END CERTIFICATE REQUEST-----\n";
    public static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n";
    public static final String END_CERTIFICATE = "\n-----END CERTIFICATE-----\n";

    private boolean configured;
    private String pkiHost;
    private Integer pkiPort;
    private String pkiTrustStore;
    private String pkiSuperAdminClientAlias;
    private String pkiManagementClientAlias;

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
        initPkiProperties(bundleContext);
    }

    @Deactivate
    public void deactivate() {
        pkiHost = null;
        pkiPort = null;
        pkiTrustStore = null;
        pkiSuperAdminClientAlias = null;
        pkiManagementClientAlias = null;
        ejbcaWS = null;
        configured = false;
    }

    private void initPkiProperties(BundleContext bundleContext) {
        pkiHost = bundleContext.getProperty(PKI_HOST_PROPERTY);
        String port = bundleContext.getProperty(PKI_PORT_PROPERTY);
        pkiPort = StringUtils.isNotBlank(port) ? Integer.parseInt(port) : null;
        pkiTrustStore = bundleContext.getProperty(PKI_CXO_TRUSTSTORE_PROPERTY);
        pkiSuperAdminClientAlias = bundleContext.getProperty(PKI_SUPER_ADMIN_CLIENT_ALIAS_PROPERTY);
        pkiManagementClientAlias = bundleContext.getProperty(PKI_MANAGEMENT_CLIENT_ALIAS_PROPERTY);

        configured = pkiPort != null
                && StringUtils.isNotBlank(pkiHost)
                && StringUtils.isNotBlank(pkiTrustStore)
                && StringUtils.isNotBlank(pkiSuperAdminClientAlias);

        if (!configured) {
            LOGGER.info("CA service started in offline mode. Any service usages will be rejected until all properties are specified.");
        }
    }

    @Override
    public boolean isConfigured() {
        return configured;
    }

    @Override
    public X509Certificate signCsr(PKCS10CertificationRequest pkcs10, Optional<CertificateRequestData> certificateUserData) {
        LOGGER.info("Signing CSR");

        checkConfiguration();
        X509Certificate x509Cert;
        CertificateResponse certificateResponse;
        UserDataVOWS userData = new UserDataVOWS();

        String caName = (certificateUserData.isPresent()) ? certificateUserData.get().getCaName() : "";
        LOGGER.info("- CA name: " + caName);
        userData.setCaName(caName);

        String endEntity = (certificateUserData.isPresent()) ? certificateUserData.get().getEndEntityName() : "";
        LOGGER.info("- EndEntity: " + endEntity);
        userData.setEndEntityProfileName(endEntity);

        String certificateProfile = (certificateUserData.isPresent()) ? certificateUserData.get().getCertificateProfileName() : "";
        LOGGER.info("- CertificateProfile: " + certificateProfile);
        userData.setCertificateProfileName(certificateProfile);

        try {
            String fullSubjectDN = pkcs10.getSubject().toString();
            String subjecDNFields = (certificateUserData.isPresent())? certificateUserData.get().getSubjectDNfields(): "";
            String subjectDN = extractSubjectDN(fullSubjectDN, subjecDNFields);
            LOGGER.info(" - Subject DN: " + subjectDN);
            userData.setSubjectDN(subjectDN);

            String userName = getUsernameFromCn(pkcs10.getSubject());
            LOGGER.info(" - Username: " + userName);
            userData.setUsername(userName);

            String csrEncoded = new String(Base64.getEncoder().encode(pkcs10.getEncoded()));

            LOGGER.info("Sending CSR to EJBCA WebService:\n" + BEGIN_CERTIFICATE_REQUEST + csrEncoded + END_CERTIFICATE_REQUEST);

            certificateResponse = ejbcaWS.certificateRequest(userData, csrEncoded, CERT_REQ_TYPE_PKCS10, null, RESPONSETYPE_CERTIFICATE);

            LOGGER.info("Response received");
            LOGGER.info("\t- responseType: " + certificateResponse.getResponseType());
            if (certificateResponse.getResponseType()!= null &&
                    certificateResponse.getResponseType().equals(RESPONSETYPE_CERTIFICATE)) {
                LOGGER.info("\t- response:\n" + new String(certificateResponse.getData()) + "\n");
            } else {
                LOGGER.info("\t- responseData:\n" + javax.xml.bind.DatatypeConverter.printHexBinary(certificateResponse.getData()) +"\n" );

            }

            LOGGER.info("Parsing as X.509 certificate");
            //using MimeDecoder because the response data is Base64 with line breaks
            byte[] decodedCertificate = Base64.getMimeDecoder().decode(certificateResponse.getData());
            InputStream byteArrayInputStream = new ByteArrayInputStream(decodedCertificate);
            CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
            x509Cert = (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);

            String certEncoded  = new String(Base64.getEncoder().encode(x509Cert.getEncoded()));
            LOGGER.info("Final certificate:\n"+ BEGIN_CERTIFICATE +certEncoded+ END_CERTIFICATE);

        } catch (ApprovalException_Exception | AuthorizationDeniedException_Exception | EjbcaException_Exception | NotFoundException_Exception |
                UserDoesntFullfillEndEntityProfile_Exception | WaitingForApprovalException_Exception | IOException | CertificateException | InvalidNameException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getLocalizedMessage());
        }
        return x509Cert;
    }

    @Override
    public void revokeCertificate(CertificateAuthoritySearchFilter certificateTemplate, int reason) {
        checkConfiguration();
        if (!RevokeStatus.fromValue(reason).isPresent()) {
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.INVALID_REVOCATION_REASON, String.valueOf(reason));
        }
        try {
            ejbcaWS.revokeCert(certificateTemplate.getIssuerDN(), certificateTemplate.getSerialNumber().toString(SN_HEX), reason);
        } catch (AlreadyRevokedException_Exception | ApprovalException_Exception | AuthorizationDeniedException_Exception | CADoesntExistsException_Exception | EjbcaException_Exception | NotFoundException_Exception | WaitingForApprovalException_Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getLocalizedMessage());
        }
    }

    @Override
    public RevokeStatus checkRevocationStatus(CertificateAuthoritySearchFilter searchFilter) {
        checkConfiguration();
        org.ejbca.core.protocol.ws.RevokeStatus rs;
        try {
            rs = ejbcaWS.checkRevokationStatus(searchFilter.getIssuerDN(), searchFilter.getSerialNumber().toString(SN_HEX));
        } catch (AuthorizationDeniedException_Exception | CADoesntExistsException_Exception | EjbcaException_Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getLocalizedMessage());
        }
        return RevokeStatus.fromValue(rs.getReason()).orElseThrow(() -> new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR));
    }

    @Override
    public Optional<X509CRL> getLatestCRL(String caname) {
        checkConfiguration();
        return getCrl(caname, false);
    }

    @Override
    public Optional<X509CRL> getLatestDeltaCRL(String caname) {
        checkConfiguration();
        return getCrl(caname, true);
    }

    @Override
    public List<String> getPkiCaNames() {
        checkConfiguration();
        try {
            return ejbcaWS.getAvailableCAs().stream().map(NameAndId::getName).collect(Collectors.toList());
        } catch (AuthorizationDeniedException_Exception | EjbcaException_Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getLocalizedMessage());
        }
    }

    public List<IdWithNameInfo> getEndEntities() {
        checkConfiguration();
        try {
            return ejbcaWS.getAuthorizedEndEntityProfiles().stream().map(f -> new IdWithNameInfo(f.getId(), f.getName())).collect(Collectors.toList());
        } catch (AuthorizationDeniedException_Exception | EjbcaException_Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getLocalizedMessage());
        }
    }

    @Override
    public List<IdWithNameInfo> getCaName(int endEntityId) {
        try {
            return ejbcaWS.getAvailableCAsInProfile(endEntityId).stream().map(f -> new IdWithNameInfo(f.getId(), f.getName())).collect(Collectors.toList());
        } catch (AuthorizationDeniedException_Exception | EjbcaException_Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getLocalizedMessage());
        }
    }

    @Override
    public List<IdWithNameInfo> getCertificateProfile(int endEntityId) {
        try {
            return ejbcaWS.getAvailableCertificateProfiles(endEntityId).stream().map(f -> new IdWithNameInfo(f.getId(), f.getName())).collect(Collectors.toList());
        } catch (AuthorizationDeniedException_Exception | EjbcaException_Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getLocalizedMessage());
        }
    }

    @Override
    public String getPkiInfo() {
        checkConfiguration();
        StringBuilder result = new StringBuilder();
        try {
            result.append("Version: ");
            String version = ejbcaWS.getEjbcaVersion();
            result.append(version).append('\n');
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
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.INVALID_REVOCATION_REASON, e.getLocalizedMessage());
        }
        return result.toString();
    }

    private Optional<X509CRL> getCrl(String caName, boolean isDelta) {
        byte[] crlBytes;
        try {
            crlBytes = ejbcaWS.getLatestCRL(caName, isDelta);
            return Optional.ofNullable(null != crlBytes ? getX509CRL(crlBytes) : null);
        } catch (CADoesntExistsException_Exception | EjbcaException_Exception | CertificateException | CRLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getLocalizedMessage());
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
        TrustStore requiredTrustStore = securityManagementService
                .getAllTrustStores()
                .stream()
                .filter(p -> p.getName().trim().equalsIgnoreCase(pkiTrustStore))
                .findFirst()
                .orElseThrow(
                        () -> new CertificateAuthorityRuntimeException(
                                thesaurus,
                                MessageSeeds.CA_RUNTIME_ERROR_NO_TRUSTSTORE,
                                pkiTrustStore)
                );
        TrustedCertificate mgmtCaCertificate = requiredTrustStore
                .getCertificates()
                .stream()
                .filter(p -> p.getAlias().trim().equalsIgnoreCase(pkiManagementClientAlias))
                .findFirst()
                .orElseThrow(
                        () -> new CertificateAuthorityRuntimeException(
                                thesaurus,
                                MessageSeeds.CA_RUNTIME_ERROR_NO_SELF_SIGNED_CERTIFICATE,
                                pkiManagementClientAlias)
                );
        X509Certificate x509MgmtCaCertificate = mgmtCaCertificate
                .getCertificate()
                .orElseThrow(
                        () -> new CertificateAuthorityRuntimeException(
                                thesaurus,
                                MessageSeeds.CA_RUNTIME_ERROR_NO_SELF_SIGNED_CERTIFICATE,
                                pkiManagementClientAlias)
                );
        CertificateWrapper certificateWrapper = securityManagementService
                .findCertificateWrapper(pkiSuperAdminClientAlias)
                .orElseThrow(
                        () -> new CertificateAuthorityRuntimeException(
                                thesaurus,
                                MessageSeeds.CA_RUNTIME_ERROR_NO_CLIENT_CERTIFICATE,
                                pkiSuperAdminClientAlias)
                );
        X509Certificate superAdminCertificate = certificateWrapper
                .getCertificate()
                .orElseThrow(
                        () -> new CertificateAuthorityRuntimeException(
                                thesaurus,
                                MessageSeeds.CA_RUNTIME_ERROR_NO_CLIENT_CERTIFICATE,
                                pkiSuperAdminClientAlias)
                );
        PrivateKeyWrapper privateKeyWrapper = ((ClientCertificateWrapper) certificateWrapper).getPrivateKeyWrapper();
        if (privateKeyWrapper == null) {
            throw new CertificateAuthorityRuntimeException(
                    thesaurus,
                    MessageSeeds.CA_RUNTIME_ERROR_NO_PRIVATE_KEY_FOR_CLIENT_CERTIFICATE
            );
        }
        try {
            if (!privateKeyWrapper.getPrivateKey().isPresent()) {
                throw new CertificateAuthorityRuntimeException(
                        thesaurus,
                        MessageSeeds.CA_RUNTIME_ERROR_NO_PRIVATE_KEY_FOR_CLIENT_CERTIFICATE
                );
            }
            PrivateKey privateKey = privateKeyWrapper.getPrivateKey().get();
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            trustStore.load(null, null);
            trustStore.setCertificateEntry(pkiManagementClientAlias, x509MgmtCaCertificate);
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
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getLocalizedMessage());
        }
    }

    private EjbcaWS createWSBackend() {
        EjbcaWSService service;
        setNewDefaultSSLSocketFactory();
        QName Q_NAME = new QName("http://ws.protocol.core.ejbca.org/", "EjbcaWSService");
        String WSDL_LOCATION = "https://" + pkiHost.trim() + ':' + pkiPort + "/ejbca/ejbcaws/ejbcaws?wsdl";
        try {
            service = new EjbcaWSService(new URL(WSDL_LOCATION), Q_NAME);
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR, e.getLocalizedMessage());
        }
        return service.getEjbcaWSPort();
    }

    private void lazyInit() {
        if (ejbcaWS == null) {
            ejbcaWS = createWSBackend();
        }
    }

    void init(EjbcaWS ejbcaWS) {
        this.ejbcaWS = ejbcaWS;
    }

    private void checkConfiguration() {
        if (!configured) {
            throw new CertificateAuthorityRuntimeException(thesaurus, MessageSeeds.CA_RUNTIME_ERROR_NOT_CONFIGURED_PROPERLY);
        }
        lazyInit();
    }


    public byte[] getPKCS7(byte[] pkcs7Data) {
        return Base64.getDecoder().decode(pkcs7Data);
    }

    /** PKI end-entity profile can be configured to block / allow / optional some DN fields.
     * Usually everybody accepts CN, but can block others, like OU, O, etc.
     *
     * This method will send only the ones configured in Connexo, which should match the PKI config.
     *
     * @param fullSubjectDN - full subject from CSR, ex CN=4B464D1010006D3B,OU=KFM,O=SZ Kaifa Technology Co.\,Ltd.,C=CN
     * @param subjecDNFields - list of fields allowed (comma separated)
     * @return the allowed SubjectDN, ex:  ex CN=4B464D1010006D3B
     */
    public String extractSubjectDN(String fullSubjectDN, String subjecDNFields) throws InvalidNameException {
        if (subjecDNFields == null){
            return fullSubjectDN;
        }
        if (subjecDNFields.trim().isEmpty()){
            return fullSubjectDN;
        }

        String[] allowedFields = subjecDNFields.toUpperCase().replace(" ", ",").split(",");
        Set<String> fieldsSet = new HashSet<>(Arrays.asList(allowedFields));

        ArrayList<String> finalSubjectDN = new ArrayList<String>();

        LdapName ldapName = new LdapName(fullSubjectDN);
        for(Rdn rdn : ldapName.getRdns()) {
            if (fieldsSet.contains(rdn.getType().toUpperCase())){
                finalSubjectDN.add(rdn.toString());
            }
        }

        Collections.reverse(finalSubjectDN);
        return String.join(",", finalSubjectDN);

    }
}
