package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.FileImporterFactory;

import javax.inject.Inject;

public class FileImporterInfoFactory {

    private final PropertyUtils propertyUtils;

    @Inject
    public FileImporterInfoFactory(PropertyUtils propertyUtils) {
        this.propertyUtils = propertyUtils;
    }

    public FileImporterInfo asInfo(FileImporterFactory fileImporter) {
        FileImporterInfo info = new FileImporterInfo();
        info.name = fileImporter.getName();
        info.displayName = fileImporter.getDisplayName();
        info.properties = propertyUtils.convertPropertySpecsToPropertyInfos(fileImporter.getPropertySpecs());
        return info;
    }
}
