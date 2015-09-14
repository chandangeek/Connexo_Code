package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;

public class FileImporterInfoFactory {

    private final Thesaurus thesaurus;
    private final PropertyUtils propertyUtils;

    @Inject
    public FileImporterInfoFactory(Thesaurus thesaurus, PropertyUtils propertyUtils) {
        this.thesaurus = thesaurus;
        this.propertyUtils = propertyUtils;
    }

    public FileImporterInfo asInfo(FileImporterFactory fileImporter) {
        FileImporterInfo info = new FileImporterInfo();
        info.name = fileImporter.getName();
        info.displayName = thesaurus.getStringBeyondComponent(fileImporter.getName(), fileImporter.getName());
        info.properties = propertyUtils.convertPropertySpecsToPropertyInfos(fileImporter.getPropertySpecs());
        return info;
    }
}
