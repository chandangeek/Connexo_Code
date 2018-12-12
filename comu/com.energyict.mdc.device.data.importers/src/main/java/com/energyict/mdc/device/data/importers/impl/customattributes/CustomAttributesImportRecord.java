/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.customattributes;

import com.energyict.mdc.device.data.importers.impl.FileImportRecord;

import java.util.HashMap;
import java.util.Map;

public class CustomAttributesImportRecord extends FileImportRecord {

    private String readingType;
    private boolean autoResolution = true;
    private Map<String, Object> customAttributes = new HashMap<>();
    private int mandatoryColumnsCount = 0;

    public String getReadingType() {
        return readingType;
    }

    public void setReadingType(String readingType) {
        mandatoryColumnsCount++;
        this.readingType = readingType;
    }

    public boolean isAutoResolution() {
        return autoResolution;
    }

    public void setAutoResolution(boolean autoResolution) {
        mandatoryColumnsCount++;
        this.autoResolution = autoResolution;
    }

    public Map<String, Object> getCustomAttributes() {
        return customAttributes;
    }

    public void addCustomAttribute(String name, Object value) {
        customAttributes.put(name, value);
    }

    public boolean isComplete() {
        return mandatoryColumnsCount >= 2;
    }
}
