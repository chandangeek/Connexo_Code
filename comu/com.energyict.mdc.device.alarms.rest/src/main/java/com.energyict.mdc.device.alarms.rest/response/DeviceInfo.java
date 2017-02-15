/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.response;


import com.elster.jupiter.metering.Location;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.data.Device;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceInfo {

    public long id;
    public String mRID;
    public String name;
    public String location;
    public IdWithNameInfo usagePoint;

    public DeviceInfo(Device device){
        this.id = device.getId();
        this.mRID = device.getmRID();
        this.name = device.getName();
        Optional<Location> location = device.getLocation();
        String formattedLocation = "";
        if (location.isPresent()) {
            List<List<String>> formattedLocationMembers = location.get().format();
            formattedLocationMembers.stream().skip(1).forEach(list ->
                    list.stream().filter(Objects::nonNull).findFirst().ifPresent(member -> list.set(list.indexOf(member), "\\r\\n" + member)));
            formattedLocation = formattedLocationMembers.stream()
                    .flatMap(List::stream).filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
        }
        this.location = formattedLocation;
        device.getUsagePoint().ifPresent(up -> usagePoint = new IdWithNameInfo(up.getId(), up.getName()));
    }

}
