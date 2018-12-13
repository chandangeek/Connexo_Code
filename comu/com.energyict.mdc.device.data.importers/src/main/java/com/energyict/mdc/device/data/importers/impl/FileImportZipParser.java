package com.energyict.mdc.device.data.importers.impl;

import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public interface FileImportZipParser {
    void init(ZipFile zipFile);

    Stream<FileImportZipEntry> getZipEntries();
}
