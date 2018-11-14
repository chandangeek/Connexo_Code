package com.energyict.protocolimplv2.dlms.idis.am540;

import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.IDISSecurityProvider;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 12/10/2016 - 13:07
 */
public class CryptoAM540SecurityProvider extends IDISSecurityProvider {

    public CryptoAM540SecurityProvider(TypedProperties properties, int authenticationLevel, short errorHandling) {
        super(properties, authenticationLevel, errorHandling);
    }

}