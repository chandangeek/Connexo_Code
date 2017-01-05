package com.energyict.smartmeterprotocolimpl.nta.dsmr40;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23Properties;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 1-sep-2011
 * Time: 11:21:22
 */
public class Dsmr40Properties extends Dsmr23Properties {

    public static final String DSMR_40_HEX_PASSWORD = "HexPassword";
    public static final String PROPERTY_FORCED_TO_READ_CACHE = "ForcedToReadCache";
    private static final String CumulativeCaptureTimeChannel = "CumulativeCaptureTimeChannel";

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(UPLPropertySpecFactory.string(DSMR_40_HEX_PASSWORD, false));
        propertySpecs.add(UPLPropertySpecFactory.integer(PROPERTY_FORCED_TO_READ_CACHE, false));
        propertySpecs.add(UPLPropertySpecFactory.integer(CumulativeCaptureTimeChannel, false));
        return propertySpecs;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        return new Dsmr40SecurityProvider(getProtocolProperties());
    }

    @ProtocolProperty
    public String getHexPassword() {
        return getStringValue(DSMR_40_HEX_PASSWORD, "");
    }

    public boolean getCumulativeCaptureTimeChannel() {
        return getBooleanProperty(CumulativeCaptureTimeChannel, "0");
    }

    @ProtocolProperty
    public boolean isForcedToReadCache() {
        return getBooleanProperty(PROPERTY_FORCED_TO_READ_CACHE, "0");
    }

    public int getForcedToReadCache() {
        return getIntProperty(PROPERTY_FORCED_TO_READ_CACHE, "0");
    }

}