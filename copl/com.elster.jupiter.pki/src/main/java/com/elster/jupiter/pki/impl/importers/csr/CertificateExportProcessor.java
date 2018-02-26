package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.pki.impl.MessageSeeds;

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
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class CertificateExportProcessor {

    private static final int PEM_CHARACTERS_ALIGNMENT = 64;

    private final Map<String, Object> properties;
    private final CertificateExportDestination exportDestination;
    private final CSRImporterLogger logger;

    public CertificateExportProcessor(Map<String, Object> properties,
                                      CertificateExportDestination exportDestination,
                                      CSRImporterLogger logger) {
        this.properties = properties;
        this.exportDestination = exportDestination;
        this.logger = logger;
    }

    public void processExport(Map<String, Map<String, X509Certificate>> certificates) throws IOException, CertificateEncodingException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        TrustStore trustStore = (TrustStore) properties.get(CSRImporterTranslatedProperty.EXPORT_TRUST_STORE.getPropertyKey());
        for (Map.Entry<String, Map<String, X509Certificate>> certificatesForDevice : certificates.entrySet()) {
            Map<String, X509Certificate> certificatesMap = certificatesForDevice.getValue();
            String dirName = certificatesForDevice.getKey();
            for (Map.Entry<String, X509Certificate> stringX509CertificateEntry : certificatesMap.entrySet()) {
                X509Certificate x509Certificate = stringX509CertificateEntry.getValue();
                storeDlmsKeyStoreCertificate(zipOutputStream, x509Certificate, dirName, stringX509CertificateEntry.getKey());
            }
            if (trustStore != null) {
                for (TrustedCertificate trustedCertificate : trustStore.getCertificates()) {
                    if (trustedCertificate.getCertificate().isPresent()) {
                        storeDlmsKeyStoreCertificate(zipOutputStream, trustedCertificate.getCertificate().get(), dirName, trustedCertificate.getAlias());
                    }
                }
            }
        }
        zipOutputStream.finish();
        zipOutputStream.close();
        byte[] bytes = byteArrayOutputStream.toByteArray();
        byte[] signature = getSignature(bytes);
        exportDestination.export(bytes, signature);
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
        pem.append(X509Factory.BEGIN_CERT).append('\n');

        String encodeToString = Base64.getEncoder().encodeToString(certificate.getEncoded());
        int encodeToStringLength = encodeToString.length();
        for (int index = 1; index < encodeToStringLength; index++) {
            pem.append(encodeToString.charAt(index));
            if (index % PEM_CHARACTERS_ALIGNMENT == 0 && index > 0) {
                pem.append('\n');
            }
        }
        pem.append('\n').append(X509Factory.END_CERT);
        return pem.toString();
    }

    private byte[] getSignature(byte[] bytes) {
        SecurityAccessor<CertificateWrapper> securityAccessor = (SecurityAccessor<CertificateWrapper>) properties.get(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR.getPropertyKey());
        CertificateWrapper certificateWrapper = securityAccessor.getActualValue()
                .orElseThrow(() -> new IllegalStateException("There is no active certificate in centrally managed security accessor!"));
        try {
            if (certificateWrapper.hasPrivateKey()
                    && ((ClientCertificateWrapper) certificateWrapper).getPrivateKeyWrapper().getPrivateKey().isPresent()) {
                Signature signature = Signature.getInstance("SHA" + CSRImporter.SIGNATURE_LENGTH + "withRSA");
                signature.initSign(((ClientCertificateWrapper) certificateWrapper).getPrivateKeyWrapper().getPrivateKey().get());
                signature.update(bytes);
                byte[] signatureBytes = signature.sign();
                if (signatureBytes.length == CSRImporter.SIGNATURE_LENGTH) {
                    return signatureBytes;
                } else {
                    throw new CSRImporterException(logger.getThesaurus(), MessageSeeds.INAPPROPRIATE_CERTIFICATE_TYPE, certificateWrapper.getAlias(), "RSA 2048");
                }
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            throw new CSRImporterException(logger.getThesaurus(), MessageSeeds.FAILED_TO_SIGN, e);
        }
        throw new CSRImporterException(logger.getThesaurus(), MessageSeeds.NO_PRIVATE_KEY_FOR_SIGNING, certificateWrapper.getAlias());
    }
}
