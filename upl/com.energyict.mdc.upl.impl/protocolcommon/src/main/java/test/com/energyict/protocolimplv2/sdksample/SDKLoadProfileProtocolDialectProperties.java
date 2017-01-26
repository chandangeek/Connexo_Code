package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Collections;
import java.util.List;

/**
 * A <i>set</i> of properties related to LoadProfiles
 * <p>
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
        super(propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.SDK_SAMPLE_LOAD_PROFILE_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "SDK dialect for loadProfile testing";
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Collections.singletonList(getNotSupportedLoadProfileObisCodePropertySpec());
    }

    private PropertySpec getNotSupportedLoadProfileObisCodePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(notSupportedLoadProfileObisCodePropertyName, false, propertySpecService::obisCodeSpec).finish();
    }
}