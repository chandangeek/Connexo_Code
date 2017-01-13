package com.energyict.smartmeterprotocolimpl.nta.dsmr40;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23Properties;

import java.util.List;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 1-sep-2011
 * Time: 11:21:22
 */
public class Dsmr40Properties extends Dsmr23Properties {

    public static final String DSMR_40_HEX_PASSWORD = "HexPassword";
    public static final String PROPERTY_FORCED_TO_READ_CACHE = "ForcedToReadCache";
    private static final String CumulativeCaptureTimeChannel = "CumulativeCaptureTimeChannel";

    public Dsmr40Properties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public Dsmr40Properties(Properties properties, PropertySpecService propertySpecService) {
        super(properties, propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(UPLPropertySpecFactory.specBuilder(DSMR_40_HEX_PASSWORD, false, this.getPropertySpecService()::stringSpec).finish());
        propertySpecs.add(UPLPropertySpecFactory.specBuilder(PROPERTY_FORCED_TO_READ_CACHE, false, this.getPropertySpecService()::integerSpec).finish());
        propertySpecs.add(UPLPropertySpecFactory.specBuilder(CumulativeCaptureTimeChannel, false, this.getPropertySpecService()::integerSpec).finish());
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