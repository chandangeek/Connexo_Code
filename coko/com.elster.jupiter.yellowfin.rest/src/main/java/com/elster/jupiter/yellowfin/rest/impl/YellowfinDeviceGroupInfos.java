/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

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
    public YellowfinDeviceGroupInfos(List<EndDeviceGroup> groups) {
        addAll(groups);
    }

    public void add(YellowfinDeviceGroupInfo group) {
        groups.add(group);
        total++;
    }

    public void addAll(List<EndDeviceGroup> groups) {
        for (EndDeviceGroup each : groups) {
            add(new YellowfinDeviceGroupInfo(each.getName(), each.isDynamic()));
        }
    }


    void addAll(Iterable<? extends YellowfinDeviceGroupInfo> groups) {
        for (YellowfinDeviceGroupInfo each : groups) {
            add(each);
        }
    }
}
