package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.metering.imports.impl.usagepoint.fields.CommonField;
import com.elster.jupiter.metering.imports.impl.usagepoint.fields.FileImportField;
import com.elster.jupiter.metering.imports.impl.usagepoint.parsers.DateParser;
import com.elster.jupiter.metering.imports.impl.usagepoint.parsers.FieldParser;

import java.util.HashMap;
import java.util.Map;

public abstract class CustomPropertySetDescription {

    public Map<String, FileImportField<?>> getCustomPropertySetFields(DateParser dateParser, FileImportRecordWithCustomProperties record) {
        Map<String, FileImportField<?>> fields = new HashMap<>();
        fields.put("customPropertySetTime", CommonField
                .withParser(dateParser)
                .build());
        fields.put("customPropertySetValue",CommonField
                .withParser((FieldParser<Map<CustomPropertySet, CustomPropertySetRecord>>) value -> {throw new UnsupportedOperationException();})
                .withSetter(record::setCustomPropertySets)
                .build());
        return fields;
    }
}
