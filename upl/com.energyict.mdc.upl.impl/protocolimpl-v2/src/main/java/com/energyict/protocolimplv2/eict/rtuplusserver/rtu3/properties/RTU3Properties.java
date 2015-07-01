package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.properties;

import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 9/06/2015 - 17:27
 */
public class RTU3Properties extends DlmsProperties {

    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(RTU3ConfigurationSupport.READCACHE_PROPERTY, false);
    }
}