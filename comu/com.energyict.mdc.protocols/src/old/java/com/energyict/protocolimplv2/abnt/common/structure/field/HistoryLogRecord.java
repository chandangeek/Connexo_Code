/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;
import com.energyict.protocolimplv2.abnt.common.field.DateTimeField;
import com.energyict.protocolimplv2.abnt.common.frame.field.ReaderSerialNumber;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class HistoryLogRecord extends AbstractField<HistoryLogRecord> {

    public static final int LENGTH = 10;
    private static final int DATE_TIME_LENGTH = 6;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("HHmmssddMMyy");

    private EventField event;
    private ReaderSerialNumber readerSerialNumber;
    private DateTimeField eventDate;


    public HistoryLogRecord(TimeZone timeZone) {
        this.dateFormatter.setTimeZone(timeZone);
        this.event = new EventField();
        this.readerSerialNumber = new ReaderSerialNumber();
        this.eventDate = new DateTimeField(this.dateFormatter, DATE_TIME_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                event.getBytes(),
                readerSerialNumber.getBytes(),
                eventDate.getBytes()
        );
    }

    @Override
    public HistoryLogRecord parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        event.parse(rawData, ptr);
        ptr += event.getLength();

        readerSerialNumber.parse(rawData, ptr);
        ptr += readerSerialNumber.getLength();

        eventDate.parse(rawData, ptr);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public EventField getEvent() {
        return event;
    }

    public ReaderSerialNumber getReaderSerialNumber() {
        return readerSerialNumber;
    }

    public DateTimeField getEventDate() {
        return eventDate;
    }
}