package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.common.rest.IntervalInfo;
import com.elster.jupiter.metering.BaseReadingRecord;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;

public class OutputChannelDataInfo {
    public IntervalInfo interval;
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;

    public OutputChannelDataInfo() {

    }

    public static OutputChannelDataInfo asInfo(BaseReadingRecord baseReadingRecord) {
        OutputChannelDataInfo outputChannelDataInfo = new OutputChannelDataInfo();
        outputChannelDataInfo.value = baseReadingRecord.getValue();
        outputChannelDataInfo.interval = IntervalInfo.from(baseReadingRecord.getTimePeriod().get());
        return outputChannelDataInfo;
    }
}
