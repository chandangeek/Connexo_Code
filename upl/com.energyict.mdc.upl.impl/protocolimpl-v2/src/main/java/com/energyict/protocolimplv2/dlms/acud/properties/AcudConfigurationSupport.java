package com.energyict.protocolimplv2.dlms.acud.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.a2.properties.A2ConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.util.List;

public class AcudConfigurationSupport extends DlmsConfigurationSupport {

    public AcudConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

}
