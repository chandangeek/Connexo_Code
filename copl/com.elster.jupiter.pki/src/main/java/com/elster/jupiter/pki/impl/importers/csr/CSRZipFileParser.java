/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.impl.MessageSeeds;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class CSRZipFileParser {

    private final Thesaurus thesaurus;

    CSRZipFileParser(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    Map<String, Map<String, PKCS10CertificationRequest>> parseInputStream(InputStream inputStream) throws IOException {
        ZipEntry zipEntry;
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        Map<String, Map<String, PKCS10CertificationRequest>> keystoreCertificates = new LinkedHashMap<>();
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (zipEntry.isDirectory()) {
                keystoreCertificates.put(getFolderName(zipEntry.getName()), new LinkedHashMap<>());
            } else {
                keystoreCertificates.get(getFolderName(zipEntry.getName())).put(getFileName(zipEntry.getName()), getCsr(zipEntryToInputStream(zipInputStream)));
            }
        }
        return keystoreCertificates;
    }

    private InputStream zipEntryToInputStream(ZipInputStream zipInputStream) throws IOException {
        int length;
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while ((length = zipInputStream.read(buffer)) > 0) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    private PKCS10CertificationRequest getCsr(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String encodedCsr = reader.lines().filter((line) -> !line.startsWith("----")).reduce("", String::concat);
        return new PKCS10CertificationRequest(Base64.getDecoder().decode(encodedCsr));
    }

    private String getFolderName(String entryName) {
        return entryName.substring(0, entryName.lastIndexOf("/"));
    }

    private String getFileName(String entryName) {
        String filePath = getFolderName(entryName);
        String fileName = entryName.substring(entryName.lastIndexOf("/") + 1, entryName.lastIndexOf("."));

        if (filePath.isEmpty()) {
            throw new CSRImporterException(thesaurus, MessageSeeds.NO_DIRECTORY_FOR_CSR_FILE, fileName);
        } else if (StringUtils.countMatches(filePath, "/") > 0) {
            throw new CSRImporterException(thesaurus, MessageSeeds.SUBDIRECTORIES_IN_ZIP_FILE, filePath);
        } else if (!entryName.substring(entryName.lastIndexOf(".") + 1).toLowerCase().equals("csr")) {
            throw new CSRImporterException(thesaurus, MessageSeeds.NOT_CSR_FILE_EXTENSION, entryName);
        }

        return fileName;
    }
}