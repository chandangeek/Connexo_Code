package com.elster.jupiter.metering.rest.impl;

import java.util.ArrayList;
import java.util.List;

public class LocationInfo {
    public long id;
    public String name;
    public List<LocationMemberInfo> members = new ArrayList<>();

    public LocationInfo() {
    }
}
