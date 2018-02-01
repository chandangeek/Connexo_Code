/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.customattributes;

import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.fields.CommonField;
import com.elster.jupiter.fileimport.csvimport.fields.FieldSetter;
import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.parsers.BigDecimalParser;
import com.energyict.mdc.device.data.importers.impl.parsers.BooleanParser;
import com.energyict.mdc.device.data.importers.impl.parsers.CustomAttributeParser;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;
import com.energyict.mdc.device.data.importers.impl.parsers.NumberParser;
import com.energyict.mdc.device.data.importers.impl.parsers.QuantityParser;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;

import com.google.common.collect.ImmutableMap;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomAttributesImportDescription implements FileImportDescription<CustomAttributesImportRecord> {

    private final DateParser dateParser;
    private final BigDecimalParser bigDecimalParser;
    private volatile DeviceDataImporterContext context;

    public CustomAttributesImportDescription(String dateFormat, String timeZone, SupportedNumberFormat numberFormat, DeviceDataImporterContext context) {
        this.dateParser = new DateParser(dateFormat, timeZone);
        this.bigDecimalParser = new BigDecimalParser(numberFormat);
        this.context = context;
    }

    @Override
    public CustomAttributesImportRecord getFileImportRecord() {
        return new CustomAttributesImportRecord();
    }

    public Map<String, FileImportField<?>> getFields(CustomAttributesImportRecord record) {
        Map<String, FileImportField<?>> fields = new LinkedHashMap<>();
        LiteralStringParser stringParser = new LiteralStringParser();
        BooleanParser booleanParser = new BooleanParser();
        QuantityParser quantityParser = new QuantityParser(bigDecimalParser, new NumberParser(), stringParser);
        CustomAttributeParser customAttributeParser = new CustomAttributeParser(context.getCustomPropertySetService(), stringParser, dateParser, quantityParser, bigDecimalParser);

        // Device mRID or name
        fields.put("deviceIdentifier", CommonField.withParser(stringParser)
                .withName("Device Identifier")
                .withSetter(record::setDeviceIdentifier)
                .markMandatory()
                .build());

        // Reading type mRID
        fields.put("readingType", CommonField.withParser(stringParser)
                .withSetter(record::setReadingType)
                .withName("Reading Type")
                .build());
        // Versioned custom attributes auto resolution
        fields.put("autoResolution", CommonField.withParser(stringParser)
                .withSetter(new FieldSetter<String>() {
                    @Override
                    public void setField(String value) {
                        record.setAutoResolution(Checks.is(value).emptyOrOnlyWhiteSpace() || booleanParser.parse(value));
                    }
                })
                .withName("Auto Resolution")
                .build());
        // Coustom attributes
        fields.put("customAttribute", CommonField.withParser(customAttributeParser)
                .withSetter(new FieldSetter<String>() {
                    @Override
                    public void setField(String value) {
                    }

                    @Override
                    public void setFieldWithHeader(String header, String value) {
                        Object parsedValue = customAttributeParser.parseCustomAttribute(header, value);
                        record.addCustomAttribute(header, parsedValue);
                    }
                })
                .markRepetitive()
                .build());
        return fields;
    }

    @Override
    public Map<Class, FieldParser> getParsers() {
        return ImmutableMap.of(
                DateParser.class, dateParser,
                BigDecimalParser.class, bigDecimalParser,
                ZonedDateTime.class, dateParser
        );
    }

    @Override
    public boolean isSkipTrailingNulls() {
        return false;
    }
}
