package com.energyict.protocolimplv2.dlms.ei7.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.a2.properties.A2ConfigurationSupport;

import java.util.List;

public class EI7InboundConfigurationSupport extends A2ConfigurationSupport {

    public static final String PUSHING_COMPACT_FRAMES = "CompactFrames";

    public EI7InboundConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(pushingCompactFramesPropertySpec());
        return propertySpecs;
    }

    protected PropertySpec pushingCompactFramesPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PUSHING_COMPACT_FRAMES, false, PropertyTranslationKeys.V2_PUSHING_COMPACT_FRAMES, getPropertySpecService()::booleanSpec).finish();
    }

}
