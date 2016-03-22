package com.elster.jupiter.metering.imports.impl;


import com.elster.jupiter.cps.CustomPropertySet;

import java.util.Map;

public abstract class FileImportRecordWithCustomProperties extends FileImportRecord {

    public FileImportRecordWithCustomProperties() {
    }

    public FileImportRecordWithCustomProperties(long lineNumber) {
        super(lineNumber);
    }

    private Map<CustomPropertySet, CustomPropertySetRecord> customPropertySets;

    public Map<CustomPropertySet, CustomPropertySetRecord> getCustomPropertySets() {
        return customPropertySets;
    }

    public void setCustomPropertySets(Map<CustomPropertySet, CustomPropertySetRecord> customPropertySets) {
        this.customPropertySets = customPropertySets;
    }
}
