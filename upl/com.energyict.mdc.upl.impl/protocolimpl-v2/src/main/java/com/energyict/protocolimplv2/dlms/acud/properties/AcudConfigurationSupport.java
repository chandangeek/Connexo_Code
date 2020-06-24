package com.energyict.protocolimplv2.dlms.acud.properties;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AcudConfigurationSupport extends DlmsConfigurationSupport {

    public AcudConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(overwriteServerLowerMacAddressPropertySpec());
        propertySpecs.add(readCachePropertySpec());
        propertySpecs.add(addressModePropertySpec());
        propertySpecs.add(informationFieldSizePropertySpec());
        return propertySpecs;
    }

    private PropertySpec informationFieldSizePropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.INFORMATION_FIELD_SIZE, false, com.energyict.nls.PropertyTranslationKeys.V2_TASKS_INFORMATION_FIELD_SIZE,BigDecimal.valueOf(128), BigDecimal.valueOf(128), BigDecimal.valueOf(256), BigDecimal.valueOf(512), BigDecimal.valueOf(1024), BigDecimal.valueOf(2048));
    }

    private PropertySpec addressModePropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.ADDRESSING_MODE, true, PropertyTranslationKeys.DLMS_ADDRESSING_MODE, new BigDecimal(4), new BigDecimal(2), new BigDecimal(4));
    }

    private PropertySpec readCachePropertySpec() {
        return this.booleanSpecBuilder(DlmsProtocolProperties.READCACHE_PROPERTY, false, PropertyTranslationKeys.DLMS_READ_CACHE);
    }

    private PropertySpec overwriteServerLowerMacAddressPropertySpec() {
        return this.booleanSpecBuilder(AcudDlmsProperties.OVERWRITE_SERVER_LOWER_MAC_ADDRESS, false, PropertyTranslationKeys.DLMS_OVERWRITE_SERVER_LOWER_MAC_ADDRESS);
    }
}
