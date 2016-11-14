package com.energyict.mdc.tasks;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Models a {@link DeviceProtocolDialect} for usage with the RTU+Server and EIWebPlus
 */
public class EiWebPlusDialect extends AbstractDeviceProtocolDialect {

    public static final String SERVER_LOG_LEVER_PROPERTY = "ServerLogLevel";
    public static final String PORT_LOG_LEVEL_PROPERTY = "PortLogLevel";
    public static final String DEFAULT_LOG_LEVEL = "INFO";

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.EIWEBPLUS_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "EIWebPlus dialect";
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(
                this.serverLogLevelPropertySpec(),
                this.portLogLevelPropertySpec()
        );
    }

    private String[] getPossibleLogValues() {
        List<String> possibleValues = new ArrayList<>();
        possibleValues.add("OFF");
        possibleValues.add("SEVERE");
        possibleValues.add("WARNING");
        possibleValues.add("INFO");
        possibleValues.add("FINE");
        possibleValues.add("FINER");
        possibleValues.add("FINEST");
        possibleValues.add("ALL");
        return possibleValues.toArray(new String[8]);
    }

    protected PropertySpec serverLogLevelPropertySpec() {
        return PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(SERVER_LOG_LEVER_PROPERTY, DEFAULT_LOG_LEVEL, getPossibleLogValues());
    }

    protected PropertySpec portLogLevelPropertySpec() {
        return PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(PORT_LOG_LEVEL_PROPERTY, DEFAULT_LOG_LEVEL, getPossibleLogValues());
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case SERVER_LOG_LEVER_PROPERTY:
                return this.serverLogLevelPropertySpec();
            case PORT_LOG_LEVEL_PROPERTY:
                return this.portLogLevelPropertySpec();
            default:
                return null;
        }
    }
}
