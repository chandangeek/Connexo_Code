package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E35C;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.composedobjects.ComposedMeterInfo;

/**
 * @author sva
 * @since 22/06/2015 - 14:11
 */
public class E35CMeterInfo extends ComposedMeterInfo {

    public E35CMeterInfo(DlmsSession dlmsSession, boolean bulkRequest) {
        super(dlmsSession, bulkRequest);
    }

    @Override
    protected String getStringValueFrom(OctetString octetString) {
        return octetString.getOctetString().stringValue().trim(); // Should trim the addition 0x00 bytes
    }
}
