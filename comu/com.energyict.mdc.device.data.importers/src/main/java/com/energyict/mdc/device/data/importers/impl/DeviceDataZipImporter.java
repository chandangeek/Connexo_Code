package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;

import java.io.IOException;
import java.util.zip.ZipFile;

public class DeviceDataZipImporter implements FileImporter {

    public static class Builder  {

        private final DeviceDataZipImporter importer;

        private Builder() {
            this.importer = new DeviceDataZipImporter();
        }

        public Builder withProcessor(FileImportZipProcessor processor) {
            this.importer.processor = processor;
            return this;
        }

        public Builder withLogger(FileImportZipLogger logger) {
            this.importer.logger = logger;
            return this;
        }

        public DeviceDataZipImporter build() {
            return this.importer;
        }
    }

    private FileImportZipParser parser;
    private FileImportZipProcessor processor;
    private FileImportZipLogger logger;

    public static Builder withParser(FileImportZipParser parser) {
        Builder builder = new Builder();
        builder.importer.parser = parser;
        return builder;
    }

    private DeviceDataZipImporter() {
    }

    /**
     * Is responsible for doing the actual import. It processes the contents of the entire zip file.
     *
     * @param fileImportOccurrence Represents the entire file data that is to be processed.
     */
    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        logger.init(fileImportOccurrence);
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(fileImportOccurrence.getPath());
            parser.init(zipFile);
            parser.getZipEntries().forEach((FileImportZipEntry importZipEntry) -> processZipEntry(zipFile, importZipEntry));
            closeZip(zipFile, logger);
            logger.importFinished();
        } catch (Exception e) {
            logger.importFailed(e);
        }
    }

    private void processZipEntry(ZipFile zipFile, FileImportZipEntry importZipEntry) {
        try {
            processor.process(zipFile, importZipEntry, logger);
            logger.importZipEntryFinished(zipFile, importZipEntry);
        } catch (Exception exception) {
            logger.importZipEntryFailed(zipFile, importZipEntry, exception);
        }
    }

    private void closeZip(ZipFile zipFile, FileImportZipLogger logger) {
        try {
            if (zipFile != null) {
                zipFile.close();
            }
        } catch (IOException e) {
            logger.info(String.format("file %s could not be closed ", zipFile.getName()));
        }
    }
}
