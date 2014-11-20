package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataProcessor;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 29/10/2014
 * Time: 9:41
 */
@Component(name = "com.elster.jupiter.export.processor.StandardCsvDataProcessorFactory",
        service = DataProcessorFactory.class, immediate = true)
public class StandardCsvDataProcessorFactory implements DataProcessorFactory {

    static final String NAME = "standardCsvDataProcessorFactory";

    private volatile PropertySpecService propertySpecService;

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec<?>> getProperties() {
        List<PropertySpec<?>> propertySpecs = new ArrayList<>();
        propertySpecs.add(propertySpecService.stringPropertySpec(FormatterProperties.FILENAME_PREFIX.getKey(), true, null));
        propertySpecs.add(propertySpecService.stringPropertySpec(FormatterProperties.FILE_EXTENSION.getKey(), false, "csv"));
        propertySpecs.add(propertySpecService.basicPropertySpec(FormatterProperties.UPDATE_IN_SEPARATE_FILE.getKey(), false, new BooleanFactory()));
        propertySpecs.add(propertySpecService.stringPropertySpec(FormatterProperties.UPDATE_FILE_PREFIX.getKey(), false, null));
        propertySpecs.add(propertySpecService.stringPropertySpec(FormatterProperties.UPDATE_FILE_EXTENSION.getKey(), false, "csv"));
        propertySpecs.add(propertySpecService.stringPropertySpecWithValues(FormatterProperties.SEPARATOR.getKey(), true, "comma", "semicolon"));
        return propertySpecs;
    }

    @Override
    public DataProcessor createDataFormatter(List<DataExportProperty> properties) {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void validateProperties(List<DataExportProperty> properties) {
        for (DataExportProperty property : properties) {
            if (property.getName().equals(FormatterProperties.FILENAME_PREFIX.getKey())) {
                checkInvalidChars((String) property.getValue(), FormatterProperties.FILENAME_PREFIX.getKey());
            } else if (property.getName().equals(FormatterProperties.FILE_EXTENSION.getKey())) {
                checkInvalidChars((String) property.getValue(), FormatterProperties.FILE_EXTENSION.getKey());
            } else if (property.getName().equals(FormatterProperties.UPDATE_FILE_PREFIX.getKey())) {
                checkInvalidChars((String) property.getValue(), FormatterProperties.UPDATE_FILE_PREFIX.getKey());
            } else if (property.getName().equals(FormatterProperties.UPDATE_FILE_EXTENSION.getKey())) {
                checkInvalidChars((String) property.getValue(), FormatterProperties.UPDATE_FILE_EXTENSION.getKey());
            }
        }
    }

    protected void checkInvalidChars(String value, String fieldName) {
        String invalidChars = "\":*?<>|";
        for (int i = 0; i < invalidChars.length(); i++) {
            char invalidChar = invalidChars.charAt(i);
            if (value.indexOf(invalidChar) != -1) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALIDCHARS_EXCEPTION, "properties." + fieldName);
            }
        }
    }

}
