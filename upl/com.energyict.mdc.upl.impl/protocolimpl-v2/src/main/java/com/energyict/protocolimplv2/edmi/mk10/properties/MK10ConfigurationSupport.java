package com.energyict.protocolimplv2.edmi.mk10.properties;

import com.energyict.mdc.protocol.LegacyProtocolProperties;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocol.MeterProtocol;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author sva
 * @since 22/02/2017 - 16:08
 */
public class MK10ConfigurationSupport implements ConfigurationSupport {

    public static final String CONNECTION_MODE = "ConnectionMode";
    public static final ConnectionMode DEFAULT_CONNECTION_MODE = ConnectionMode.EXTENDED_COMMAND_LINE;

    public MK10ConfigurationSupport() {
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(
                connectionModePropertySpec(),
                timeZonePropertySpec(),
                deviceIdPropertySpec(),
                callHomeIdPropertySpec()
        );
    }

    private PropertySpec deviceIdPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(MeterProtocol.ADDRESS);
    }

    private PropertySpec callHomeIdPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
    }

    private PropertySpec connectionModePropertySpec() {
        return PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(CONNECTION_MODE, DEFAULT_CONNECTION_MODE.getName(), ConnectionMode.EXTENDED_COMMAND_LINE.getName(), ConnectionMode.MINI_E_COMMAND_LINE
                .getName());
    }

    protected PropertySpec timeZonePropertySpec() {
        return PropertySpecFactory.timeZoneInUseReferencePropertySpec(DlmsProtocolProperties.TIMEZONE);
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