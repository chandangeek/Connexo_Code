package com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties;

import com.energyict.cbo.TimeDuration;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.edp.EDPProperties;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 11/06/2014 - 13:46
 */
public class G3GatewayProperties extends DlmsProperties {

    public static final String AARQ_TIMEOUT = "AARQ_Timeout";
    public static final TimeDuration AARQ_TIMEOUT_DEFAULT = new TimeDuration(0);   //0 means use the normal timeout value

    public long getAarqTimeout() {
        return getProperties().getTypedProperty(AARQ_TIMEOUT, AARQ_TIMEOUT_DEFAULT).getMilliSeconds();
    }

    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(DlmsProtocolProperties.READCACHE_PROPERTY, false);
    }
}