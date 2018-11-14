package com.energyict.smartmeterprotocolimpl.nta.esmr50.common;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;

import java.util.List;
import java.util.Properties;

@Deprecated
public class ESMR50Properties extends Dsmr40Properties {

    public static final String ESMR_50_HEX_PASSWORD = "HexPassword";
    public static final String FIRMWARE_UPGRADE_AUTHENTICATION_KEY = "FirmwareUpgradeAuthenticationKey";
    public static final String IGNORE_DST_STATUS_BIT = "IgnoreDstStatusBit";
    public static final String FRAME_COUNTER_LIMIT = "FrameCounterLimit";
    public static final String MASTER_KEY = "MasterKey";
    public static final String DEFAULT_KEY = "DefaultKey";
    public static final String WORKING_KEY_LABEL_PHASE1 = "WorkingKeyLabelPhase1";
    public static final String WORKING_KEY_LABEL_PHASE2 = "WorkingKeyLabelPhase2";

    public ESMR50Properties(TypedProperties properties,  PropertySpecService propertySpecService) { super(properties, propertySpecService);
    }

    public ESMR50Properties(PropertySpecService propertySpecService){
        super(propertySpecService);
    }



//    @Override todo Are optional keys used in Connexo?
//    public List<String> getOptionalKeys() {
//        List<String> optionals = super.getOptionalKeys();
//        optionals.add(ESMR_50_HEX_PASSWORD);
//        optionals.add(PROPERTY_FORCED_TO_READ_CACHE);
//        optionals.add(CumulativeCaptureTimeChannel);
//        optionals.add(FIRMWARE_UPGRADE_AUTHENTICATION_KEY);
//        optionals.add(IGNORE_DST_STATUS_BIT);
//        optionals.add(FRAME_COUNTER_LIMIT);
//        optionals.add(MASTER_KEY);
//        optionals.add(WORKING_KEY_LABEL_PHASE1);
//        optionals.add(WORKING_KEY_LABEL_PHASE2);
//
//        optionals.remove(WAKE_UP);
//        optionals.add(USE_GBT);
//        optionals.add(GBT_WINDOW_SIZE);
//        return optionals;
//    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new ESMR50SecurityProvider(getProtocolProperties());
        }
        return securityProvider;
    }

    @ProtocolProperty
    public String getHexPassword() {
        return getStringValue(ESMR_50_HEX_PASSWORD, "");
    }

    public boolean getCumulativeCaptureTimeChannel() {
        return getBooleanProperty("CumulativeCaptureTimeChannel", false);
    }

    @ProtocolProperty
    public boolean isForcedToReadCache() {
        return getBooleanProperty(PROPERTY_FORCED_TO_READ_CACHE, false);
    }

    @ProtocolProperty
    public String getFirmwareUpgradeAuthenticationKey() {
        return getStringValue(FIRMWARE_UPGRADE_AUTHENTICATION_KEY, "");
    }

    @ProtocolProperty
    public boolean getIgnoreDstStatusBit() {
        return getBooleanProperty(IGNORE_DST_STATUS_BIT, false);
    }

    @ProtocolProperty
    public int getFrameCounterLimit() {
        return getIntProperty(FRAME_COUNTER_LIMIT, 0);
    }

    @Override
    public ConformanceBlock getConformanceBlock() {
        ConformanceBlock conformanceBlock = super.getConformanceBlock();
//        conformanceBlock.setGeneralBlockTransfer(useGeneralBlockTransfer()); todo Verify if getGeneralBlockChannel is required
        conformanceBlock.setGeneralProtection(getCipheringType().equals(CipheringType.GENERAL_DEDICATED) || getCipheringType().equals(CipheringType.GENERAL_GLOBAL));
        return conformanceBlock;
    }

    public String getWorkingKeyLabelPhase1(){
        return getStringValue(WORKING_KEY_LABEL_PHASE1,"");
    }

    public String getWorkingKeyLabelPhase2(){
        return getStringValue(WORKING_KEY_LABEL_PHASE2,"");
    }
}
