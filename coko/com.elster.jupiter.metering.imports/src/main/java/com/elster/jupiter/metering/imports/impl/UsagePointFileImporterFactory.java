package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(name = "com.elster.jupiter.metering.imports.impl.usagepointfileimporterfactory", service = {FileImporterFactory.class}, immediate = true, property = {})
public class UsagePointFileImporterFactory implements FileImporterFactory {

    static final String NAME = "UsagePointFileImporterFactory";
    static final String APP_NAME = "MDC";

    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile PropertySpecService propertySpecService;
    private volatile FileImportService fileImportService;
    private volatile Map<String, UsagePointParser> parsers = new HashMap<>();

    public UsagePointFileImporterFactory() {
    }

    @Inject
    public UsagePointFileImporterFactory(Thesaurus thesaurus, PropertySpecService propertySpecService, FileImportService fileImportService, MeteringService meteringService, Clock clock) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.fileImportService = fileImportService;
        this.meteringService = meteringService;
        this.clock = clock;
    }

    @Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, unbind = "unbind", policy = ReferencePolicy.DYNAMIC)
    protected void bind(UsagePointParser usagePointParser) {
        for (String extensionName : usagePointParser.getParserFormatExtensionName()) {
            parsers.put(extensionName, usagePointParser);
        }
    }

    protected void unbind(UsagePointParser usagePointParser) {
        usagePointParser.getParserFormatExtensionName().stream().filter(extensionName -> parsers.get(extensionName).equals(usagePointParser)).forEach(parsers::remove);
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        return new UsagePointFileImporter(thesaurus, getMeteringService(), getParsers(), getDataProcessor());
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
    public String getDisplayName() {
        return thesaurus.getString(NAME, NAME);
    }

    @Override
    public void validateProperties(List<FileImporterProperty> properties) {
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
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
        this.propertySpecService = propertySpecService;
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
        this.thesaurus = nlsService.getThesaurus(FileImportService.COMPONENT_NAME, Layer.REST);
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    public Map<String, UsagePointParser> getParsers() {
        return parsers;
    }

    public UsagePointProcessor getDataProcessor() {
        return new UsagePointProcessor(getClock(), getThesaurus(), getMeteringService());
    }
}