package com.energyict.protocolimplv2.dlms.actaris.sl7000.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.ADDRESSING_MODE;

public class ActarisSl7000ConfigurationSupport extends DlmsConfigurationSupport {

    public static final String USE_CACHED_FRAME_COUNTER = "UseCachedFrameCounter";
    public static final String LIMIT_MAX_NR_OF_DAYS_PROPERTY = "MaxDaysLoadProfileRead";
    public static final String USE_REGISTER_PROFILE = "UseRegisterProfile";

    public ActarisSl7000ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> uplPropertySpecs = super.getUPLPropertySpecs();
        uplPropertySpecs.add(maxDaysLoadProfileRead());
        uplPropertySpecs.add(useRegisterProfiles());
        uplPropertySpecs.add(useCachedFrameCounter());
        uplPropertySpecs.add(addressingMode());
        return uplPropertySpecs;
    }

    private PropertySpec maxDaysLoadProfileRead() {
        return UPLPropertySpecFactory.specBuilder(LIMIT_MAX_NR_OF_DAYS_PROPERTY, false, PropertyTranslationKeys.V2_DLMS_LIMIT_MAX_NR_OF_DAYS, this.getPropertySpecService()::integerSpec).finish();
    }

    private PropertySpec useRegisterProfiles() {
        return UPLPropertySpecFactory.specBuilder(USE_REGISTER_PROFILE, false, PropertyTranslationKeys.V2_USE_REGISTER_PROFILE, this.getPropertySpecService()::booleanSpec).finish();
    }

    private PropertySpec useCachedFrameCounter() {
        return UPLPropertySpecFactory.specBuilder(USE_CACHED_FRAME_COUNTER, false, PropertyTranslationKeys.V2_USE_CACHED_FRAME_COUNTER, this.getPropertySpecService()::booleanSpec).finish();
    }

    private PropertySpec addressingMode() {
        return UPLPropertySpecFactory.specBuilder(ADDRESSING_MODE, false, PropertyTranslationKeys.V2_TASKS_ADDRESSING_MODE, this.getPropertySpecService()::integerSpec).finish();
    }



}
