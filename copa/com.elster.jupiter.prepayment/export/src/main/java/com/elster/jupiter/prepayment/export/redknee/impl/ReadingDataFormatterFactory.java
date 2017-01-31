/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.export.redknee.impl;

import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataFormatter;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.streams.FancyJoiner;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.prepayment.export.redknee.ReadingDataFormatterFactory",
        property = {DataExportService.DATA_TYPE_PROPERTY + "=" + DataExportService.STANDARD_READING_DATA_TYPE,
                    "DisplayName=" + ReadingDataFormatterFactory.DISPLAY_NAME },
        service = DataFormatterFactory.class,
        immediate = true)
public class ReadingDataFormatterFactory implements DataFormatterFactory {

    public enum FieldSeparator{
        SEMICOLON("Semicolon (;)",";"),
        COMMA("Comma (,)", ","),
        PIPE("Pipe (|)", "\u007C");

        private String name;
        private String symbol;

        FieldSeparator(String name, String symbol){
           this.name = name;
           this.symbol = symbol;
        }

        public String getName() {
            return name;
        }

        public String getSymbol() {
            return symbol;
        }

        // If name not found default (PIPE) will be used;
        static String separatorForName(String name){
            if (name == null) {
                return PIPE.symbol;
            }
            return Stream.of(values()).filter(x -> x.getName().equals(name)).findFirst().orElse(PIPE).getSymbol();
        }
    }

    static final String NAME = "ReadingDataFormatterFactory";
    static final String DISPLAY_NAME = "Prepayment specific formatter";
    private static final String NON_PATH_INVALID = "\":*?<>|";

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile DataExportService dataExportService;

    public ReadingDataFormatterFactory() {
    }

    @Inject
    public ReadingDataFormatterFactory(PropertySpecService propertySpecService, DataExportService dataExportService) {
        this();
        this.propertySpecService = propertySpecService;
        this.dataExportService = dataExportService;
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setThesaurus(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(NAME, Layer.DOMAIN);
    }

    @Override
    public List<PropertySpec> getPropertySpecs(){
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(
                this.propertySpecService
                        .stringSpec()
                        .named(FormatterProperties.TAG)
                        .fromThesaurus(this.thesaurus)
                        .markRequired()
                        .finish());
        propertySpecs.add(
                this.propertySpecService
                        .stringSpec()
                        .named(FormatterProperties.UPDATE_TAG)
                        .fromThesaurus(this.thesaurus)
                        .markRequired()
                        .finish());
        propertySpecs.add(
                this.propertySpecService
                        .stringSpec()
                        .named(FormatterProperties.SEPARATOR)
                        .fromThesaurus(this.thesaurus)
                        .markRequired()
                        .setDefaultValue(FieldSeparator.COMMA.getName())
                        .addValues(
                                FieldSeparator.SEMICOLON.getName(),
                                FieldSeparator.PIPE.getName())
                        .markExhaustive()
                        .finish());
        return propertySpecs;
    }

    @Override
    public DataFormatter createDataFormatter(Map<String, Object> properties) {
        return new com.elster.jupiter.prepayment.export.redknee.impl.ReadingDataFormatter(dataExportService, thesaurus, properties );
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void validateProperties(List<DataExportProperty> properties) {
        properties.stream().filter(x -> !x.getName().equals(FormatterProperties.SEPARATOR.getKey())).forEach(
                x -> checkInvalidChars((String) x.getValue(), x.getName(), NON_PATH_INVALID));
    }

    private void checkInvalidChars(String value, String fieldName, String invalidCharacters) {
        for (int i = 0; i < invalidCharacters.length(); i++) {
            char invalidChar = invalidCharacters.charAt(i);
            if (value.indexOf(invalidChar) != -1) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALIDCHARS_EXCEPTION, "properties." + fieldName, asString(invalidCharacters));
            }
        }
    }

    private String asString(String invalidCharacters) {
        String and = ' ' + Translations.Labels.AND.translate(thesaurus) + ' ';
        return Pattern.compile("").splitAsStream(invalidCharacters).collect(FancyJoiner.joining(", ", and));
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(Translations.Labels.CSV_FORMATTER).format();
    }
}
