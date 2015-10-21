package com.elster.jupiter.prepayment.export.redknee.impl;

import com.elster.jupiter.export.*;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.streams.FancyJoiner;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;


@Component(name = "com.elster.jupiter.prepayment.export.redknee.ReadingDataFormatterFactory",
        property = {DataExportService.DATA_TYPE_PROPERTY + "=" + DataExportService.STANDARD_READING_DATA_TYPE,
                    "DisplayName="+ReadingDataFormatterFactory.DISPLAY_NAME },
        service = DataFormatterFactory.class,
        immediate = true)
public class ReadingDataFormatterFactory implements DataFormatterFactory {

    public enum FieldSeparator{
        SEMICOLON("Semicolon (;)",";"),
        COMMA("Comma (,)", ";"),
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
            if (name == null)
                return PIPE.symbol;
            return Stream.of(values()).filter(x -> x.getName().equals(name)).findFirst().orElse(PIPE).getSymbol();
        }
    }

    static final String NAME = "ReadingDataFormatterFactory";
    static final String DISPLAY_NAME = "RedKnee prepayment reading formatter";
    private static final String NON_PATH_INVALID = "\":*?<>|";

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile DataExportService dataExportService;

    public ReadingDataFormatterFactory() {
    }

    @Inject
    public ReadingDataFormatterFactory(PropertySpecService propertySpecService, DataExportService dataExportService) {
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
        propertySpecs.add(propertySpecService.stringPropertySpec(FormatterProperties.TAG.getKey(), true, null));
        propertySpecs.add(propertySpecService.stringPropertySpec(FormatterProperties.UPDATE_TAG.getKey(), true, null));
        propertySpecs.add(propertySpecService.stringPropertySpecWithValues(FormatterProperties.SEPARATOR.getKey(), true, FieldSeparator.COMMA.getName(), FieldSeparator.SEMICOLON.getName(), FieldSeparator.PIPE.getName()));
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

    protected void checkInvalidChars(String value, String fieldName, String invalidCharacters) {
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
        return DISPLAY_NAME;
    }
}
