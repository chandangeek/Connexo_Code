package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.metering.imports.impl.usagepoint.exceptions.ValueParserException;
import com.elster.jupiter.metering.imports.impl.usagepoint.fields.CommonField;
import com.elster.jupiter.metering.imports.impl.usagepoint.fields.FileImportField;
import com.elster.jupiter.metering.imports.impl.usagepoint.parsers.DateParser;

import java.util.HashMap;
import java.util.Map;

public abstract class CustomPropertySetDescription {

    public Map<String, FileImportField<?>> getCustomPropertySetFields(DateParser dateParser, FileImportRecordWithCustomProperties record) {
        Map<String, FileImportField<?>> fields = new HashMap<>();
        fields.put("customPropertySetTime", CommonField
                .withParser(dateParser)
                .build());
        fields.put("customPropertySetValue",CommonField
                .withParser(new FieldParser<Map<CustomPropertySet, CustomPropertySetRecord>>() {
                    @Override
                    public Class<Map<CustomPropertySet, CustomPropertySetRecord>> getValueType() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Map<CustomPropertySet, CustomPropertySetRecord> parse(String value) throws
                            ValueParserException {
                        throw new UnsupportedOperationException();
                    }
                })
                .withSetter(record::setCustomPropertySets)
                .build());
        return fields;
    }
}
