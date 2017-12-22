/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.customattributes;

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

import java.util.ArrayList;
import java.util.List;

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

    public List<FileImportField<?>> getFields(CustomAttributesImportRecord record) {
        List<FileImportField<?>> fields = new ArrayList<>();
        LiteralStringParser stringParser = new LiteralStringParser();
        BooleanParser booleanParser = new BooleanParser();
        QuantityParser quantityParser = new QuantityParser(bigDecimalParser, new NumberParser(), stringParser);
        CustomAttributeParser customAttributeParser = new CustomAttributeParser(context.getCustomPropertySetService(), stringParser, dateParser, quantityParser, bigDecimalParser);

        // Device mRID or name
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setDeviceIdentifier)
                .markMandatory()
                .build());
        // Reading type mRID
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setReadingType)
                .withName("readingType")
                .build());
        // Versioned custom attributes auto resolution
        fields.add(CommonField.withParser(stringParser)
                .withSetter(new FieldSetter<String>() {
                    @Override
                    public void setField(String value) {
                        record.setAutoResolution(Checks.is(value).emptyOrOnlyWhiteSpace() || booleanParser.parse(value));
                    }
                })
                .withName("autoResolution")
                .build());
        // Coustom attributes
        fields.add(CommonField.withParser(customAttributeParser)
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
}
