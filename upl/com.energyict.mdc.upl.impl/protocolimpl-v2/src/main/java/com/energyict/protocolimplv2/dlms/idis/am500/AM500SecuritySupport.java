package com.energyict.protocolimplv2.dlms.idis.am500;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
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

    @Override
    protected PropertySpec getClientMacAddressPropertySpec() {
        return PropertySpecFactory.boundedDecimalPropertySpecWithDefaultValue(
                SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0x7F),
                BigDecimal.valueOf(1),
                DeviceSecurityProperty.getPossibleClientMacAddressValues(0, 0x7F));
    }
}
