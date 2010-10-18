package com.energyict.genericprotocolimpl.elster.ctr.util;

import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.*;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.ReferenceDate;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.WriteDataBlock;
import com.energyict.protocolimpl.utils.ProtocolTools;

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
    private int wdbCounter = 0x92;

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

    public String getConverterSerialNumber() {
        AbstractCTRObject object3 = getCtrObjectList().get(2);
        return object3.getValue()[1].getValue().toString();
    }

    public Date getTime() throws CTRException {
        AbstractCTRObject object1 = getCtrObjectList().get(0);
        return getDateFromObject(object1);
    }

    public Data setTime(ReferenceDate refDate, int mode, int year, int month, int day, int dayOfWeek, int hour, int minutes, int seconds, int gmtOffset, int period) throws CTRException {
        byte[] data = new byte[10];
        data[0] = (byte) mode;
        data[1] = (byte) year;
        data[2] = (byte) month;
        data[3] = (byte) day;
        data[4] = (byte) dayOfWeek;
        data[5] = (byte) hour;
        data[6] = (byte) minutes;
        data[7] = (byte) seconds;
        data[8] = (byte) gmtOffset;
        data[9] = (byte) period;
        WriteDataBlock wdb = new WriteDataBlock(wdbCounter++);

        Data ackOrNack = getRequestFactory().executeRequest(refDate, wdb, new CTRObjectID("11.0.1"), data);
        return ackOrNack;
    }

    public Data setTime(ReferenceDate refDate, int hour, int minutes, int seconds, int gmtOffset, int period) throws CTRException {

        GregorianCalendar calendar = new GregorianCalendar();

        byte[] data = new byte[10];
        data[0] = (byte) 0;
        data[1] = (byte) (calendar.get(Calendar.YEAR) - 2000);
        data[2] = (byte) (calendar.get(Calendar.MONTH) + 1);
        data[3] = (byte) calendar.get(Calendar.DATE);
        data[4] = (byte) (calendar.get(Calendar.DAY_OF_WEEK) - 1);
        data[5] = (byte) hour;
        data[6] = (byte) minutes;
        data[7] = (byte) seconds;
        data[8] = (byte) gmtOffset;
        data[9] = (byte) period;
        WriteDataBlock wdb = new WriteDataBlock(wdbCounter++);

        Data ackOrNack = getRequestFactory().executeRequest(refDate, wdb, new CTRObjectID("11.0.1"), data);
        return ackOrNack;
    }

    public String getMTUSerialNumber() {
        AbstractCTRObject object2 = getCtrObjectList().get(1);
        return object2.getValue()[0].getValue().toString();
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

    private List<AbstractCTRObject> getCtrObjectList() {

        if (ctrObjectList == null) {
            AttributeType attributeType = new AttributeType(0x03);
            try {
                ctrObjectList = getRequestFactory().queryRegisters(attributeType, "8.0.0", "9.0.2", "C.2.1");
                time = System.currentTimeMillis();
            } catch (CTRException e) {
                e.printStackTrace();
            }
        }
        return ctrObjectList;
    }
}
