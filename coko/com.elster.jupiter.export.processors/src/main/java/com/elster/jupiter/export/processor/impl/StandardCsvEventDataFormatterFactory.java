package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataFormatter;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.metering.MeteringService;
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

@Component(name = "com.elster.jupiter.export.processor.StandardCsvEventDataFormatterFactory",
        property = {DataExportService.DATA_TYPE_PROPERTY + "=" + DataExportService.STANDARD_EVENT_DATA_TYPE},
        service = DataFormatterFactory.class, immediate = true)
public class StandardCsvEventDataFormatterFactory implements DataFormatterFactory {

    static final String NAME = "standardCsvEventDataProcessorFactory";
    static final String DISPLAY_NAME = "CSV formatter";
    private static final String NON_PATH_INVALID = "\":*?<>|";

    private volatile PropertySpecService propertySpecService;
    private volatile DataExportService dataExportService;
    private volatile ValidationService validationService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;

    //OSGI
    public StandardCsvEventDataFormatterFactory() {
    }

    // Tests
    @Inject
    public StandardCsvEventDataFormatterFactory(PropertySpecService propertySpecService, DataExportService dataExportService, ValidationService validationService, NlsService nlsService, MeteringService meteringService) {
        setPropertySpecService(propertySpecService);
        setDataExportService(dataExportService);
        setValidationService(validationService);
        setMeteringService(meteringService);
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
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }


    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(propertySpecService.stringPropertySpec(FormatterProperties.TAG.getKey(), true, null));
        propertySpecs.add(propertySpecService.stringPropertySpecWithValues(FormatterProperties.SEPARATOR.getKey(), true, "Comma (,)", "Semicolon (;)"));
        return propertySpecs;
    }

    @Override
    public DataFormatter createDataFormatter(Map<String, Object> properties) {
        return StandardCsvEventDataFormatter.from(dataExportService, getSeparator(properties), getTag(properties));
    }

    private String getTag(Map<String, Object> properties) {
        return (String) properties.get(FormatterProperties.TAG.getKey());
    }

    private String getSeparator(Map<String, Object> properties) {
        return (String) properties.get(FormatterProperties.SEPARATOR.getKey());
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

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }
}
