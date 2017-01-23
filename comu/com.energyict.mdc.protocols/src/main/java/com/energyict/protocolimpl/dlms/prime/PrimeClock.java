package com.energyict.protocolimpl.dlms.prime;

import com.energyict.mdc.common.NestedIOException;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.Clock;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 24/02/12
 * Time: 9:38
 */
public class PrimeClock {

    public static final ObisCode CLOCK_OBIS_CODE = ObisCode.fromString("0.0.1.0.0.255");

    private final DlmsSession session;

    public PrimeClock(DlmsSession session) {
        this.session = session;
    }

    /**
     * Uses the device timezone that is configured in EiServer
     */
    public Date getTime() throws IOException {
        return session.getCosemObjectFactory().getClock(CLOCK_OBIS_CODE).getAXDRDateTime().getValue().getTime();
    }

    public void setTime() throws IOException {
        try {
            AXDRDateTime dateTime = new AXDRDateTime(session.getTimeZone());
            Clock clock = session.getCosemObjectFactory().getClock(CLOCK_OBIS_CODE);
            clock.setAXDRDateTimeAttr(dateTime);
        } catch (IOException e) {
            session.getLogger().log(Level.FINEST, e.getMessage());
            throw new NestedIOException(e, "Could not write the clock!");
        }
    }
}