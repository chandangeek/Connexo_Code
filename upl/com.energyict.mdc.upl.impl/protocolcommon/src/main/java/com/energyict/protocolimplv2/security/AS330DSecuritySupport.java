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

    private static final BigDecimal DEFAULT_CLIENT_MAC_ADDRESS = BigDecimal.valueOf(2);

    public AS330DSecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return Optional.of(DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService, DEFAULT_CLIENT_MAC_ADDRESS));
    }

}