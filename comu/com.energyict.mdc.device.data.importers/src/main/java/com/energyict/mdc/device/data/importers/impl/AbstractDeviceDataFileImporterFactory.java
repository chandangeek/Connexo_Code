package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.properties.PropertySpec;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Set;

public abstract class AbstractDeviceDataFileImporterFactory implements FileImporterFactory {

    public static final String IMPORTER_FACTORY_PROPERTY_PREFIX = "DeviceDataFileImporterFactory";

    public static final char DOT = '.';
    public static final char COMMA = ',';
    public static final char SEMICOLON = ';';

    @Override
    public String getApplicationName() {
        return "MDC";
    }

    @Override
    public String getDestinationName() {
        return DeviceDataImporterMessageHandler.DESTINATION_NAME;
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

    protected abstract Set<DeviceDataImporterProperty> getProperties();

    protected abstract DeviceDataImporterContext getContext();

    public abstract void setDeviceDataImporterContext(DeviceDataImporterContext context);
}
