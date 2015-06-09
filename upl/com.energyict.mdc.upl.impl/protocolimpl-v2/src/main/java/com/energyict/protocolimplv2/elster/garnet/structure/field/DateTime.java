package com.energyict.protocolimplv2.elster.garnet.structure.field;


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

    public static final int LENGTH = 6;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyMMddHHmmss");
    private SimpleDateFormat toStringDateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private Calendar calendar;

    public DateTime(TimeZone timeZone) {
        calendar = Calendar.getInstance(timeZone);
        calendar.setTime(new Date());
        dateFormatter.setTimeZone(timeZone);
    }

    public DateTime(TimeZone timeZone, Date date) {
        calendar = Calendar.getInstance(timeZone);
        dateFormatter.setTimeZone(calendar.getTimeZone());
        calendar.setTime(date);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                getBCDFromHexString(Integer.toString(calendar.get(Calendar.YEAR) - 2000), 1),
                getBCDFromHexString(Integer.toString(calendar.get(Calendar.MONTH) + 1), 1), // Month should be 1-based
                getBCDFromHexString(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)), 1),
                getBCDFromHexString(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)), 1),
                getBCDFromHexString(Integer.toString(calendar.get(Calendar.MINUTE)), 1),
                getBCDFromHexString(Integer.toString(calendar.get(Calendar.SECOND)), 1)
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