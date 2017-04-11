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

    public static final String CONNECTION_MODE = "ConnectionMode";
    public static final ConnectionMode DEFAULT_CONNECTION_MODE = ConnectionMode.EXTENDED_COMMAND_LINE;
    public static final String PREVENT_CROSSING_INTERVAL_BOUNDARY_WHEN_READING_PROFILES = "PreventCrossingIntervalBoundaryWhenReadingProfiles";

    private PropertySpecService propertySpecService;

    public MK10ConfigurationSupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                connectionModePropertySpec(),
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

    private PropertySpec connectionModePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(CONNECTION_MODE, false, PropertyTranslationKeys.V2_DLMS_CONNECTION_MODE, propertySpecService::stringSpec)
                .addValues(Arrays.asList(ConnectionMode.EXTENDED_COMMAND_LINE.getName(), ConnectionMode.MINI_E_COMMAND_LINE.getName()))
                .setDefaultValue(DEFAULT_CONNECTION_MODE.getName())
                .finish();
    }

    protected PropertySpec timeZonePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.TIMEZONE, false, PropertyTranslationKeys.V2_ELSTER_TIMEZONE, propertySpecService::timeZoneSpec).finish();
    }

    public enum ConnectionMode {
        EXTENDED_COMMAND_LINE("Extended command line"),
        MINI_E_COMMAND_LINE("Mini-E command line");

        private final String name;

        ConnectionMode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static ConnectionMode fromName(String name) {
            for (ConnectionMode connectionMode : values()) {
                if (connectionMode.getName().equals(name)) {
                    return connectionMode;
                }
            }
            return ConnectionMode.EXTENDED_COMMAND_LINE;
        }
    }
}