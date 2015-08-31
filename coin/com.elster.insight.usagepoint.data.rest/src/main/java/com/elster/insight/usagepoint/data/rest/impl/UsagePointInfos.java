package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.UsagePoint;

import javax.xml.bind.annotation.XmlRootElement;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class UsagePointInfos {
    public int total;
    public List<UsagePointInfo> usagePoints = new ArrayList<>();

    UsagePointInfos() {
    }

    UsagePointInfos(UsagePoint usagePoint, Clock clock) {
        add(usagePoint, clock);
    }

    UsagePointInfos(Iterable<? extends UsagePoint> usagePoints, Clock clock) {
        addAll(usagePoints, clock);
    }

    UsagePointInfo add(UsagePoint usagePoint, Clock clock) {
        UsagePointInfo result = new UsagePointInfo(usagePoint, clock);
        usagePoints.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends UsagePoint> usagePoints, Clock clock) {
        for (UsagePoint each : usagePoints) {
            add(each, clock);
        }
    }

    void addServiceLocationInfo() {
        for (UsagePointInfo each : usagePoints) {
            each.addServiceLocationInfo();
        }
    }
}