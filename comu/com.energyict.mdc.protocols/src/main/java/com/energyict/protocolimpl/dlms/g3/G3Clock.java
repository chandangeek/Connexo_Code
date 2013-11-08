package com.energyict.protocolimpl.dlms.g3;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.Clock;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 21/03/12
 * Time: 11:52
 */
public class G3Clock {

    public static final ObisCode CLOCK_OBIS = ObisCode.fromString("0.0.1.0.0.255");
    private final DlmsSession session;

    public G3Clock(DlmsSession session) {
        this.session = session;
    }

    public Date getTime() throws IOException {
        return getClock().getDateTime();
    }

    private Clock getClock() throws IOException {
        return this.session.getCosemObjectFactory().getClock(CLOCK_OBIS);
    }

    public void setTime() throws IOException {
        Calendar cal = Calendar.getInstance(session.getTimeZone());
        cal.setTime(new Date());
        getClock().setAXDRDateTimeAttr(new AXDRDateTime(cal));
    }

    public void setTime(Date date) throws IOException {
        Calendar cal = Calendar.getInstance(session.getTimeZone());
        cal.setTime(date);
        getClock().setAXDRDateTimeAttr(new AXDRDateTime(cal));
    }
}