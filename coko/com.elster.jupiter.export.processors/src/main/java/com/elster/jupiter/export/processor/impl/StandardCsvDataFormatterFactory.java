package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.appserver.AppService;
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
import com.elster.jupiter.validation.ValidationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component(name = "com.elster.jupiter.export.processor.StandardCsvDataProcessorFactory",
        property = { DataExportService.DATA_TYPE_PROPERTY + "="+ DataExportService.STANDARD_DATA_TYPE},
        service = DataFormatterFactory.class, immediate = true)
public class StandardCsvDataFormatterFactory implements DataFormatterFactory {

    static final String NAME = "standardCsvDataProcessorFactory";
    private static final String NON_PATH_INVALID = "\":*?<>|";

    private volatile PropertySpecService propertySpecService;
    private volatile DataExportService dataExportService;
    private volatile ValidationService validationService;
    private volatile AppService appService;
    private volatile Thesaurus thesaurus;

    //OSGI
    public StandardCsvDataFormatterFactory() {
    }

    // Tests
    @Inject
    public StandardCsvDataFormatterFactory(PropertySpecService propertySpecService, DataExportService dataExportService, ValidationService validationService, AppService appService, NlsService nlsService) {
        setPropertySpecService(propertySpecService);
        setDataExportService(dataExportService);
        setValidationService(validationService);
        setAppService(appService);
        setThesaurus(nlsService);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setThesaurus(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(NAME, Layer.DOMAIN);
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(propertySpecService.stringPropertySpecWithValues(FormatterProperties.SEPARATOR.getKey(), true, "comma", "semicolon"));
        propertySpecs.add(propertySpecService.stringPropertySpec(FormatterProperties.TAG.getKey(), true, null));
        propertySpecs.add(propertySpecService.stringPropertySpec(FormatterProperties.UPDATE_TAG.getKey(), true, null));
        return propertySpecs;
    }

    @Override
    public DataFormatter createDataFormatter(Map<String, Object> properties) {
        return new StandardCsvDataFormatter(properties, thesaurus, validationService, dataExportService);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void validateProperties(List<DataExportProperty> properties) {
        for (DataExportProperty property : properties) {
            String stringValue = (String) property.getValue();
            checkInvalidChars(stringValue, property.getName(), NON_PATH_INVALID);
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
        String and = ' ' + Translations.Labels.AND.translate(thesaurus) + ' ';
        return Pattern.compile("").splitAsStream(invalidCharacters).collect(FancyJoiner.joining(", ", and));
    }

}
