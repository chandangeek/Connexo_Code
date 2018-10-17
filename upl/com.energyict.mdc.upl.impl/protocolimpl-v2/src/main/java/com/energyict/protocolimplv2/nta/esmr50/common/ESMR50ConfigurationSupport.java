package com.energyict.protocolimplv2.nta.esmr50.common;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.util.List;

public class ESMR50ConfigurationSupport extends DlmsConfigurationSupport {

    public static final String ESMR_50_HEX_PASSWORD = "HexPassword";
    public static final String FIRMWARE_UPGRADE_AUTHENTICATION_KEY = "FirmwareUpgradeAuthenticationKey";
    public static final String IGNORE_DST_STATUS_BIT = "IgnoreDstStatusBit";
    public static final String FRAME_COUNTER_LIMIT = "FrameCounterLimit";
    public static final String DEFAULT_KEY = "DefaultKey";
    public static final String WORKING_KEY_LABEL_PHASE1 = "WorkingKeyLabelPhase1";
    public static final String WORKING_KEY_LABEL_PHASE2 = "WorkingKeyLabelPhase2";

    public ESMR50ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List <PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(this.esmr50HexPassword());
        propertySpecs.add(this.firmwareUpgradeAutheticationKey());
        propertySpecs.add(this.ignoreDstStatusBit());
        propertySpecs.add(this.frameCounterLimit());
        propertySpecs.add(this.defaultKey());
        propertySpecs.add(this.workingKeyLabelPhase1());
        propertySpecs.add(this.workingKeyLabelPhase2());
        return propertySpecs;
    }

    private PropertySpec esmr50HexPassword(){
        return UPLPropertySpecFactory.specBuilder(ESMR_50_HEX_PASSWORD, false, PropertyTranslationKeys.V2_NTA_ESMR_50_HEX_PASSWORD, getPropertySpecService()::stringSpec).finish();
    }

    private PropertySpec firmwareUpgradeAutheticationKey(){
        return UPLPropertySpecFactory.specBuilder(FIRMWARE_UPGRADE_AUTHENTICATION_KEY, false, PropertyTranslationKeys.V2_NTA_FIRMWARE_UPGRADE_AUTHENTICATION_KEY, getPropertySpecService()::stringSpec).finish();
    }

    private PropertySpec ignoreDstStatusBit(){
        return UPLPropertySpecFactory.specBuilder(IGNORE_DST_STATUS_BIT, false, PropertyTranslationKeys.V2_NTA_IGNORE_DST_STATUS_BIT, getPropertySpecService()::booleanSpec).finish();
    }
    private PropertySpec frameCounterLimit(){
        return UPLPropertySpecFactory.specBuilder(FRAME_COUNTER_LIMIT, false, PropertyTranslationKeys.V2_NTA_FRAME_COUNTER_LIMIT, getPropertySpecService()::integerSpec).finish();
    }

    private PropertySpec defaultKey(){
        return UPLPropertySpecFactory.specBuilder(DEFAULT_KEY, false, PropertyTranslationKeys.V2_NTA_DEFAULT_KEY, getPropertySpecService()::stringSpec).finish();
    }

    private PropertySpec workingKeyLabelPhase1(){
        return UPLPropertySpecFactory.specBuilder(WORKING_KEY_LABEL_PHASE1, false, PropertyTranslationKeys.V2_NTA_WORKING_KEY_LABEL_PHASE1, getPropertySpecService()::stringSpec).finish();
    }

    private PropertySpec workingKeyLabelPhase2(){
        return UPLPropertySpecFactory.specBuilder(WORKING_KEY_LABEL_PHASE2, false, PropertyTranslationKeys.V2_NTA_WORKING_KEY_LABEL_PHASE2, getPropertySpecService()::stringSpec).finish();
    }
}
