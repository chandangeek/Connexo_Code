/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.impl.MessageSeeds;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

class CSRZipFileParser {

    private final Thesaurus thesaurus;
    private final FileImportOccurrence fileImportOccurrence;
    private final Logger logger;

    private  Map<String, Map<String, PKCS10CertificationRequest>> keystoreCertificates;

    public CSRZipFileParser(FileImportOccurrence fileImportOccurrence, Thesaurus thesaurus) {
        this.fileImportOccurrence = fileImportOccurrence;
        this.thesaurus = thesaurus;

        logger = Logger.getLogger(this.getClass().getName());
    }

    public Map<String, Map<String, PKCS10CertificationRequest>> parse() throws IOException {
        String zipFileName = fileImportOccurrence.getPath();
        logger.info("Parsing zip file: " + zipFileName);
        ZipFile zipFile = new ZipFile(zipFileName);

        keystoreCertificates = new LinkedHashMap<>();

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()){
            ZipEntry zipEntry = entries.nextElement();
            InputStream entryStream = zipFile.getInputStream(zipEntry);
            processEntry(zipEntry, entryStream);
            //No resource leak --- Closing this ZIP file will, in turn, close all input
            //     * streams that have been returned by invocations of this method.
        }

        logger.info("Done processing zip file: "+zipFileName);
        zipFile.close();

        return keystoreCertificates;
    }

    private void processEntry(ZipEntry zipEntry, InputStream entryStream) throws IOException {
        logger.info("Processing zip entry: "+zipEntry.getName());

        String folderName = getFolderName(zipEntry.getName());

        if (zipEntry.isDirectory()) {
            keystoreCertificates.put(folderName, new LinkedHashMap<>());
            logger.info("ZIP folder added for processing: "+folderName);
        } else {
            String entryName = zipEntry.getName();
            String fileName = getFileName(entryName);
            Logger.getLogger(this.getClass().getName()).info("CSR file added: "+fileName);

            if (folderName == null || folderName.isEmpty()){
                folderName = getCommonName(fileName);
            }

            if ( ! keystoreCertificates.containsKey(folderName)){
                // this is a flat file, without any root
                Logger.getLogger(this.getClass().getName()).info("Root folder added: "+folderName);
                keystoreCertificates.put(folderName, new LinkedHashMap<>());
            }

            PKCS10CertificationRequest csr = getCsr(entryStream);
            keystoreCertificates.get(folderName).put(fileName, csr);
        }
    }

    private PKCS10CertificationRequest getCsr(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String encodedCsr = reader
                .lines()
                .filter((line) -> !line.startsWith("----"))
                .reduce("", String::concat);
        return new PKCS10CertificationRequest(Base64.getDecoder().decode(encodedCsr));
    }

    private String getFolderName(String entryName) {
        int pos = entryName.lastIndexOf("/");
        if (pos>=0) {
            // normal file in subfolder
            return entryName.substring(0, pos);
        } else {
            // file in root folder
            return "";
        }
    }

    private String getCommonName(String entryName) {
        int pos = entryName.lastIndexOf("-");
        if (pos>0) {
            // normal file in subfolder
            return entryName.substring(pos+1);
        }
        return entryName;
    }

    private String getFileName(String entryName) {
        String filePath = getFolderName(entryName);
        String fileName = entryName.substring(entryName.lastIndexOf("/") + 1, entryName.lastIndexOf("."));

        if (filePath.isEmpty()) {
            return fileName;
        } else if (StringUtils.countMatches(filePath, "/") > 0) {
            throw new CSRImporterException(thesaurus, MessageSeeds.SUBDIRECTORIES_IN_ZIP_FILE, filePath);
        } else if (!entryName.substring(entryName.lastIndexOf(".") + 1).toLowerCase().equals("csr")) {
            throw new CSRImporterException(thesaurus, MessageSeeds.NOT_CSR_FILE_EXTENSION, entryName);
        }

        return fileName;
    }
}