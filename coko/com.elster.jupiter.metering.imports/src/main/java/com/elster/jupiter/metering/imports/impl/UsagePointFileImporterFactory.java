package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.fileimport.*;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.metering.imports.impl.usagepointfileimporterfactory", service = {FileImporterFactory.class}, immediate = true, property = {})
public class UsagePointFileImporterFactory extends FileImporterAbstractFactory {

    static final String NAME = "UsagePointFileImporterFactory";
    static final String APP_NAME = "MDC";

    private volatile Clock clock;
    private volatile MeteringService meteringService;
    private volatile FileImportService fileImportService;

    public UsagePointFileImporterFactory() {
    }

    @Override
    protected void init() {
    }

    @Inject
    public UsagePointFileImporterFactory(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties, FileImportService fileImportService, MeteringService meteringService, Clock clock) {
        super(thesaurus, propertySpecService, properties);
        this.fileImportService = fileImportService;
        this.meteringService = meteringService;
        this.clock = clock;
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        FileImporter fileImporter = new UsagePointFileImporter(getThesaurus(), getMeteringService(), getClock());
        return fileImporter;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDestinationName() {
        return UsagePointFileImporterMessageHandler.DESTINATION_NAME;
    }

    @Override
    public String getApplicationName() {
        return APP_NAME;
    }

    @Override
    public void validateProperties(List<FileImporterProperty> properties) {
        for (FileImporterProperty property : properties) {
        }
    }

    @Override
    public String getDefaultFormat() {
        return "";
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        return "";
    }

    @Override
    public List<String> getRequiredProperties() {
        return getPropertySpecs().stream().filter(p -> p.isRequired()).map(PropertySpec::getName).collect(Collectors.toList());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        return propertySpecs;
    }

    public Clock getClock() {
        return clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        super.setPropertySpecService(propertySpecService);
    }

    public MeteringService getMeteringService() {
        return meteringService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setThesaurus(NlsService nlsService) {
        super.setThesaurus(nlsService);
    }
}