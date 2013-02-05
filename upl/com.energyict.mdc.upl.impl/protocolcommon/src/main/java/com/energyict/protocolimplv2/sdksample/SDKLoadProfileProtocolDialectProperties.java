package com.energyict.protocolimplv2.sdksample;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.tasks.DeviceProtocolDialectImpl;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A <i>set</i> of properties related to LoadProfiles
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/02/13
 * Time: 15:46
 */
public class SDKLoadProfileProtocolDialectProperties extends DeviceProtocolDialectImpl {

    /**
     * This value holds the name of the Property that contains an ObisCode that we don't support for this session
     */
    public static final String notSupportedLoadProfileObisCodePropertyName = "NotSupportedLoadProfile";

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.SDK_SAMPLE_LOAD_PROFILE_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        if(name.equals(notSupportedLoadProfileObisCodePropertyName)){
            return getNotSupportedLoadProfileObisCodePropertySpec();
        }
        return null;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(getNotSupportedLoadProfileObisCodePropertySpec());
        return optionalProperties;    }

    private PropertySpec<ObisCode> getNotSupportedLoadProfileObisCodePropertySpec() {
        return PropertySpecFactory.obisCodePropertySpec(notSupportedLoadProfileObisCodePropertyName);
    }
}
