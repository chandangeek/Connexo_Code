package com.energyict.protocolimplv2.dialects;

import com.energyict.cpo.PropertySpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple dialect that has no parameters.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/06/13
 * Time: 13:39
 * Author: khe
 */
public class NoParamsDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public static final String NO_PARAMETERS_DEVICE_PROTOCOL_DIALECT_NAME = "NoParamsDialect";

    @Override
    public PropertySpec getPropertySpec(String name) {
        return null;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return new ArrayList<PropertySpec>(0);
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return new ArrayList<PropertySpec>(0);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return NO_PARAMETERS_DEVICE_PROTOCOL_DIALECT_NAME;
    }
}