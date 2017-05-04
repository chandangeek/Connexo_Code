/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.frame.RequestFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;

import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class ConfigureHolidayListRequest extends Data<ConfigureHolidayListRequest> {

    private static final int PADDING_DATA_LENGTH = 15;

    private HolidayRecords holidayRecords;

    private PaddingData paddingData;

    public ConfigureHolidayListRequest(TimeZone timeZone) {
        super(RequestFrame.REQUEST_DATA_LENGTH, timeZone);
        this.holidayRecords = new HolidayRecords();
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                holidayRecords.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public ConfigureHolidayListRequest parse(byte[] rawData, int offset) throws ParsingException {
        HolidayRecords records = new HolidayRecords();
        records.parse(rawData, offset);
        return this;
    }

    public HolidayRecords getHolidayRecords() {
        return holidayRecords;
    }

    public void setHolidayRecords(HolidayRecords holidayRecords) {
        this.holidayRecords = holidayRecords;
    }
}