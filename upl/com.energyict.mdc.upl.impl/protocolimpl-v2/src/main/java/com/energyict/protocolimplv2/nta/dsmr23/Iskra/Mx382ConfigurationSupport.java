package com.energyict.protocolimplv2.nta.dsmr23.Iskra;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.util.List;

public class Mx382ConfigurationSupport extends DlmsConfigurationSupport {

    public static final String READCACHE_PROPERTY = "ReadCache";
    public static final String FRAME_COUNTER_LIMIT = "FrameCounterLimit";

    public Mx382ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(this.increaseFrameCounterOnHLSReply());
        propertySpecs.add(this.frameCounterLimit());
        return propertySpecs;
    }

    /**
     * Property spec indicating whether or not to increment the FC for the reply to HLS.
     *
     * @return The corresponding PropertySpec.
     */
    public PropertySpec increaseFrameCounterOnHLSReply() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS, false, PropertyTranslationKeys.V2_INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS, getPropertySpecService()::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    private PropertySpec frameCounterLimit(){
        return UPLPropertySpecFactory.specBuilder(FRAME_COUNTER_LIMIT, false, PropertyTranslationKeys.V2_NTA_FRAME_COUNTER_LIMIT,getPropertySpecService()::longSpec)
                .setDefaultValue(0L)
                .finish();
    }
}
