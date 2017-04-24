/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;
import com.energyict.protocolimplv2.abnt.common.field.DateTimeField;
import com.energyict.protocolimplv2.abnt.common.structure.field.HolidayRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * @author sva
 * @since 11/09/2014 - 11:39
 */
public class HolidayRecords extends AbstractField<HolidayRecords> {

    private static final int NUMBER_OF_HOLIDAY_RECORDS = 15;
    private static final String DUMMY_DATE = "010101";
    private static final String EMPTY_DATE = "000000";
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");

    private List<HolidayRecord> holidayRecords;

    public HolidayRecords() {
        this.holidayRecords = new ArrayList<>();
    }

    public HolidayRecords(List<HolidayRecord> holidayRecords) {
        this.holidayRecords = holidayRecords;
    }

    @Override
    public byte[] getBytes() {
        byte[] bytes = new byte[0];
        for (int i = 0; i < 15; i++) {
            bytes = ProtocolTools.concatByteArrays(bytes,
                    holidayRecords.size() > i
                            ? holidayRecords.get(i).getBytes()
                            : new byte[3]
            );
        }
        return bytes;
    }

    @Override
    public HolidayRecords parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.holidayRecords.clear();
        for (int i = 0; i < NUMBER_OF_HOLIDAY_RECORDS; i++) {
            HolidayRecord record = new HolidayRecord();
            record.parse(rawData, ptr);
            holidayRecords.add(record);
            ptr += record.getLength();
        }
        return this;
    }

    @Override
    public int getLength() {
        return HolidayRecord.LENGTH * NUMBER_OF_HOLIDAY_RECORDS;
    }

    public List<HolidayRecord> getHolidayRecords() {
        return holidayRecords;
    }

    public void addHolidayRecord(HolidayRecord holidayRecord) {
        this.holidayRecords.add(holidayRecord);
    }

    public String getAllHolidaysAsText(TimeZone timeZone) throws ParsingException {
        dateFormatter.setTimeZone(timeZone);

        StringBuilder builder = new StringBuilder();
        for (HolidayRecord holidayRecord : holidayRecords) {
            DateTimeField holidayDate = holidayRecord.getHolidayDate();
            if (!holidayDate.getBcdEncodedDate().getText().equals(EMPTY_DATE) && !holidayDate.getBcdEncodedDate().getText().equals(DUMMY_DATE)) {
                builder.append(dateFormatter.format(holidayDate.getDate(timeZone)));
                builder.append("; ");
            }
        }
        return builder.toString();
    }
}