package com.elster.jupiter.metering.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class TimeOfUseFieldInfos {
    public int total;
    public List<TimeOfUseFieldInfo> timeOfUse = new ArrayList<>();

    public TimeOfUseFieldInfos() {
        for (total = 0; total <= 7; total++) {
            timeOfUse.add(new TimeOfUseFieldInfo(total));
        }
        total = timeOfUse.size();
    }
}