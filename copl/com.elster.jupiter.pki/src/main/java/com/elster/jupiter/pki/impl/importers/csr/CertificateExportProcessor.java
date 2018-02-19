package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.ftpclient.FtpClientService;
import sun.security.provider.X509Factory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    public CertificateExportProcessor(FtpClientService ftpClientService) {
        this.ftpClientService = ftpClientService;
    }

    public void processExport(LinkedHashMap<String, LinkedHashMap<String, X509Certificate>> certificates) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        for (Map.Entry<String, LinkedHashMap<String, X509Certificate>> stringLinkedHashMapEntry : certificates.entrySet()) {
            LinkedHashMap<String, X509Certificate> certificatesMap = stringLinkedHashMapEntry.getValue();
            for (Map.Entry<String, X509Certificate> stringX509CertificateEntry : certificatesMap.entrySet()) {
                X509Certificate x509Certificate = stringX509CertificateEntry.getValue();
                // Note that the path separator in ZIP files is a slash (/), even on other platforms such as Windows
                String dirName = stringLinkedHashMapEntry.getKey().substring(0, stringLinkedHashMapEntry.getKey().lastIndexOf('/'));
                String fileName = dirName + '/' + stringX509CertificateEntry.getKey();
                try {
                    storeDlmsKeyStoreCertificate(zipOutputStream, x509Certificate, fileName);
                } catch (CertificateEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        byteArrayOutputStream.toByteArray();
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
}
