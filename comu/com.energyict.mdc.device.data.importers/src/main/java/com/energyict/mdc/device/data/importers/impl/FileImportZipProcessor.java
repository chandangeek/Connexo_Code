package com.energyict.mdc.device.data.importers.impl;

import java.util.zip.ZipFile;

public interface FileImportZipProcessor {

    void process(ZipFile zipFile, FileImportZipEntry data, FileImportZipLogger logger);
}