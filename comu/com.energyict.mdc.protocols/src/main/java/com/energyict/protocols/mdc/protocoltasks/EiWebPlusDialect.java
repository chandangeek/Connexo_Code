package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimplv2.DeviceProtocolDialectName;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Models a DeviceProtocolDialect for usage with the RTU+Server and EIWebPlus
 */
public class EiWebPlusDialect extends AbstractDeviceProtocolDialect {

    public static final String SERVER_LOG_LEVER_PROPERTY = "ServerLogLevel";
    public static final String PORT_LOG_LEVEL_PROPERTY = "PortLogLevel";
    public static final String DEFAULT_LOG_LEVEL = "INFO";

    public EiWebPlusDialect(PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectName.EIWEBPLUS.getName();
    }

    @Override
    public String getDisplayName() {
        return this.getThesaurus().getFormat(DeviceProtocolDialectName.EIWEBPLUS).format();
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
        return getPropertySpecService().stringPropertySpecWithValuesAndDefaultValue(SERVER_LOG_LEVER_PROPERTY, false, DEFAULT_LOG_LEVEL, getPossibleLogValues());
    }

    protected PropertySpec portLogLevelPropertySpec() {
        return getPropertySpecService().stringPropertySpecWithValuesAndDefaultValue(PORT_LOG_LEVEL_PROPERTY, false, DEFAULT_LOG_LEVEL, getPossibleLogValues());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(portLogLevelPropertySpec(), serverLogLevelPropertySpec());
    }

}