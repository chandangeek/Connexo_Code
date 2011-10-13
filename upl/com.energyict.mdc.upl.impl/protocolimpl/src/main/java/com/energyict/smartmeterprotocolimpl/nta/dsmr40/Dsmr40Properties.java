package com.energyict.smartmeterprotocolimpl.nta.dsmr40;

import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23Properties;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 1-sep-2011
 * Time: 11:21:22
 */
public class Dsmr40Properties extends Dsmr23Properties {

    public static final String Dsmr40HexPassword = "HexPassword";
    public static final String PropertyForcedToReadCache = "ForcedToReadCache";

    @Override
    public List<String> getOptionalKeys() {
        List<String> optionals = super.getOptionalKeys();
        optionals.add(Dsmr40HexPassword);
        optionals.add(PropertyForcedToReadCache);
        return optionals;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        return new Dsmr40SecurityProvider(getProtocolProperties());
    }

    @ProtocolProperty
    public String getHexPassword(){
        return getStringValue(Dsmr40HexPassword, "");
    }

    @ProtocolProperty
    public boolean getForcedToReadCache(){
        return getBooleanProperty(PropertyForcedToReadCache, "0");
    }
}
