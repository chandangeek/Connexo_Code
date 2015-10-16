package com.elster.jupiter.prepayment.export.redknee.impl;

import com.elster.jupiter.export.*;
import com.elster.jupiter.properties.PropertySpec;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.*;


@Component(name = "com.elster.jupiter.prepayment.export.redknee.ReadingDataFormatterFactory",
        property = {DataExportService.DATA_TYPE_PROPERTY + "=" + DataExportService.STANDARD_READING_DATA_TYPE},
        service = DataFormatterFactory.class,
        immediate = true)
public class ReadingDataFormatterFactory implements DataFormatterFactory {

    static final String NAME = "ReadingDataFormatterFactory";
    static final String DISPLAY_NAME = "RedKnee prepayment reading formatter";
    private static final String NON_PATH_INVALID = "\":*?<>|";

    private volatile DataExportService dataExportService;

    public ReadingDataFormatterFactory() {
    }

    @Inject
    public ReadingDataFormatterFactory(DataExportService dataExportService) {
        setDataExportService(dataExportService);
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }


    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public DataFormatter createDataFormatter(Map<String, Object> properties) {
        return new com.elster.jupiter.prepayment.export.redknee.impl.ReadingDataFormatter();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void validateProperties(List<DataExportProperty> properties) {
//        for (DataExportProperty property : properties) {
//            String stringValue = (String) property.getValue();
//            checkInvalidChars(stringValue, property.getName(), NON_PATH_INVALID);
//        }
    }

//    protected void checkInvalidChars(String value, String fieldName, String invalidCharacters) {
//        for (int i = 0; i < invalidCharacters.length(); i++) {
//            char invalidChar = invalidCharacters.charAt(i);
//            if (value.indexOf(invalidChar) != -1) {
//                throw new LocalizedFieldValidationException(MessageSeeds.INVALIDCHARS_EXCEPTION, "properties." + fieldName, asString(invalidCharacters));
//            }
//        }
//    }

//    private String asString(String invalidCharacters) {
//        String and = ' ' + Translations.Labels.AND.translate(thesaurus) + ' ';
//        return Pattern.compile("").splitAsStream(invalidCharacters).collect(FancyJoiner.joining(", ", and));
//    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }
}
