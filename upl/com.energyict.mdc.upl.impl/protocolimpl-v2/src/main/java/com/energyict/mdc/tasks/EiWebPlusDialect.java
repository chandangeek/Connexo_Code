package com.energyict.mdc.tasks;

import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
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

    public EiWebPlusDialect(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public EiWebPlusDialect(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.EIWEBPLUS_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.EIWEBPLUS_DIALECT_NAME).format();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
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
        return this.stringSpec(SERVER_LOG_LEVER_PROPERTY, DEFAULT_LOG_LEVEL, PropertyTranslationKeys.V2_TASKS_SERVER_LOG_LEVEL, getPossibleLogValues());
    }

    protected PropertySpec portLogLevelPropertySpec() {
        return this.stringSpec(PORT_LOG_LEVEL_PROPERTY, DEFAULT_LOG_LEVEL, PropertyTranslationKeys.V2_TASKS_PORT_LOG_LEVEL, getPossibleLogValues());
    }

    private PropertySpec stringSpec(String name, String defaultValue, TranslationKey translationKey, String... otherValues) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, getPropertySpecService()::stringSpec)
                .setDefaultValue(defaultValue)
                .addValues(otherValues)
                .markExhaustive()
                .finish();
    }

}
