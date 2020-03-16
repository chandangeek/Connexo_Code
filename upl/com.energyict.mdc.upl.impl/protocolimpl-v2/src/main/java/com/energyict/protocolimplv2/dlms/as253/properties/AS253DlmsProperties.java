package com.energyict.protocolimplv2.dlms.as253.properties;

import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

public class AS253DlmsProperties extends DlmsProperties {

    /**
     * Property indicating to read pushed compact frame
     */
    public String getLoadProfileMultiplier() {
        return getProperties().<String>getTypedProperty(AS253ConfigurationSupport.PROPERTY_LP_MULTIPLIER, "1");
    }

}