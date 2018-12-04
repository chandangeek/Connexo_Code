package com.energyict.protocolimplv2.nta.dsmr40;

import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.protocolimplv2.nta.dsmr23.Dsmr23Properties;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_MANUFACTURER;

/**
 * Copyrights EnergyICT
 * Date: 1-sep-2011
 * Time: 11:21:22
 */
public class Dsmr40Properties extends Dsmr23Properties {

    public static final String DSMR_40_HEX_PASSWORD = "HexPassword";
    public static final String PROPERTY_FORCED_TO_READ_CACHE = "ForcedToReadCache";
    public static final String CumulativeCaptureTimeChannel = "CumulativeCaptureTimeChannel";



    @Override
    public SecurityProvider getSecurityProvider() {
        return new Dsmr40SecurityProvider(this.getProperties(), getAuthenticationSecurityLevel());
    }


    public String getHexPassword() {
        return this.getProperties().getTypedProperty(DSMR_40_HEX_PASSWORD, DEFAULT_MANUFACTURER);
    }

    public boolean getCumulativeCaptureTimeChannel() {
        return parseBooleanProperty(CumulativeCaptureTimeChannel, false);
    }


    public boolean getForcedToReadCache() {
        return parseBooleanProperty(PROPERTY_FORCED_TO_READ_CACHE, false);
    }
}
