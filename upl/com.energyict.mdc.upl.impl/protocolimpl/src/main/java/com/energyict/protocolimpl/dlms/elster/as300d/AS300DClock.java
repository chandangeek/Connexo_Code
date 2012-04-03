package com.energyict.protocolimpl.dlms.elster.as300d;

import com.energyict.cbo.NestedIOException;
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
public class AS300DClock {

    public static final ObisCode CLOCK_OBIS_CODE = ObisCode.fromString("0.0.1.0.0.255");

    private final DlmsSession session;

    public AS300DClock(DlmsSession session) {
        this.session = session;
    }

    public Date getTime() throws IOException {
        return session.getCosemObjectFactory().getClock(CLOCK_OBIS_CODE).getDateTime();
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
