package com.energyict.protocolimplv2.dialects;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;

import java.util.Collections;
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

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.NO_PARAMETERS_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "Default with no properties";
    }

}