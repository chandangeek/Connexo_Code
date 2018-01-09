/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.attributes.protocoldialects;

import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.fields.CommonField;
import com.elster.jupiter.fileimport.csvimport.fields.FieldSetter;
import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProtocolDialectAttributesImportDescription implements FileImportDescription<ProtocolDialectAttributesImportRecord> {

    private LiteralStringParser stringParser = new LiteralStringParser();

    @Override
    public ProtocolDialectAttributesImportRecord getFileImportRecord() {
        return new ProtocolDialectAttributesImportRecord();
    }

    public Map<String, FileImportField<?>> getFields(ProtocolDialectAttributesImportRecord record) {
        Map<String, FileImportField<?>> fields = new LinkedHashMap<>();

        // Device mRID or name
        fields.put("deviceIdentifier", CommonField.withParser(stringParser)
                .withSetter(record::setDeviceIdentifier)
                .withName("Device Identifier")
                .markMandatory()
                .build());
        // Device protocol dialect
        fields.put("protocolDialect", CommonField.withParser(stringParser)
                .withSetter(record::setProtocolDialect)
                .withName("Protocol Dialect")
                .markMandatory()
                .build());
        // Attributes
        fields.put("attribute", CommonField.withParser(stringParser)
                .withSetter(new FieldSetter<String>() {
                    @Override
                    public void setField(String value) {
                    }

                    @Override
                    public void setFieldWithHeader(String header, String value) {
                        record.addAttribute(header, value);
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
