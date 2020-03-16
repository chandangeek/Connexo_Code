package com.energyict.protocolimplv2.dlms.as253.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.a2.properties.A2ConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.util.List;

public class AS253ConfigurationSupport extends DlmsConfigurationSupport {

    public static final String PROPERTY_LP_MULTIPLIER = "ApplyLoadProfileMultiplier";

    public AS253ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(loadProfilerMultiplierPropertySpec());
        return propertySpecs;
    }

    protected PropertySpec loadProfilerMultiplierPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PROPERTY_LP_MULTIPLIER, false, PropertyTranslationKeys.V2_PUSHING_COMPACT_FRAMES, getPropertySpecService()::stringSpec).finish();
    }
}
