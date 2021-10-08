package com.energyict.protocolimplv2.dlms.idis.aec.properties;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.properties.DescriptionTranslationKey;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AECConfigurationSupport extends AM540ConfigurationSupport {

    public AECConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> specs = new ArrayList<>(super.getUPLPropertySpecs());
        specs.add(this.serverUpperMacAddressPropertySpec());
        specs.add(this.overwriteServerLowerMacAddressPropertySpec());
        specs.add(this.addressModePropertySpec());
        return specs;
    }

    protected PropertySpec overwriteServerLowerMacAddressPropertySpec() {
        return this.booleanSpecBuilder(AECDlmsProperties.OVERWRITE_SERVER_LOWER_MAC_ADDRESS, false, com.energyict.protocolimpl.nls.PropertyTranslationKeys.DLMS_OVERWRITE_SERVER_LOWER_MAC_ADDRESS);
    }

    protected PropertySpec booleanSpecBuilder(String name, boolean defaultValue, TranslationKey translationKey) {
        return getPropertySpecService()
                .booleanSpec()
                .named(name, translationKey).describedAs(new DescriptionTranslationKey(translationKey))
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec addressModePropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.ADDRESSING_MODE, true, com.energyict.protocolimpl.nls.PropertyTranslationKeys.DLMS_ADDRESSING_MODE, new BigDecimal(4), new BigDecimal(2), new BigDecimal(4));
    }
}
