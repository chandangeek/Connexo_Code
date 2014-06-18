package com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties;

import com.energyict.cbo.TimeDuration;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.math.BigDecimal;

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
}