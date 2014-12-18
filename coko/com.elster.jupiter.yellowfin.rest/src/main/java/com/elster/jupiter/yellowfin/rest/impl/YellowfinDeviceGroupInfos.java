package com.elster.jupiter.yellowfin.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class YellowfinDeviceGroupInfos {
    public int total;
    public List<YellowfinDeviceGroupInfo> groups = new ArrayList<>();

    public YellowfinDeviceGroupInfos() {
    }

    public YellowfinDeviceGroupInfos(YellowfinDeviceGroupInfo group) {
        add(group);
    }

    public YellowfinDeviceGroupInfos(Iterable<? extends YellowfinDeviceGroupInfo> groups) {
        addAll(groups);
    }

    public void add(YellowfinDeviceGroupInfo group) {
        groups.add(group);
        total++;
    }

    void addAll(Iterable<? extends YellowfinDeviceGroupInfo> groups) {
        for (YellowfinDeviceGroupInfo each : groups) {
            add(each);
        }
    }
}
