package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.device.data.importers.DeviceDataImporterContext",
        service = {DeviceDataImporterContext.class})
public class DeviceDataImporterContext {
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Reference
    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }
}
