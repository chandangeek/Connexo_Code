package com.energyict.protocolimplv2.abnt.common.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class DateTimeField extends AbstractField<DateTimeField> {

    private SimpleDateFormat dateFormatter;
    private BcdEncodedField bcdEncodedDate;

    public DateTimeField(SimpleDateFormat dateFormatter, int length) {
        this.dateFormatter = dateFormatter;
        this.bcdEncodedDate = new BcdEncodedField(length);
    }

    @Override
    public byte[] getBytes() {
        return bcdEncodedDate.getBytes();
    }

    @Override
    public DateTimeField parse(byte[] rawData, int offset) throws ParsingException {
         this.bcdEncodedDate.parse(rawData, offset);
        return this;
    }

    @Override
    public int getLength() {
        return getBcdEncodedDate().getLength();
    }

    public BcdEncodedField getBcdEncodedDate() {
        return bcdEncodedDate;
    }

    public Date getDate(TimeZone timeZone) throws ParsingException {
        dateFormatter.setTimeZone(timeZone);
        try {
            return dateFormatter.parse(bcdEncodedDate.getText());
        } catch (ParseException e) {
            throw new ParsingException("Failed to parse a proper date: " + e.getMessage());
        }
    }

    public void setDate(Date date, TimeZone timeZone) {
        dateFormatter.setTimeZone(timeZone);
        this.bcdEncodedDate.setText(dateFormatter.format(date));
    }

    public void setDate(String formattedDateTime) {
       this.bcdEncodedDate.setText(formattedDateTime);
    }
}