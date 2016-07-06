package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.common.rest.IntervalInfo;
import com.elster.jupiter.metering.BaseReadingRecord;

import javax.inject.Inject;

public class OutputChannelDataInfoFactory {

    @Inject
    public OutputChannelDataInfoFactory() {

    }

    public OutputChannelDataInfo createChannelDataInfo(BaseReadingRecord baseReadingRecord) {
        OutputChannelDataInfo outputChannelDataInfo = new OutputChannelDataInfo();
        outputChannelDataInfo.value = baseReadingRecord.getValue();
        outputChannelDataInfo.interval = IntervalInfo.from(baseReadingRecord.getTimePeriod().get());
        return outputChannelDataInfo;
    }
}
