package com.energyict.protocolimplv2.dlms.ei7.properties;

import com.energyict.protocolimplv2.dlms.a2.properties.A2Properties;

public class EI7InboundDlmsProperties extends A2Properties {

    /**
     * Property indicating to read pushed compact frame
     */
    public boolean isPushingCompactFrames() {
        return getProperties().<Boolean>getTypedProperty(EI7InboundConfigurationSupport.PUSHING_COMPACT_FRAMES, false);
    }

}