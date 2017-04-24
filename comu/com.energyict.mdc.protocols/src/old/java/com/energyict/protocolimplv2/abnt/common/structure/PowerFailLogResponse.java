/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.frame.ResponseFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.abnt.common.structure.field.PowerFailRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class PowerFailLogResponse extends Data<PowerFailLogResponse> {

    private static final int NR_OF_POWER_FAIL_RECORDS = 20;
    private static final String INVALID_DATE = "000000000000";

    private List<PowerFailRecord> powerFailRecords;

    public PowerFailLogResponse(TimeZone timeZone) {
        super(ResponseFrame.RESPONSE_DATA_LENGTH, timeZone);
        this.powerFailRecords = new ArrayList<>(NR_OF_POWER_FAIL_RECORDS);
    }

    @Override
    public PowerFailLogResponse parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;
        super.parse(rawData, ptr);

        for (int i = 0; i < NR_OF_POWER_FAIL_RECORDS; i++) {
            PowerFailRecord powerFailRecord = new PowerFailRecord(getTimeZone()).parse(rawData, ptr);
            if (!powerFailRecord.getStartOfPowerFail().getBcdEncodedDate().getText().equals(INVALID_DATE)) {
                this.powerFailRecords.add(powerFailRecord); // Only add valid entries
            }
            ptr += powerFailRecord.getLength();
        }
        return this;
    }

    public List<PowerFailRecord> getPowerFailRecords() {
        return powerFailRecords;
    }
}