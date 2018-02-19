package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.ftpclient.FtpClientService;
import sun.security.provider.X509Factory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class CertificateExportProcessor {

    private static final int PEM_CHARACTERS_ALIGNMENT = 64;

    private final FtpClientService ftpClientService;
    private final Map<String, Object> properties;

    public CertificateExportProcessor(FtpClientService ftpClientService, Map<String, Object> properties) {
        this.ftpClientService = ftpClientService;
        this.properties = properties;
    }

    public void processExport(LinkedHashMap<String, LinkedHashMap<String, X509Certificate>> certificates) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        for (Map.Entry<String, LinkedHashMap<String, X509Certificate>> stringLinkedHashMapEntry : certificates.entrySet()) {
            LinkedHashMap<String, X509Certificate> certificatesMap = stringLinkedHashMapEntry.getValue();
            for (Map.Entry<String, X509Certificate> stringX509CertificateEntry : certificatesMap.entrySet()) {
                X509Certificate x509Certificate = stringX509CertificateEntry.getValue();
                String dirName = stringLinkedHashMapEntry.getKey();
                String fileName = dirName + '/' + stringX509CertificateEntry.getKey() + ".pem";
                try {
                    storeDlmsKeyStoreCertificate(zipOutputStream, x509Certificate, fileName);
                } catch (CertificateEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            zipOutputStream.finish();
            zipOutputStream.close();
            exportToFtpTest(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void storeDlmsKeyStoreCertificate(ZipOutputStream zipOutputStream, X509Certificate x509Certificate, String fileName)
            throws CertificateEncodingException, IOException {
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
        for (int index = 0; index < encodeToStringLength; index++) {
            pem.append(encodeToString.charAt(index));
            if (index % PEM_CHARACTERS_ALIGNMENT == 0 && index > 0) {
                pem.append('\n');
            }
        }
        pem.append('\n').append(X509Factory.END_CERT);
        return pem.toString();
    }

    private void exportToFtp(byte[] bytes) throws IOException {
        String host = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_HOSTNAME.getPropertyKey());
        Integer port = (Integer) properties.get(CSRImporterTranslatedProperty.EXPORT_PORT.getPropertyKey());
        String username = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_USER.getPropertyKey());
        String password = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_PASSWORD.getPropertyKey());
        String filename = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_FILE_NAME.getPropertyKey());
        String directory = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_FILE_LOCATION.getPropertyKey());
        String extension = (String) properties.get(CSRImporterTranslatedProperty.EXPORT_FILE_EXTENSION.getPropertyKey());
        ftpClientService.getSftpFactory(host, port, username, password).runInSession(fileSystem -> {
            Path file = fileSystem.getPath(directory.replaceAll("/$", "") + "/" + filename + "/" + extension);
            Files.write(file, bytes);
        });
    }

    private void exportToFtpTest(byte[] bytes) throws IOException {
        String host = "192.168.99.100";
        Integer port = 2222;
        String username = "foo";
        String password = "pass";
        String filename = "test1234";
        String directory = "upload";
        String extension = "zip";
        ftpClientService.getSftpFactory(host, port, username, password).runInSession(fileSystem -> {
            Path file = fileSystem.getPath(directory.replaceAll("/$", "") + "/" + filename + "." + extension);
            Files.write(file, bytes);
        });
    }
}
