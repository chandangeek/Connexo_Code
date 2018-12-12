package com.energyict.protocolimplv2.edp;

import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.math.BigDecimal;

import static com.energyict.dlms.common.DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS;
import static com.energyict.dlms.common.DlmsProtocolProperties.READCACHE_PROPERTY;

/**
 * Wrapper class that holds the EDP DLMS protocol properties, parses them and returns the proper values.
 * This reuses the general DlmsProperties functionality and adds a few things.
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 16:11
 * Author: khe
 */
public class EDPProperties extends DlmsProperties {

    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(READCACHE_PROPERTY, false);
    }

    @Override
    public int getServerLowerMacAddress() {
        return parseBigDecimalProperty(SERVER_LOWER_MAC_ADDRESS, BigDecimal.valueOf(16));
    }
}