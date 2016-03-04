package com.elster.jupiter.metering.imports.impl.usagepoint;


import com.elster.jupiter.cps.CustomPropertySet;

import java.util.Map;

public abstract class FileImportRecordWithCustomProperties extends FileImportRecord {

    private Map<CustomPropertySet, CustomPropertySetRecord> customPropertySets;

    public Map<CustomPropertySet, CustomPropertySetRecord> getCustomPropertySets() {
        return customPropertySets;
    }

    public void setCustomPropertySets(Map<CustomPropertySet, CustomPropertySetRecord> customPropertySets) {
        this.customPropertySets = customPropertySets;
    }
}
