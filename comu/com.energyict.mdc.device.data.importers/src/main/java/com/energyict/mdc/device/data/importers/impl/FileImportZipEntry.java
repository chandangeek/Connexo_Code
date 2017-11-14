package com.energyict.mdc.device.data.importers.impl;

import java.util.zip.ZipEntry;

public class FileImportZipEntry {
    private String directory;
    private String fileName;
    private ZipEntry zipEntry;
    private String securityAccessorTypeName;

    public FileImportZipEntry() {
    }

    public FileImportZipEntry(String directory, String fileName, ZipEntry zipEntry, String securityAccessorTypeName) {
        this.directory = directory;
        this.fileName = fileName;
        this.zipEntry = zipEntry;
        this.securityAccessorTypeName = securityAccessorTypeName;
    }

    public String getDirectory() {
        return directory;
    }

    public String getFileName() {
        return fileName;
    }

    public ZipEntry getZipEntry() {
        return zipEntry;
    }

    public String getSecurityAccessorTypeName() {
        return securityAccessorTypeName;
    }
}
