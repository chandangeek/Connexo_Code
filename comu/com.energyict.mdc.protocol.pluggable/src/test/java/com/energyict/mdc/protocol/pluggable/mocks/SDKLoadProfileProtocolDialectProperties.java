package com.energyict.mdc.protocol.pluggable.mocks;

import com.energyict.mdc.dynamic.ObisCodeValueFactory;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.elster.jupiter.properties.PropertySpec;

import java.util.ArrayList;
import java.util.List;

/**
 * A <i>set</i> of properties related to LoadProfiles.
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/02/13
 * Time: 15:46
 */
public class SDKLoadProfileProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    private final PropertySpecService propertySpecService;

    /**
     * This value holds the name of the Property that contains an ObisCode that we don't support for this session
     */
    public static final String notSupportedLoadProfileObisCodePropertyName = "NotSupportedLoadProfile";

    public SDKLoadProfileProtocolDialectProperties(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return "SDKLoadProfileDialect";
    }

    @Override
    public String getDisplayName() {
        return "SDK dialect for loadProfile testing";
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        if (name.equals(notSupportedLoadProfileObisCodePropertyName)) {
            return getNotSupportedLoadProfileObisCodePropertySpec();
        }
        return null;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(this.getNotSupportedLoadProfileObisCodePropertySpec());
        return optionalProperties;
    }

    private PropertySpec getNotSupportedLoadProfileObisCodePropertySpec() {
        return this.propertySpecService.basicPropertySpec(notSupportedLoadProfileObisCodePropertyName, false, new ObisCodeValueFactory());
    }

}