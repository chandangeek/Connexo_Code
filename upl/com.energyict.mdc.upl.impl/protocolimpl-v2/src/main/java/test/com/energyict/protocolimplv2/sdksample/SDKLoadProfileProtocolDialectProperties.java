package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.properties.nls.PropertyTranslationKeys;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
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

    public SDKLoadProfileProtocolDialectProperties(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public SDKLoadProfileProtocolDialectProperties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.SDK_SAMPLE_LOAD_PROFILE_DEVICE_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.SDK_SAMPLE_LOAD_PROFILE_DEVICE_PROTOCOL_DIALECT_NAME).format();
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Collections.singletonList(getNotSupportedLoadProfileObisCodePropertySpec());
    }

    private PropertySpec getNotSupportedLoadProfileObisCodePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(notSupportedLoadProfileObisCodePropertyName, false, PropertyTranslationKeys.SDKSAMPLE_NOT_SUPPORTED_LOADPROFILE_OBISCODE, getPropertySpecService()::obisCodeSpec).finish();
    }
}