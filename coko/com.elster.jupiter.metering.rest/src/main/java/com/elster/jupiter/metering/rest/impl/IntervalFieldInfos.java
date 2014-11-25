package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.TimeAttribute;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class IntervalFieldInfos {
    public int total;
    public List<IntervalFieldInfo> intervals = new ArrayList<>();

    public void add(TimeAttribute ta) {
        IntervalFieldInfo intervalFieldInfo = new IntervalFieldInfo();
        intervalFieldInfo.name = ta.getDescription();
        intervalFieldInfo.time = ta.getId();
        intervals.add(intervalFieldInfo);
        total++;
    }
}