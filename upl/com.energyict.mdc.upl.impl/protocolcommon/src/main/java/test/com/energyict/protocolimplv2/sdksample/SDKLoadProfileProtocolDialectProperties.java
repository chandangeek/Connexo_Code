package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.cpo.MdwToUplPropertySpecAdapter;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Collections;
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
    public String getDeviceProtocolDialectDisplayName() {
        return "SDK dialect for loadProfile testing";
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getPropertySpecs() {
        return Collections.singletonList(MdwToUplPropertySpecAdapter.adapt(getNotSupportedLoadProfileObisCodePropertySpec()));
    }

    private PropertySpec getNotSupportedLoadProfileObisCodePropertySpec() {
        return PropertySpecFactory.obisCodePropertySpec(notSupportedLoadProfileObisCodePropertyName);
    }
}
