package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.BaseReading;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ReadingInfos {
    public int total;
    public List<ReadingInfo> readingInfos = new ArrayList<>();

    ReadingInfos() {
    }

    ReadingInfos(BaseReading reading) {
        add(reading);
    }

    ReadingInfos(Iterable<? extends BaseReading> readings) {
        addAll(readings);
    }

    ReadingInfo add(BaseReading reading) {
        ReadingInfo result = new ReadingInfo(reading);
        readingInfos.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends BaseReading> readings) {
        for (BaseReading each : readings) {
            add(each);
        }
    }

}
