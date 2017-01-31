/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.properties.PropertySpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractFileImporterFactory implements FileImporterFactory {

    public static final String IMPORTER_FACTORY_PROPERTY_PREFIX = "MeteringFileImporterFactory";

    public static final char DOT = '.';
    public static final char COMMA = ',';
    public static final char SEMICOLON = ';';

    @Override
    public String getApplicationName() {
        return "MDC";
    }

    @Override
    public String getDestinationName() {
        return UsagePointFileImporterMessageHandler.DESTINATION_NAME;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return getProperties()
                .stream()
                .map(property -> property.getPropertySpec(getContext()))
                .collect(Collectors.toList());
    }

    @Override
    public void validateProperties(List<FileImporterProperty> properties) {
        getProperties()
                .stream()
                .forEach(property -> property.validateProperties(properties, getContext()));
    }

    protected abstract Set<DataImporterProperty> getProperties();

    protected abstract MeteringDataImporterContext getContext();

    public abstract void setMeteringDataImporterContext(MeteringDataImporterContext context);
}
