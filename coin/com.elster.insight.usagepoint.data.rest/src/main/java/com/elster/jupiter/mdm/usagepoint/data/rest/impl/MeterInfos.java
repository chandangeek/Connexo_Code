/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Meter;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class MeterInfos {
    public int total;
    public List<MeterInfo> meterInfos = new ArrayList<>();

    MeterInfos(Meter meter) {
        add(meter);
    }

    MeterInfos(Iterable<? extends Meter> meters) {
        addAll(meters);
    }

    final MeterInfo add(Meter meter) {
        MeterInfo result = new MeterInfo(meter);
        meterInfos.add(result);
        total++;
        return result;
    }

    final void addAll(Iterable<? extends Meter> meters) {
        for (Meter each : meters) {
            add(each);
        }
    }
}