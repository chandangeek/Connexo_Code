package com.energyict.protocolimplv2.dlms.idis.am500;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.security.DeviceSecurityProperty;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 29/03/2016 - 9:18
 */
public class AM500SecuritySupport extends DsmrSecuritySupport {
    public AM500SecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected PropertySpec getClientMacAddressPropertySpec(PropertySpecService propertySpecService) {
        return UPLPropertySpecFactory
                .specBuilder(
                    SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(),
                    false,
                    PropertyTranslationKeys.V2_DLMS_CLIENT_MAC_ADDRESS,
                    () -> propertySpecService.boundedBigDecimalSpec(BigDecimal.valueOf(0), BigDecimal.valueOf(0x7F)))
                .setDefaultValue(BigDecimal.ONE)
                .addValues(DeviceSecurityProperty.getPossibleClientMacAddressValues(0, 0x7F))
                .markExhaustive()
                .finish();
    }

}