/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.metering.rest.impl.LocationMemberInfo;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class LocationMemberInfos {
    public int total;
    public List<LocationMemberInfo> locationMemberInfos = new ArrayList<>();

    LocationMemberInfos() {
    }

    LocationMemberInfos(LocationMember locationMember) {
        add(locationMember);
    }

    LocationMemberInfos(Iterable<? extends LocationMember> locationMember) {
        addAll(locationMember);
    }

    LocationMemberInfo add(LocationMember locationMember) {
        LocationMemberInfo result = new LocationMemberInfo(locationMember);
        locationMemberInfos.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends LocationMember> meters) {
        for (LocationMember each : meters) {
            add(each);
        }
    }
}



