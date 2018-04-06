package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.util.Pair;

import sun.security.provider.X509Factory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class CertificateExportProcessor {

    private static final int PEM_CHARACTERS_ALIGNMENT = 64;

    private final Map<String, Object> properties;
    private final CertificateExportDestination exportDestination;
    private final CSRImporterLogger logger;

    CertificateExportProcessor(Map<String, Object> properties,
                                      CertificateExportDestination exportDestination,
                                      CSRImporterLogger logger) {
        this.properties = properties;
        this.exportDestination = exportDestination;
        this.logger = logger;
    }

    void processExport(Map<String, Map<String, X509Certificate>> certificates) throws IOException, CertificateEncodingException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            TrustStore trustStore = (TrustStore) properties.get(CSRImporterTranslatedProperty.EXPORT_TRUST_STORE.getPropertyKey());
            Map<String, X509Certificate> trustedCertificatesMap = trustStore == null ? Collections.emptyMap() : trustStore.getCertificates().stream()
                    .map(certificateWrapper -> Pair.of(certificateWrapper.getAlias(), certificateWrapper.getCertificate().orElse(null)))
                    .filter(Pair::hasLast)
                    .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
            for (Map.Entry<String, Map<String, X509Certificate>> certificatesForDevice : certificates.entrySet()) {
                Map<String, X509Certificate> certificatesMap = certificatesForDevice.getValue();
                String dirName = certificatesForDevice.getKey();
                for (Map.Entry<String, X509Certificate> fullAliasAndCertificate : certificatesMap.entrySet()) {
                    storeDlmsKeyStoreCertificate(zipOutputStream, fullAliasAndCertificate.getValue(), dirName, fullAliasAndCertificate.getKey());
                }
                for (Map.Entry<String, X509Certificate> fullAliasAndCertificate : trustedCertificatesMap.entrySet()) {
                    storeDlmsKeyStoreCertificate(zipOutputStream, fullAliasAndCertificate.getValue(), dirName, fullAliasAndCertificate.getKey());
                }
            }
            zipOutputStream.finish();
        }
        byte[] bytes = byteArrayOutputStream.toByteArray();
        byte[] signature = getSignature(bytes);
        exportDestination.export(bytes, signature);
        logger.log(MessageSeeds.CERTIFICATES_EXPORTED_SUCCESSFULLY, exportDestination.getLastExportedFileName().get(), exportDestination.getUrl());
    }

    private void storeDlmsKeyStoreCertificate(ZipOutputStream zipOutputStream, X509Certificate x509Certificate, String dirName, String alias)
            throws CertificateEncodingException, IOException {
        String fileName = dirName + '/' + alias + ".pem";
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
        CertificateWrapper certificateWrapper = securityAccessor.getActualValue()
                .orElseThrow(() -> new IllegalStateException("There is no active certificate in centrally managed security accessor!"));
        return getSignature(certificateWrapper, bytes, logger.getThesaurus());
    }

    // TODO: move to some utils
    public static byte[] getSignature(CertificateWrapper certificateWrapper, byte[] bytes, Thesaurus thesaurus) {
        try {
            if (certificateWrapper.hasPrivateKey()
                    && ((ClientCertificateWrapper) certificateWrapper).getPrivateKeyWrapper().getPrivateKey().isPresent()) {
                Signature signature = Signature.getInstance("SHA256withRSA");
                signature.initSign(((ClientCertificateWrapper) certificateWrapper).getPrivateKeyWrapper().getPrivateKey().get());
                signature.update(bytes);
                byte[] signatureBytes = signature.sign();
                if (signatureBytes.length == CSRImporter.SIGNATURE_LENGTH) {
                    return signatureBytes;
                } else {
                    throw new CSRImporterException(thesaurus, MessageSeeds.INAPPROPRIATE_CERTIFICATE_TYPE, certificateWrapper.getAlias(), "RSA " + CSRImporter.RSA_MODULUS_BIT_LENGTH);
                }
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            throw new CSRImporterException(thesaurus, MessageSeeds.FAILED_TO_SIGN, e);
        }
        throw new CSRImporterException(thesaurus, MessageSeeds.NO_PRIVATE_KEY_FOR_SIGNING, certificateWrapper.getAlias());
    }
}
