/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.attributes.protocols;

import com.energyict.mdc.device.data.importers.impl.FileImportRecord;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProtocolAttributesImportRecord extends FileImportRecord {

    private Map<String, String> attributes = new LinkedHashMap<>();

    public ProtocolAttributesImportRecord() {
    }

    public ProtocolAttributesImportRecord(long lineNumber) {
        super(lineNumber);
    }

    public void addAttribute(String name, String value) {
        attributes.put(name, value);
    }
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
