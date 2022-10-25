package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import org.json.JSONObject;
import sun.security.provider.X509Factory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class CertificateExportProcessor {

    private static final int PEM_CHARACTERS_ALIGNMENT = 64;

    private final Map<String, Object> properties;
    private final CertificateExportDestination exportDestination;
    private final CSRImporterLogger logger;
    private final SecurityManagementService securityManagementService;
    private Map<String, X509Certificate> clientCertificates = null;

    CertificateExportProcessor(Map<String, Object> properties,
                                      CertificateExportDestination exportDestination,
                                      SecurityManagementService securityManagementService,
                                      CSRImporterLogger logger) {
        this.properties = properties;
        this.exportDestination = exportDestination;
        this.securityManagementService = securityManagementService;
        this.logger = logger;
    }

    void processExport(Map<String, Map<String, X509Certificate>> certificates) throws IOException, CertificateEncodingException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {

            prepareClientCertificates();

            for (Map.Entry<String, Map<String, X509Certificate>> certificatesForDevice : certificates.entrySet()) {
                Map<String, X509Certificate> certificatesMap = certificatesForDevice.getValue();
                String dirName = certificatesForDevice.getKey();
                for (Map.Entry<String, X509Certificate> fullAliasAndCertificate : certificatesMap.entrySet()) {
                    storeDlmsKeyStoreCertificate(zipOutputStream, fullAliasAndCertificate.getValue(), dirName, fullAliasAndCertificate.getKey());
                }

                addClientCertificates(zipOutputStream, dirName);
            }

            zipOutputStream.finish();
        }

        byte[] bytes = byteArrayOutputStream.toByteArray();
        byte[] signature = getSignature(bytes);
        exportDestination.export(bytes, signature);
        logger.log(MessageSeeds.CERTIFICATES_EXPORTED_SUCCESSFULLY, exportDestination.getLastExportedFileName().get(), exportDestination.getUrl());
    }

    /**
     * For Beacons (and maybe other devices) a full list of HES client certificates can be added to the archive,
     * along with the server signed certificates.
     * Since the production tooling accepts a certain naming convention, we need to provide those file with the proper
     * naming. We do this by mapping the alias in Connexo with the desired name.
     *
     * We prepare everything before the actual export, to optimize the export
     */
    private void prepareClientCertificates() {
        String jsonMappingProperty = (String) properties.get(CSRImporterTranslatedProperty.CLIENT_TRUSTSTORE_MAPPING.getPropertyKey());
        if (Objects.isNull(jsonMappingProperty) || jsonMappingProperty.isEmpty()){
            return;
        }
        clientCertificates = new HashMap<>();
        try {
            JSONObject jsonMapping = new JSONObject(jsonMappingProperty);
            Iterator keys = jsonMapping.keys();

            while (keys.hasNext()) {
                String connexoAlias = (String) keys.next();
                String exportName = jsonMapping.getString(connexoAlias);

                Condition condition = Where.where("alias").isEqualTo(connexoAlias);
                Finder<CertificateWrapper> certificateWrapperFinder = securityManagementService.findCertificateWrappers(condition)
                        .paged(0,2); // pageSie = 2 because we just need to see if there are duplicates (shouldn't be!)
                List<CertificateWrapper> foundAliases = certificateWrapperFinder.find();

                /**
                 *  Verify many-many times that we have the actual certificate to export!
                 *  Even it's validated when the importer is configured, could be that somebody messes up the config
                 *  after the PKI signed the CSR, we don't want to screw everything up because a wrong config
                 */
                if (foundAliases.size()==1){
                    CertificateWrapper certificateWrapper;
                    certificateWrapper = foundAliases.get(0);
                    if (certificateWrapper.getCertificate().isPresent()) {
                        exportName = appendCommonName(exportName, certificateWrapper);
                        clientCertificates.put(exportName, certificateWrapper.getCertificate().get());
                    }
                }
                if (foundAliases.size()>1){
                    logger.log(MessageSeeds.ALIAS_NOT_UNIQUE_EXCEPTION, connexoAlias);
                }
                if (foundAliases.size()==0){
                    logger.log(MessageSeeds.ALIAS_NOT_FOUND_EXCEPTION, connexoAlias);
                }
            }

        } catch (Exception e) {
            logger.log(e);
            logger.markFailure();
        }
    }

    private String appendCommonName(String exportName, CertificateWrapper certificate) {
        Optional<String> cn = certificate.getSubjectCN();
        if (cn.isPresent()){
            return exportName+"-"+cn.get();
        } else {
            logger.log(MessageSeeds.CANNOT_EXTRACT_COMMON_NAME, certificate.getSubject());
            return exportName;
        }
    }

    private void addClientCertificates(ZipOutputStream zipOutputStream, String dirName) throws IOException, CertificateEncodingException {
        if (Objects.isNull(clientCertificates)){
            // nothing to do, clientCertificates not prepared, we don't have a property
            return;
        }
        for (Map.Entry<String, X509Certificate> entry : clientCertificates.entrySet()) {
            storeDlmsKeyStoreCertificate(zipOutputStream, entry.getValue(), dirName, entry.getKey());
        }
    }


    private SecurityManagementService getSecurityManagementService() {
        return securityManagementService;
    }

    private void storeDlmsKeyStoreCertificate(ZipOutputStream zipOutputStream, X509Certificate x509Certificate, String dirName, String alias)
            throws CertificateEncodingException, IOException {
        String fileName = dirName + '/' + alias + ".pem";
        if ((Boolean)properties.get(CSRImporterTranslatedProperty.EXPORT_FLAT_DIR.getPropertyKey())){
            // meter have single certificates and are stored in flat directory
            fileName = alias + ".pem";
        }
        try {
            zipOutputStream.putNextEntry(new ZipEntry(fileName));
            zipOutputStream.write(pemEncode(x509Certificate).getBytes());
            zipOutputStream.closeEntry();
        } catch (IOException e) {
            if (e instanceof ZipException && e.getMessage().contains("duplicate entry")) {
                zipOutputStream.closeEntry();
            } else {
                zipOutputStream.closeEntry();
                throw new IOException(e.getMessage());
            }
        }
    }

    private String pemEncode(X509Certificate certificate) throws CertificateEncodingException {
        StringBuilder pem = new StringBuilder();
        pem.append(X509Factory.BEGIN_CERT);

        String encodeToString = Base64.getEncoder().encodeToString(certificate.getEncoded());
        int encodeToStringLength = encodeToString.length();
        for (int index = 0; index < encodeToStringLength; ++index) {
            if (index % PEM_CHARACTERS_ALIGNMENT == 0) {
                pem.append('\n');
            }
            pem.append(encodeToString.charAt(index));
        }
        pem.append('\n').append(X509Factory.END_CERT);
        return pem.toString();
    }

    private byte[] getSignature(byte[] bytes) {
        SecurityAccessor<CertificateWrapper> securityAccessor = (SecurityAccessor<CertificateWrapper>) properties.get(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR.getPropertyKey());
        if (securityAccessor==null){
            // no signature available
            return "".getBytes();
        }
        CertificateWrapper certificateWrapper = securityAccessor.getActualValue()
                .orElseThrow(() -> new IllegalStateException("There is no active certificate in centrally managed security accessor!"));
        return getSignature(certificateWrapper, bytes, logger.getThesaurus());
    }

    // TODO: move to some utils
    public static byte[] getSignature(CertificateWrapper certificateWrapper, byte[] bytes, Thesaurus thesaurus) {
        try {
            if (certificateWrapper.hasPrivateKey()
                    && ((ClientCertificateWrapper) certificateWrapper).getPrivateKeyWrapper().getPrivateKey().isPresent()) {
                PrivateKey privateKey = ((ClientCertificateWrapper) certificateWrapper).getPrivateKeyWrapper().getPrivateKey().get();
                int modulusLength = ((RSAPrivateKey) privateKey).getModulus().bitLength();
                int signatureLength =  modulusLength / 8;
                Signature signature = Signature.getInstance("SHA256withRSA");
                signature.initSign(privateKey);
                signature.update(bytes);
                byte[] signatureBytes = signature.sign();
                if (signatureBytes.length == signatureLength) {
                    return signatureBytes;
                } else {
                    throw new CSRImporterException(thesaurus, MessageSeeds.INAPPROPRIATE_CERTIFICATE_TYPE, certificateWrapper.getAlias(), "RSA " + modulusLength);
                }
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            throw new CSRImporterException(thesaurus, MessageSeeds.FAILED_TO_SIGN, e);
        }
        throw new CSRImporterException(thesaurus, MessageSeeds.NO_PRIVATE_KEY_FOR_SIGNING, certificateWrapper.getAlias());
    }
}
