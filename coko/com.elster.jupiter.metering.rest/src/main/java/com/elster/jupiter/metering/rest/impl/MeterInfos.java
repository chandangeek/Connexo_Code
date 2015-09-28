package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.Meter;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class MeterInfos {
    public int total;
    public List<MeterInfo> meterInfos = new ArrayList<>();

    MeterInfos() {
    }

    MeterInfos(Meter meter) {
        add(meter);
    }

    MeterInfos(Iterable<? extends Meter> meters) {
        addAll(meters);
    }

    MeterInfo add(Meter meter) {
        MeterInfo result = new MeterInfo(meter);
        meterInfos.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends Meter> meters) {
        for (Meter each : meters) {
            add(each);
        }
    }
}