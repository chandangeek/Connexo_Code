package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.ObisCodeValueFactory;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimplv2.DeviceProtocolDialectName;
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

    public SDKLoadProfileProtocolDialectProperties(PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectName.SDK_SAMPLE_LOAD_PROFILE_DEVICE_PROTOCOL.getName();
    }

    @Override
    public String getDisplayName() {
        return this.getThesaurus().getFormat(DeviceProtocolDialectName.SDK_SAMPLE_LOAD_PROFILE_DEVICE_PROTOCOL).format();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(this.getNotSupportedLoadProfileObisCodePropertySpec());
        return optionalProperties;
    }

    private PropertySpec getNotSupportedLoadProfileObisCodePropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(notSupportedLoadProfileObisCodePropertyName, false, new ObisCodeValueFactory());
    }

}