package com.energyict.protocolimplv2.dialects;

import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.properties.PropertySpecService;

/**
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 8:50
 */
public abstract class AbstractDeviceProtocolDialect implements DeviceProtocolDialect {

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    public AbstractDeviceProtocolDialect(PropertySpecService propertySpecService, NlsService nlsService) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = nlsService.getThesaurus(com.energyict.protocolimplv2.messages.nls.Thesaurus.ID.toString());
    }

    public AbstractDeviceProtocolDialect(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }
}