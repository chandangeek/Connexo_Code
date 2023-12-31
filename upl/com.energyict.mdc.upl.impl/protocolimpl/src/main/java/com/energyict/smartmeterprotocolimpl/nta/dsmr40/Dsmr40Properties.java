package com.energyict.smartmeterprotocolimpl.nta.dsmr40;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
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

    public Dsmr40Properties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public Dsmr40Properties(TypedProperties properties, PropertySpecService propertySpecService) {
        super(properties, propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(UPLPropertySpecFactory.specBuilder(DSMR_40_HEX_PASSWORD, false, PropertyTranslationKeys.NTA_DSMR_40_HEX_PASSWORD, this.getPropertySpecService()::stringSpec).finish());
        propertySpecs.add(UPLPropertySpecFactory.specBuilder(PROPERTY_FORCED_TO_READ_CACHE, false, PropertyTranslationKeys.NTA_FORCED_TO_READ_CACHE, this.getPropertySpecService()::integerSpec).finish());
        propertySpecs.add(UPLPropertySpecFactory.specBuilder(CumulativeCaptureTimeChannel, false, PropertyTranslationKeys.NTA_CUMULATIVE_CAPTURE_TIME_CHANNEL, this.getPropertySpecService()::integerSpec).finish());
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
        return getBooleanProperty(CumulativeCaptureTimeChannel, false);
    }

    @ProtocolProperty
    public boolean isForcedToReadCache() {
        return getBooleanProperty(PROPERTY_FORCED_TO_READ_CACHE, false);
    }

    public int getForcedToReadCache() {
        return getIntProperty(PROPERTY_FORCED_TO_READ_CACHE, 0);
    }

}