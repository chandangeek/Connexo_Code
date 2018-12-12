package com.energyict.protocolimplv2.eict.webrtuz3.properties;

import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import static com.energyict.dlms.common.DlmsProtocolProperties.BULK_REQUEST;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/04/2015 - 10:55
 */
public class WebRTUZ3Properties extends DlmsProperties {

    @Override
    public boolean isBulkRequest() {
        return getProperties().<Boolean>getTypedProperty(BULK_REQUEST, true);
    }
}