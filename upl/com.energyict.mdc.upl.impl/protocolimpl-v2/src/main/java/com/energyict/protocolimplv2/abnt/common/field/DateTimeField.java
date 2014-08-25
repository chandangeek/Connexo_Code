package com.energyict.protocolimplv2.abnt.common.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class DateTimeField extends AbstractField<DateTimeField> {

    public int length;
    private SimpleDateFormat dateFormatter;

    private Date dateTime;

    public DateTimeField(SimpleDateFormat dateFormatter, int length) {
        this.dateFormatter = dateFormatter;
        this.length = length;
    }

    @Override
    public byte[] getBytes() {
        return getBCDFromHexString(dateFormatter.format(dateTime), length);
    }

    @Override
    public DateTimeField parse(byte[] rawData, int offset) throws ParsingException {
        try {
            BcdEncodedField startOfPowerFailField = new BcdEncodedField(length).parse(rawData, offset);
            this.dateTime = this.dateFormatter.parse(Integer.toString(startOfPowerFailField.getNumber()));
        } catch (ParseException e) {
            throw new ParsingException("Failed to parse DateTimeField:", e);
        }
        return this;
    }

    @Override
    public int getLength() {
        return length;
    }

    public Date getDateTime() {
        return dateTime;
    }
}