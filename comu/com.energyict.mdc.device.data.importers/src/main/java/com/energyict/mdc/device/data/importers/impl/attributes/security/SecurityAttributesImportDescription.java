/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.attributes.security;

import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.fields.CommonField;
import com.elster.jupiter.fileimport.csvimport.fields.FieldSetter;
import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SecurityAttributesImportDescription implements FileImportDescription<SecurityAttributesImportRecord> {
    private LiteralStringParser stringParser = new LiteralStringParser();

    @Override
    public SecurityAttributesImportRecord getFileImportRecord() {
        return new SecurityAttributesImportRecord();
    }

    @Override
    public Map<String, FileImportField<?>> getFields(SecurityAttributesImportRecord record) {
        Map<String, FileImportField<?>> fields = new LinkedHashMap<>();
        // Device mRID or name
        fields.put("deviceIdentifier", CommonField.withParser(stringParser)
                .withName("Device Identifier")
                .withSetter(record::setDeviceIdentifier)
                .markMandatory()
                .build());
        // Security settings name
        fields.put("securitySettingsName", CommonField.withParser(stringParser)
                .withName("Security settings name")
                .withSetter(record::setSecuritySettingsName)
                .markMandatory()
                .build());
        // Security attributes
        fields.put("attribute", CommonField.withParser(stringParser)
                .withSetter(new FieldSetter<String>() {
                    @Override
                    public void setField(String value) {
                    }

                    @Override
                    public void setFieldWithHeader(String header, String value) {
                        record.addSecurityAttribute(header, value);
                    }
                })
                .markRepetitive()
                .build());
        return fields;
    }

    @Override
    public Map<Class, FieldParser> getParsers() {
        return Collections.singletonMap(String.class, stringParser);
    }
}
