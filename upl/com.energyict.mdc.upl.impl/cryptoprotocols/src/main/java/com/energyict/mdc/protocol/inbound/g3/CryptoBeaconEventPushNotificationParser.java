package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.common.framework.CryptoDlmsSession;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.CryptoBeacon3100Properties;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 15/11/2016 - 17:57
 */
public class CryptoBeaconEventPushNotificationParser extends EventPushNotificationParser {

    public CryptoBeaconEventPushNotificationParser(ComChannel comChannel, InboundDiscoveryContext context, ObisCode obisStandardEventLog) {
        super(comChannel, context, obisStandardEventLog);
    }

    @Override
    protected DlmsProperties getNewInstanceOfProperties() {
        return new CryptoBeacon3100Properties(context.getCertificateWrapperExtractor());
    }

    @Override
    protected DlmsSession getNewInstanceOfDlmsSession(DlmsProperties dlmsProperties, DummyComChannel comChannel) {
        return new CryptoDlmsSession(comChannel, dlmsProperties);
    }
}