package com.energyict.protocolimplv2.dlms.acud.properties;


import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

public class AcudDlmsProperties extends DlmsProperties {

    /**
     * Property indicating to read pushed compact frame
     */
    public String getLoadProfileMultiplier() {
        return getProperties().<String>getTypedProperty(AcudConfigurationSupport.PROPERTY_LP_MULTIPLIER, "1");
    }

}