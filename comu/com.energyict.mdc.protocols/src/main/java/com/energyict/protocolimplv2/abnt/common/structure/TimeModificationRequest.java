/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
public class TimeModificationRequest extends Data<TimeModificationRequest> {

    private static final int PADDING_DATA_LENGTH = 57;

    private BcdEncodedField hour;
    private BcdEncodedField minutes;
    private BcdEncodedField seconds;

    private PaddingData paddingData;

    public TimeModificationRequest(TimeZone timeZone) {
        super(RequestFrame.REQUEST_DATA_LENGTH, timeZone);
        this.hour = new BcdEncodedField();
        this.minutes = new BcdEncodedField();
        this.seconds = new BcdEncodedField();
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    public TimeModificationRequest(TimeZone timeZone, Calendar calendar) {
        super(RequestFrame.REQUEST_DATA_LENGTH, timeZone);
        this.hour = new BcdEncodedField(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
        this.minutes = new BcdEncodedField(Integer.toString(calendar.get(Calendar.MINUTE)));
        this.seconds = new BcdEncodedField(Integer.toString(calendar.get(Calendar.SECOND)));
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                hour.getBytes(),
                minutes.getBytes(),
                seconds.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public TimeModificationRequest parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        hour.parse(rawData, ptr);
        ptr += hour.getLength();

        minutes.parse(rawData, ptr);
        ptr += minutes.getLength();

        seconds.parse(rawData, ptr);
        return this;
    }

    public BcdEncodedField getHour() {
        return hour;
    }

    public BcdEncodedField getMinutes() {
        return minutes;
    }

    public BcdEncodedField getSeconds() {
        return seconds;
    }

    public void applyCalendar(Calendar calendar) {
        this.hour = new BcdEncodedField(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
        this.minutes = new BcdEncodedField(Integer.toString(calendar.get(Calendar.MINUTE)));
        this.seconds = new BcdEncodedField(Integer.toString(calendar.get(Calendar.SECOND)));
    }
}