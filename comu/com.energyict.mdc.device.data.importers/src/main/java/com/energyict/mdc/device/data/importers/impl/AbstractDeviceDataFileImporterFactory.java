package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;

import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractDeviceDataFileImporterFactory implements FileImporterFactory {

    public static final String DOT = ".";
    public static final String COMMA = ",";
    public static final String SEMICOLON = ";";

    protected volatile PropertySpecService propertySpecService;
    protected volatile Thesaurus thesaurus;
    protected volatile Logger logger;

    @Override
    public String getDisplayName() {
        return thesaurus.getString(getName(), getDefaultFormat());
    }

    @Override
    public String getDisplayName(String property) {
        return thesaurus.getString(property, getPropertyDefaultFormat(property));
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
    public void validateProperties(List<FileImporterProperty> properties) {
    }

    @Override
    public void init(Logger logger) {
        this.logger = logger == null ? Logger.getLogger(this.getClass().getName()) : logger;
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
    public List<String> getRequiredProperties() {//not used
        return null;
    }

    public void setThesaurus(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataImporterMessageHandler.COMPONENT, Layer.DOMAIN);
    }

    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }
}
