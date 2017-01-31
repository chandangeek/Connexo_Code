/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataFormatter;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.streams.FancyJoiner;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

abstract class StandardCsvDataFormatterFactory implements DataFormatterFactory {

    private static final String NON_PATH_INVALID = "\":*?<>|";

    private volatile PropertySpecService propertySpecService;
    private volatile DataExportService dataExportService;
    private volatile Thesaurus thesaurus;

    //OSGI
    public StandardCsvDataFormatterFactory() {
    }

    // Tests
    @Inject
    public StandardCsvDataFormatterFactory(PropertySpecService propertySpecService, DataExportService dataExportService, NlsService nlsService) {
        this();
        setPropertySpecService(propertySpecService);
        setDataExportService(dataExportService);
        setThesaurus(nlsService);
    }

    void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    void setThesaurus(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DataExportService.COMPONENTNAME, Layer.REST);
    }

    void setDataExportService(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }

    PropertySpec getTagProperty() {
        return propertySpecService
                .stringSpec()
                .named(FormatterProperties.TAG)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish();
    }

    PropertySpec getUpdateTagProperty() {
        return propertySpecService
                .stringSpec()
                .named(FormatterProperties.UPDATE_TAG)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish();
    }

    PropertySpec getSeparatorProperty() {
        Stream<TranslatablePropertyValueInfo> separatorValues =
                FormatterProperties
                        .separatorValues()
                        .stream()
                        .map(this::asInfo);
        return propertySpecService
                .specForValuesOf(new TranslatablePropertyValueInfoFactory(thesaurus))
                .named(FormatterProperties.SEPARATOR)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .addValues(separatorValues.toArray(TranslatablePropertyValueInfo[]::new))
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .setDefaultValue(this.asInfo(FormatterProperties.defaultSeparator()))
                .finish();
    }

    private TranslatablePropertyValueInfo asInfo(FormatterProperties property) {
        return new TranslatablePropertyValueInfo(property.getKey(), thesaurus.getFormat(property).format());
    }

    @Override
    public DataFormatter createDataFormatter(Map<String, Object> properties) {
        return new StandardCsvDataFormatter(properties, dataExportService);
    }

    @Override
    public void validateProperties(List<DataExportProperty> properties) {
        for (DataExportProperty property : properties) {
            if (property.getValue() instanceof TranslatablePropertyValueInfo) {
                TranslatablePropertyValueInfo translatablePropertyValueInfo = (TranslatablePropertyValueInfo) property.getValue();
                checkInvalidChars(translatablePropertyValueInfo.getId().toString(), property.getName(), NON_PATH_INVALID);
            } else {
                String stringValue = (String) property.getValue();
                checkInvalidChars(stringValue, property.getName(), NON_PATH_INVALID);
            }
        }
    }

    protected void checkInvalidChars(String value, String fieldName, String invalidCharacters) {
        for (int i = 0; i < invalidCharacters.length(); i++) {
            char invalidChar = invalidCharacters.charAt(i);
            if (value.indexOf(invalidChar) != -1) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALIDCHARS_EXCEPTION, "properties." + fieldName, asString(invalidCharacters));
            }
        }
    }

    private String asString(String invalidCharacters) {
        String and = ' ' + thesaurus.getFormat(Translations.Labels.AND).format() + ' ';
        return Pattern.compile("").splitAsStream(invalidCharacters).collect(FancyJoiner.joining(", ", and));
    }
}