package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.properties.PropertySpec;
import com.google.common.collect.ImmutableList;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class AbstractDeviceDataFileImporterFactory implements FileImporterFactory {
    public static final String IMPORTER_FACTORY_PROPERTY_PREFIX = "DeviceDataFileImporterFactory";
    public static final char DOT = '.';
    public static final char COMMA = ',';
    public static final char SEMICOLON = ';';

    @Override
    public String getDisplayName() {
        return getContext().getThesaurus().getString(getName(), getDefaultFormat());
    }

    @Override
    public String getDisplayName(String property) {
        return getContext().getThesaurus().getString(property, getPropertyDefaultFormat(property));
    }

    @Override
    public String getApplicationName() {
        return "MDC";
    }

    @Override
    public String getDestinationName() {
        return DeviceDataImporterMessageHandler.DESTINATION_NAME;
    }

    @Override
    public List<String> getRequiredProperties() {
        return getProperties()
                .stream()
                .map(DeviceDataImporterProperty::getPropertyKey)
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        getProperties().stream().forEach(property -> builder.add(property.getPropertySpec(getContext())));
        return builder.build();
    }

    @Override
    public void validateProperties(List<FileImporterProperty> properties) {
        getProperties()
                .stream()
                .forEach(property -> property.validateProperties(properties, getContext()));
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        return EnumSet.of(
                TranslationKeys.DEVICE_DATA_IMPORTER_DELIMITER,
                TranslationKeys.DEVICE_DATA_IMPORTER_DATE_FORMAT,
                TranslationKeys.DEVICE_DATA_IMPORTER_TIMEZONE,
                TranslationKeys.DEVICE_DATA_IMPORTER_NUMBER_FORMAT)
                .stream()
                .filter(key -> property != null && key.equals(property))
                .findFirst()
                .map(TranslationKeys::getDefaultFormat)
                .orElse("");
    }

    @Override
    public NlsKey getNlsKey() {//not used
        return null;
    }

    @Override
    public NlsKey getPropertyNlsKey(String property) {//not used
        return null;
    }

    @Override
    public void init(Logger logger) {//not used
    }

    protected abstract Set<DeviceDataImporterProperty> getProperties();

    protected abstract DeviceDataImporterContext getContext();

    public abstract void setDeviceDataImporterContext(DeviceDataImporterContext context);
}
