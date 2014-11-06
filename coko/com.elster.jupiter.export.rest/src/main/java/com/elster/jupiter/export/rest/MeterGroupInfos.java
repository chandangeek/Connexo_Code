package com.elster.jupiter.export.rest;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class MeterGroupInfos {

    public int total;
    public List<MeterGroupInfo> metergroups = new ArrayList<>();

    public MeterGroupInfos() {
    }

    public MeterGroupInfos(Iterable<? extends EndDeviceGroup> deviceGroups) {
        addAll(deviceGroups);
    }

    public void add(EndDeviceGroup endDeviceGroup) {
        metergroups.add(new MeterGroupInfo(endDeviceGroup));
        total++;
    }

    public void addAll(Iterable<? extends EndDeviceGroup> deviceGroups) {
        for (EndDeviceGroup each : deviceGroups) {
            add(each);
        }
    }
}
