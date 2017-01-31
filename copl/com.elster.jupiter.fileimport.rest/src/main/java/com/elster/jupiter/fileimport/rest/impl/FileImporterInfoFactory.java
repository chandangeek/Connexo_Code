/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;

import javax.inject.Inject;

public class FileImporterInfoFactory {

    private final PropertyValueInfoService propertyValueInfoService;

    @Inject
    public FileImporterInfoFactory(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    public FileImporterInfo asInfo(FileImporterFactory fileImporter) {
        FileImporterInfo info = new FileImporterInfo();
        info.name = fileImporter.getName();
        info.displayName = fileImporter.getDisplayName();
        info.properties = propertyValueInfoService.getPropertyInfos(fileImporter.getPropertySpecs());
        return info;
    }
}
