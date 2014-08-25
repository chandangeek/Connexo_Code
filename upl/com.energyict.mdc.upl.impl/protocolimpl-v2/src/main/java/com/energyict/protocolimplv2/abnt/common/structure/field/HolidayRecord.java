package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;
import com.energyict.protocolimplv2.abnt.common.field.DateTimeField;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class HolidayRecord extends AbstractField<HolidayRecord> {

    public static final int LENGTH = 3;
    private static final int DATE_LENGTH = 3;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("ddMMyy");

    private DateTimeField holidayDate;

    public HolidayRecord(TimeZone timeZone) {
        this.dateFormatter.setTimeZone(timeZone);
        this.holidayDate = new DateTimeField(dateFormatter, DATE_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return holidayDate.getBytes();
    }

    @Override
    public HolidayRecord parse(byte[] rawData, int offset) throws ParsingException {
        holidayDate.parse(rawData, offset); //TODO: check if correct - can year be left unspecified?
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public DateTimeField getHolidayDate() {
        return holidayDate;
    }
}