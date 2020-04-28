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
        propertySpecs.add(readCachePropertySpec());
        propertySpecs.add(addressModePropertySpec());
        return propertySpecs;
    }

    private PropertySpec addressModePropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.ADDRESSING_MODE, true, PropertyTranslationKeys.DLMS_ADDRESSING_MODE, new BigDecimal(4), new BigDecimal(2), new BigDecimal(4));
    }

    private PropertySpec readCachePropertySpec() {
        return this.booleanSpecBuilder(DlmsProtocolProperties.READCACHE_PROPERTY, false, PropertyTranslationKeys.DLMS_READ_CACHE);
    }
}
