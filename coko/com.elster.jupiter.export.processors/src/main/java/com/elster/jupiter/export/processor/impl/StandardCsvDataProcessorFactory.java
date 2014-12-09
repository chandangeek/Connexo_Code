package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataProcessor;
import com.elster.jupiter.export.DataProcessorFactory;
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

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Copyrights EnergyICT
 * Date: 29/10/2014
 * Time: 9:41
 */
@Component(name = "com.elster.jupiter.export.processor.StandardCsvDataProcessorFactory",
        service = DataProcessorFactory.class, immediate = true)
public class StandardCsvDataProcessorFactory implements DataProcessorFactory {

    static final String NAME = "standardCsvDataProcessorFactory";
    public static final String NON_PATH_INVALID = "\":*?<>|";
    public static final String PATH_INVALID = "\"*?<>|";

    private volatile PropertySpecService propertySpecService;
    private volatile DataExportService dataExportService;
    private volatile ValidationService validationService;
    private volatile AppService appService;
    private volatile Thesaurus thesaurus;

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
    public List<PropertySpec<?>> getProperties() {
        List<PropertySpec<?>> propertySpecs = new ArrayList<>();
        propertySpecs.add(propertySpecService.stringPropertySpec(FormatterProperties.FILENAME_PREFIX.getKey(), true, null));
        propertySpecs.add(propertySpecService.stringPropertySpec(FormatterProperties.FILE_EXTENSION.getKey(), false, "csv"));
        propertySpecs.add(propertySpecService.stringPropertySpecWithValues(FormatterProperties.SEPARATOR.getKey(), true, "comma", "semicolon"));
        propertySpecs.add(propertySpecService.stringPropertySpec(FormatterProperties.FILE_PATH.getKey(), false, null));
        return propertySpecs;
    }

    @Override
    public DataProcessor createDataFormatter(List<DataExportProperty> properties) {
        return new StandardCsvDataProcessor(dataExportService, appService, properties, thesaurus, FileSystems.getDefault(), getTempDir(), validationService);
    }

    private Path getTempDir() {
        return FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir"));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void validateProperties(List<DataExportProperty> properties) {
        for (DataExportProperty property : properties) {
            if (property.getName().equals(FormatterProperties.FILENAME_PREFIX.getKey())) {
                String string = (String) property.getValue();
                checkInvalidChars(string, FormatterProperties.FILENAME_PREFIX.getKey(), NON_PATH_INVALID);
                checkIsRelativePath(string, FormatterProperties.FILENAME_PREFIX.getKey());
            } else if (property.getName().equals(FormatterProperties.FILE_EXTENSION.getKey())) {
                checkInvalidChars((String) property.getValue(), FormatterProperties.FILE_EXTENSION.getKey(), NON_PATH_INVALID);
            } else if (property.getName().equals(FormatterProperties.FILE_PATH.getKey())) {
                checkInvalidChars((String) property.getValue(), FormatterProperties.FILE_PATH.getKey(), PATH_INVALID);
            }
        }
    }

    private void checkIsRelativePath(String value, String fieldName) {
        Path path = FileSystems.getDefault().getPath(value);
        if (path.isAbsolute()) {
            throw new LocalizedFieldValidationException(MessageSeeds.ABSOLUTE_PATH_NOT_ALLOWED, "properties." + fieldName);
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
