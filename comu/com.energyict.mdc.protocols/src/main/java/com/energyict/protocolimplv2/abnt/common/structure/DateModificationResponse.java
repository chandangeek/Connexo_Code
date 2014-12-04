package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;
import com.energyict.protocolimplv2.abnt.common.frame.RequestFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class DateModificationResponse extends Data<DateModificationResponse> {

    private static final int PADDING_DATA_LENGTH = 56;

    private BcdEncodedField day;
    private BcdEncodedField month;
    private BcdEncodedField year;
    private BcdEncodedField dayOfWeek;

    private PaddingData paddingData;

    public DateModificationResponse(TimeZone timeZone) {
        super(RequestFrame.REQUEST_DATA_LENGTH, timeZone);
        this.day = new BcdEncodedField();
        this.month = new BcdEncodedField();
        this.year = new BcdEncodedField();
        this.dayOfWeek = new BcdEncodedField();
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    public DateModificationResponse(TimeZone timeZone, Calendar calendar) {
        super(RequestFrame.REQUEST_DATA_LENGTH, timeZone);
        this.day = new BcdEncodedField(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
        this.month = new BcdEncodedField(Integer.toString(calendar.get(Calendar.MONTH)));
        this.year = new BcdEncodedField(Integer.toString(calendar.get(Calendar.YEAR) % 2000));
        this.dayOfWeek = new BcdEncodedField(Integer.toString(calendar.get(Calendar.DAY_OF_WEEK)));
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                day.getBytes(),
                month.getBytes(),
                year.getBytes(),
                dayOfWeek.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public DateModificationResponse parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;
        super.parse(rawData, ptr);

        day.parse(rawData, ptr);
        ptr += day.getLength();

        month.parse(rawData, ptr);
        ptr += month.getLength();

        year.parse(rawData, ptr);
        ptr += year.getLength();

        dayOfWeek.parse(rawData, ptr);
        return this;
    }

    public BcdEncodedField getDay() {
        return day;
    }

    public BcdEncodedField getMonth() {
        return month;
    }

    public BcdEncodedField getYear() {
        return year;
    }

    public BcdEncodedField getDayOfWeek() {
        return dayOfWeek;
    }
}