/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationMember;

import java.util.ArrayList;
import java.util.List;


public class LocationInfo {

    public long id;
    public List<LocationMemberInfo> members = new ArrayList<>();

    public LocationInfo(){

    }

    public LocationInfo(Location location){
        this.id = location.getId();
        addAll(location.getMembers());
    }

    private void addAll(List<? extends LocationMember> locationMembers){
        locationMembers.stream()
                .forEach(locationMember -> members.add(new LocationMemberInfo(locationMember)));
    }
}
