package com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.common.ObisCode;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.OptionalPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.ArrayList;
import java.util.List;

/**
 * A <i>set</i> of properties related to LoadProfiles
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/02/13
 * Time: 15:46
 */
public class SDKLoadProfileProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    /**
     * This value holds the name of the Property that contains an ObisCode that we don't support for this session
     */
    public static final String notSupportedLoadProfileObisCodePropertyName = "NotSupportedLoadProfile";

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.SDK_SAMPLE_LOAD_PROFILE_DEVICE_PROTOCOL_DIALECT_NAME.getName();
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

    private PropertySpec<ObisCode> getNotSupportedLoadProfileObisCodePropertySpec() {
        return OptionalPropertySpecFactory.newInstance().obisCodePropertySpec(notSupportedLoadProfileObisCodePropertyName);
    }
}
