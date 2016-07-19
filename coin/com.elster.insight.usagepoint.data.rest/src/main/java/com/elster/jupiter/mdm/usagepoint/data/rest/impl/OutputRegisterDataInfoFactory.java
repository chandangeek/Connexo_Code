package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ReadingRecord;

public class OutputRegisterDataInfoFactory {

    public OutputRegisterDataInfo createRegisterDataInfo(ReadingRecord readingRecord) {
        OutputRegisterDataInfo info = new OutputRegisterDataInfo();
        info.timeStamp = readingRecord.getTimeStamp();
        info.value = readingRecord.getValue();
        return info;
    }
}
