package com.energyict.protocolimplv2.edmi.mk10.properties;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.MeterProtocol;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author sva
 * @since 22/02/2017 - 16:08
 */
public class MK10ConfigurationSupport implements HasDynamicProperties {

    public static final String PREVENT_CROSSING_INTERVAL_BOUNDARY_WHEN_READING_PROFILES = "PreventCrossingIntervalBoundaryWhenReadingProfiles";

    private final PropertySpecService propertySpecService;

    public MK10ConfigurationSupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                preventCrossingIntervalBoundaryWhenReadingPropertySpec(),
                timeZonePropertySpec(),
                deviceIdPropertySpec(),
                callHomeIdPropertySpec()
        );
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        // currently I don't hold any properties
    }

    private PropertySpec preventCrossingIntervalBoundaryWhenReadingPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PREVENT_CROSSING_INTERVAL_BOUNDARY_WHEN_READING_PROFILES, false, PropertyTranslationKeys.PREVENT_CROSSING_INTERVAL_BOUNDARY_WHEN_READING_PROFILES, propertySpecService::booleanSpec).finish();
    }

    private PropertySpec deviceIdPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(MeterProtocol.Property.ADDRESS.getName(), false, PropertyTranslationKeys.V2_ELSTER_DEVICE_ID, propertySpecService::stringSpec).finish();
    }

    private PropertySpec callHomeIdPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, false, PropertyTranslationKeys.V2_ELSTER_CALL_HOME_ID, propertySpecService::stringSpec).finish();
    }

    protected PropertySpec timeZonePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.TIMEZONE, false, PropertyTranslationKeys.V2_ELSTER_TIMEZONE, propertySpecService::timeZoneSpec).finish();
    }

}