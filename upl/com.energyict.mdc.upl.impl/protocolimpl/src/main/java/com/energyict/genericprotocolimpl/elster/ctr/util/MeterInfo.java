package com.energyict.genericprotocolimpl.elster.ctr.util;

import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.*;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.structure.NackStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.ReferenceDate;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.WriteDataBlock;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 14-okt-2010
 * Time: 16:31:45
 */
public class MeterInfo extends AbstractUtilObject {

    private TimeZone timeZone;
    private List<AbstractCTRObject> ctrObjectList;
    private long time;
    private int wdbCounter = 0x05;

    public MeterInfo(GprsRequestFactory requestFactory, Logger logger, TimeZone timeZone) {
        super(requestFactory, logger);
        if (timeZone == null) {
            this.timeZone = TimeZone.getDefault();
            getLogger().warning("No timezone given. Using default timeZone: [" + this.timeZone.getID() + "]");
        } else {
            this.timeZone = timeZone;
        }
    }

    public MeterInfo(GprsRequestFactory requestFactory, Logger logger) {
        super(requestFactory, logger);
        this.timeZone = TimeZone.getDefault();
        getLogger().warning("No timezone given. Using default timeZone: [" + this.timeZone.getID() + "]");
    }

    public String getConverterSerialNumber() throws CTRException {
        try {
            return getMeterTimeAndSerialNumbers().get(2).getValue()[1].getValue().toString();
        } catch (CTRException e) {
            throw new CTRException("Unable to read Converter SerialNumber", e);
        }
    }

    public Date getTime() throws CTRException {
        try {
            return getDateFromObject(getMeterTimeAndSerialNumbers().get(0));
        } catch (CTRException e) {
            throw new CTRException("Unable to read Clock", e);
        }
    }

    private Data setTime(Date referenceDate, int mode, int year, int month, int day, int dayOfWeek, int hour, int minutes, int seconds) throws CTRException {
        TimeZone comServerTimeZone = TimeZone.getDefault();

        byte[] data = new byte[10];
        data[0] = (byte) mode;
        data[1] = (byte) year;
        data[2] = (byte) month;
        data[3] = (byte) day;
        data[4] = (byte) dayOfWeek;
        data[5] = (byte) hour;
        data[6] = (byte) minutes;
        data[7] = (byte) seconds;
        data[8] = (byte) (comServerTimeZone.getRawOffset() / 3600000);
        data[9] = (byte) (comServerTimeZone.inDaylightTime(referenceDate) ? 1 : 0);
        WriteDataBlock wdb = new WriteDataBlock(wdbCounter++);
        ReferenceDate refDate = new ReferenceDate().parse(referenceDate, timeZone);
        refDate.setTomorrow();

        Data ackOrNack = getRequestFactory().executeRequest(refDate, wdb, new CTRObjectID("11.0.1"), data);
        if (ackOrNack instanceof NackStructure) {
            throw new CTRException("There was an error setting the time to " + year + "/" + month + "/" + day + "/" + hour + " " + minutes  + ":" + seconds);
        }
        return ackOrNack;
    }

    public Data setTime(Date time) throws CTRException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        Data ackOrNack = setTime(
                time,
                0,
                cal.get(Calendar.YEAR) - 2000,
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.DAY_OF_WEEK),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND)
        );
        return ackOrNack;
    }

    public String getMTUSerialNumber() throws CTRException {
        try {
            return getMeterTimeAndSerialNumbers().get(1).getValue()[0].getValue().toString();
        } catch (CTRException e) {
            throw new CTRException("Unable to read MTU SerialNumber", e);
        }
    }

    private Date getDateFromObject(AbstractCTRObject object) throws CTRException {
        Calendar cal = Calendar.getInstance(timeZone);
        if (!(object instanceof DateAndTimeCategory)) {
            throw new CTRException("Expected DateAndTimeCategory object!");
        } else {
            DateAndTimeCategory dateAndTime = (DateAndTimeCategory) object;
            CTRAbstractValue<BigDecimal>[] values = dateAndTime.getValue();
            int ptr = 0;
            int year = values[ptr++].getValue().intValue() + 2000;
            int month = values[ptr++].getValue().intValue() - 1;
            int day = values[ptr++].getValue().intValue();
            ptr++; // Day of week
            int hour = values[ptr++].getValue().intValue();
            int min = values[ptr++].getValue().intValue();
            int sec = values[ptr++].getValue().intValue();

            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, min);
            cal.set(Calendar.SECOND, sec);
            cal.set(Calendar.MILLISECOND, 0);
        }
        cal.add(Calendar.MILLISECOND, (int) (System.currentTimeMillis() - time));

        return cal.getTime();
    }

    private List<AbstractCTRObject> getMeterTimeAndSerialNumbers() throws CTRException {

        if (ctrObjectList == null) {
            AttributeType attributeType = new AttributeType(0x03);
            ctrObjectList = getRequestFactory().queryRegisters(attributeType, "8.0.0", "9.0.2", "C.2.1");
            time = System.currentTimeMillis();
        }
        return ctrObjectList;
    }
}
