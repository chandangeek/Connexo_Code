package com.energyict.protocolimplv2.elster.garnet.structure.field;


import com.energyict.comserver.time.Clocks;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 15:28
 */
public class DateTime extends AbstractField<DateTime> {

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyMMddHHmmss");
    private static SimpleDateFormat toStringDateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public static final int LENGTH = 6;

    private Calendar calendar;

    public DateTime(TimeZone timeZone) {
        calendar = Calendar.getInstance(timeZone);
        calendar.setTime(Clocks.getAppServerClock().now());
        dateFormatter.setTimeZone(calendar.getTimeZone());
    }

    public DateTime(TimeZone timeZone, Date date) {
        this.calendar = Calendar.getInstance(timeZone);
        dateFormatter.setTimeZone(calendar.getTimeZone());
        this.calendar.setTime(date);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                getBCDFromInt(calendar.get(Calendar.YEAR) - 2000, 1),
                getBCDFromInt(calendar.get(Calendar.MONTH) + 1, 1), // Month should be 1-based
                getBCDFromInt(calendar.get(Calendar.DAY_OF_MONTH), 1),
                getBCDFromInt(calendar.get(Calendar.HOUR_OF_DAY), 1),
                getBCDFromInt(calendar.get(Calendar.MINUTE), 1),
                getBCDFromInt(calendar.get(Calendar.SECOND), 1)
        );
    }

    @Override
    public DateTime parse(byte[] rawData, int offset) throws ParsingException {
        try {
            String dateTimeString = getHexStringFromBCD(rawData, offset, 6);
            calendar.setTime(dateFormatter.parse(dateTimeString));
            return this;
        } catch (ParseException e) {
            throw new ParsingException(e);
        }
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public Date getDate() {
        return calendar.getTime();
    }

    public void setDate(Date date) {
        this.calendar.setTime(date);
    }

    @Override
    public String toString() {
        return toStringDateFormatter.format(calendar.getTime());
    }
}