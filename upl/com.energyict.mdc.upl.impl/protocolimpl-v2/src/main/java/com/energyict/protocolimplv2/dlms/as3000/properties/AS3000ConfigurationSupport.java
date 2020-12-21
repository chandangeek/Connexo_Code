package com.energyict.protocolimplv2.dlms.as3000.properties;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.util.List;

public class AS3000ConfigurationSupport extends DlmsConfigurationSupport {

    public static final String USE_CACHED_FRAME_COUNTER = "UseCachedFrameCounter";
    public static final String LIMIT_MAX_NR_OF_DAYS_PROPERTY = "MaxDaysLoadProfileRead";

    public AS3000ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> uplPropertySpecs = super.getUPLPropertySpecs();
        uplPropertySpecs.add(useCachedFrameCounter());
        uplPropertySpecs.add(flushCache());
        uplPropertySpecs.add(frameInboundTimeout());
        uplPropertySpecs.add(maxDaysLoadProfileRead());
        return uplPropertySpecs;
    }

    private PropertySpec useCachedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(USE_CACHED_FRAME_COUNTER, false, PropertyTranslationKeys.V2_USE_CACHED_FRAME_COUNTER, this.getPropertySpecService()::booleanSpec).finish();
    }

    private PropertySpec flushCache() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.READCACHE_PROPERTY, false, PropertyTranslationKeys.V2_EICT_READCACHE, this.getPropertySpecService()::booleanSpec).finish();
    }

    private PropertySpec frameInboundTimeout() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.TIMEOUT, false, PropertyTranslationKeys.V2_INBOUND_TIMEOUT, this.getPropertySpecService()::durationSpec).finish();
    }

    private PropertySpec maxDaysLoadProfileRead() {
        return UPLPropertySpecFactory.specBuilder(LIMIT_MAX_NR_OF_DAYS_PROPERTY, false, PropertyTranslationKeys.V2_DLMS_LIMIT_MAX_NR_OF_DAYS, this.getPropertySpecService()::integerSpec).finish();
    }

}
