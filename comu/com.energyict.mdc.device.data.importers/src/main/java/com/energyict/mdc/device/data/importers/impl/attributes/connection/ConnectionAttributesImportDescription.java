package com.energyict.mdc.device.data.importers.impl.attributes.connection;

import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.fields.CommonField;
import com.energyict.mdc.device.data.importers.impl.fields.FieldSetter;
import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;

import java.util.ArrayList;
import java.util.List;

public class ConnectionAttributesImportDescription implements FileImportDescription<ConnectionAttributesImportRecord> {

    @Override
    public ConnectionAttributesImportRecord getFileImportRecord() {
        return new ConnectionAttributesImportRecord();
    }

    public List<FileImportField<?>> getFields(ConnectionAttributesImportRecord record) {
        List<FileImportField<?>> fields = new ArrayList<>();
        LiteralStringParser stringParser = new LiteralStringParser();
        //Device MRID
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setDeviceMRID)
                .markMandatory()
                .build());
        //Connection method name
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setConnectionMethodName)
                .markMandatory()
                .build());
        //Connection attributes
        fields.add(CommonField.withParser(stringParser)
                .withSetter(new FieldSetter<String>() {
                    @Override
                    public void setField(String value) {
                    }

                    @Override
                    public void setFieldWithHeader(String header, String value) {
                        record.addConnectionAttribute(header, value);
                    }
                })
                .markRepetitive()
                .build());
        return fields;
    }
}
