package com.energyict.protocolimplv2.dlms.landisAndGyr.properties;

import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import static com.energyict.dlms.common.DlmsProtocolProperties.READCACHE_PROPERTY;

/**
 * Copyrights Honeywell
 * Date: 26/01/22
 * Time: 15:00
 * Author: db
 */
public class ZMYProperties extends DlmsProperties {

    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(READCACHE_PROPERTY, false);
    }

    @Override
    public int getServerLowerMacAddress() {
        String serial_num = getSerialNumber();
        return Integer.parseInt(serial_num.substring(serial_num.length() - 4)) + 1000;
    }
}