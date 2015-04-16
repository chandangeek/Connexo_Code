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
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.streams.FancyJoiner;
import com.elster.jupiter.validation.ValidationService;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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

    //OSGI
    public StandardCsvDataProcessorFactory() {
    }

    // Tests
    @Inject
    public StandardCsvDataProcessorFactory(PropertySpecService propertySpecService, DataExportService dataExportService, ValidationService validationService, AppService appService, NlsService nlsService) {
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
    public List<PropertySpec> getProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(propertySpecService.stringPropertySpec(FormatterProperties.FILENAME_PREFIX.getKey(), true, null));
        propertySpecs.add(propertySpecService.stringPropertySpec(FormatterProperties.FILE_EXTENSION.getKey(), false, "csv"));
        propertySpecs.add(propertySpecService.stringPropertySpecWithValues(FormatterProperties.SEPARATOR.getKey(), true, "comma", "semicolon"));
        propertySpecs.add(propertySpecService.stringPropertySpec(FormatterProperties.FILE_PATH.getKey(), false, null));
        return propertySpecs;
    }

    @Override
    public DataProcessor createDataFormatter(Map<String, Object> properties) {
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
        String prefix = null;
        String extension = null;
        String path = null;
        for (DataExportProperty property : properties) {
            String stringValue = (String) property.getValue();
            if (property.getName().equals(FormatterProperties.FILENAME_PREFIX.getKey())) {
                 prefix = stringValue;
                checkInvalidChars(stringValue, FormatterProperties.FILENAME_PREFIX.getKey(), NON_PATH_INVALID);
                checkIsRelativeChildPath(stringValue, FormatterProperties.FILENAME_PREFIX.getKey());
            } else if (property.getName().equals(FormatterProperties.FILE_EXTENSION.getKey())) {
                extension = Checks.is(stringValue).emptyOrOnlyWhiteSpace()?"csv":stringValue;
                checkInvalidChars(stringValue, FormatterProperties.FILE_EXTENSION.getKey(), NON_PATH_INVALID);
            } else if (property.getName().equals(FormatterProperties.FILE_PATH.getKey())) {
                path = Checks.is(stringValue).emptyOrOnlyWhiteSpace()?".":stringValue;
                checkInvalidChars(stringValue, FormatterProperties.FILE_PATH.getKey(), PATH_INVALID);
                checkIsRelativeChildPath(stringValue, FormatterProperties.FILE_PATH.getKey());
            }
        }
        checkIsRelativeChildPath(path + File.separatorChar+prefix+"A"+extension, FormatterProperties.FILE_EXTENSION.getKey());
    }

    private void checkIsRelativeChildPath(String value, String fieldName) {
        Path path = FileSystems.getDefault().getPath(value);
        if (path.isAbsolute()) {
            throw new LocalizedFieldValidationException(MessageSeeds.ABSOLUTE_PATH_NOT_ALLOWED, "properties." + fieldName);
        }
        if (!resolvesToSubDirectory(path)) {
            throw new LocalizedFieldValidationException(MessageSeeds.PARENT_BREAKING_PATH_NOT_ALLOWED, "properties." + fieldName);
        }
    }

    private boolean resolvesToSubDirectory(Path path) {
        // construct imaginary root that can not be part of the given path
        Path root = Paths.get("/A");
        Path normalize = root.resolve(path).normalize();

        return normalize.toString().startsWith(root.toString());
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
