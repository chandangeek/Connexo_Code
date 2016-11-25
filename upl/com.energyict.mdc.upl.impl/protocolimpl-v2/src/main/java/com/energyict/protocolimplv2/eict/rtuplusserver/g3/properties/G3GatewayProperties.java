package com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.time.Duration;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 11/06/2014 - 13:46
 */
public class G3GatewayProperties extends DlmsProperties {

    public static final String AARQ_TIMEOUT = "AARQ_Timeout";
    public static final Duration AARQ_TIMEOUT_DEFAULT = Duration.ofSeconds(0);

    public long getAarqTimeout() {
        return getProperties().getTypedProperty(AARQ_TIMEOUT, AARQ_TIMEOUT_DEFAULT).toMillis();
    }

    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(DlmsProtocolProperties.READCACHE_PROPERTY, false);
    }

}