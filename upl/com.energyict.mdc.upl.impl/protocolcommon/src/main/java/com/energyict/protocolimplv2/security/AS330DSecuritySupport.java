package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/06/2015 - 11:45
 */
public class AS330DSecuritySupport extends DsmrSecuritySupport {

    public AS330DSecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return Optional.of(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService, BigDecimal.valueOf(2)));
    }

}