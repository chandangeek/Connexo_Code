package com.energyict.mdc.tasks;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Models a {@link DeviceProtocolDialect} for usage with the RTU+Server and EIWebPlus
 */
public class EiWebPlusDialect extends AbstractDeviceProtocolDialect {

    public static final String SERVER_LOG_LEVER_PROPERTY = "ServerLogLevel";
    public static final String PORT_LOG_LEVEL_PROPERTY = "PortLogLevel";
    public static final String DEFAULT_LOG_LEVEL = "INFO";
    private final PropertySpecService propertySpecService;

    public EiWebPlusDialect(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.EIWEBPLUS_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "EIWebPlus dialect";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
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
        return this.stringSpec(SERVER_LOG_LEVER_PROPERTY, DEFAULT_LOG_LEVEL, getPossibleLogValues());
    }

    protected PropertySpec portLogLevelPropertySpec() {
        return this.stringSpec(PORT_LOG_LEVEL_PROPERTY, DEFAULT_LOG_LEVEL, getPossibleLogValues());
    }

    private PropertySpec stringSpec(String name, String defaultValue, String... otherValues) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, this.propertySpecService::stringSpec)
                .setDefaultValue(defaultValue)
                .addValues(otherValues)
                .markExhaustive()
                .finish();
    }

}
