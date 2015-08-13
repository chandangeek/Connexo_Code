package com.elster.protocolimpl.dlms.profile.entrymgmt;

import com.elster.dlms.types.basic.DlmsDate;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.DlmsTime;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsDataOctetString;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * User: heuckeg
 * Date: 15.04.13
 * Time: 12:07
 */
@SuppressWarnings("unused")
public class TimeStampEntry extends AbstractArchiveEntry
{
    public TimeStampEntry(final ObisCode obisCode, final int attribute)
    {
        super(obisCode, attribute);
    }

    public Date toDate(Object[] data, TimeZone timezone, Integer eisStatus)
    {
        Object tstValue = data[getIndex()];
        if (tstValue instanceof DlmsDataOctetString)
        {
            tstValue = new DlmsDateTime(((DlmsDataOctetString)tstValue).getValue());

        }
        if (!(tstValue instanceof DlmsDateTime))
        {
            return null;
        }

        //TODO: set eis status according to clock status?
        DlmsDateTime dateTime = (DlmsDateTime) tstValue;
        if ((dateTime.getDeviation() & DlmsDateTime.DEVIATION_NOT_SPECIFIED) == 0)
        {
            return dateTime.getUtcDate();
        }

        DlmsDate dd = dateTime.getDlmsDate();
        DlmsTime dt = dateTime.getDlmsTime();

        Calendar c = Calendar.getInstance(timezone);
        c.clear();

        c.set(dd.getYear(), dd.getMonth() - 1, dd.getDayOfMonth(), dt.getHour(), dt.getMinute(), dt.getSecond());

        return c.getTime();
    }

    @Override
    public String toString()
    {
        return "TST=" + super.toString();
    }
}
