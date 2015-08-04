package com.energyict.mdc.device.data.importers.impl.attributes.security;

import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.fields.CommonField;
import com.energyict.mdc.device.data.importers.impl.fields.FieldSetter;
import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;

import java.util.ArrayList;
import java.util.List;

public class SecurityAttributesImportDescription implements FileImportDescription<SecurityAttributesImportRecord> {

    @Override
    public SecurityAttributesImportRecord getFileImportRecord() {
        return new SecurityAttributesImportRecord();
    }

    public List<FileImportField<?>> getFields(SecurityAttributesImportRecord record) {
        List<FileImportField<?>> fields = new ArrayList<>();
        LiteralStringParser stringParser = new LiteralStringParser();
        //Device MRID
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setDeviceMRID)
                .markMandatory()
                .build());
        //Security settings name
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setSecuritySettingsName)
                .markMandatory()
                .build());
        //Security attributes
        fields.add(CommonField.withParser(stringParser)
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
}
