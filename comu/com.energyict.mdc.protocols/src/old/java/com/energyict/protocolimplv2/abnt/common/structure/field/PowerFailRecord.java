/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;
import com.energyict.protocolimplv2.abnt.common.field.DateTimeField;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class PowerFailRecord extends AbstractField<PowerFailRecord> {

    public static final int LENGTH = 12;
    private static final int DATE_TIME_LENGTH = 6;
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HHmmssddMMyy");

    private DateTimeField startOfPowerFail;
    private DateTimeField endOfPowerFail;

    public PowerFailRecord(TimeZone timeZone) {
        this.dateFormatter.setTimeZone(timeZone);
        this.startOfPowerFail = new DateTimeField(this.dateFormatter, DATE_TIME_LENGTH);
        this.endOfPowerFail = new DateTimeField(this.dateFormatter, DATE_TIME_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                startOfPowerFail.getBytes(),
                endOfPowerFail.getBytes()
        );
    }

    @Override
    public PowerFailRecord parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        startOfPowerFail.parse(rawData, ptr);
        ptr += startOfPowerFail.getLength();

        endOfPowerFail.parse(rawData, ptr);

        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public DateTimeField getStartOfPowerFail() {
        return startOfPowerFail;
    }

    public DateTimeField getEndOfPowerFail() {
        return endOfPowerFail;
    }
}