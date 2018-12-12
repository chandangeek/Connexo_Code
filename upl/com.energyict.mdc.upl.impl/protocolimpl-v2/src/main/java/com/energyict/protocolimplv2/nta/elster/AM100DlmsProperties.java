package com.energyict.protocolimplv2.nta.elster;

import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

/**
 * @author sva
 * @since 29/05/2015 - 14:26
 */
public class AM100DlmsProperties extends DlmsProperties {

    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(AM100ConfigurationSupport.READCACHE_PROPERTY, false);
    }

}