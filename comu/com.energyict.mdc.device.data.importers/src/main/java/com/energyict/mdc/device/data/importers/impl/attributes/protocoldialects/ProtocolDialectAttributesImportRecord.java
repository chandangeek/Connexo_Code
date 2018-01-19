/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.attributes.protocoldialects;

import com.energyict.mdc.device.data.importers.impl.FileImportRecord;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProtocolDialectAttributesImportRecord extends FileImportRecord {

    private String protocolDialect;
    private Map<String, String> attributes = new LinkedHashMap<>();

    public ProtocolDialectAttributesImportRecord() {
    }

    public ProtocolDialectAttributesImportRecord(long lineNumber) {
        super(lineNumber);
    }

    public void addAttribute(String name, String value) {
        attributes.put(name, value);
    }
    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getProtocolDialect() {
        return protocolDialect;
    }

    public void setProtocolDialect(String protocolDialect) {
        this.protocolDialect = protocolDialect;
    }
}
