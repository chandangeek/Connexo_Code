package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.util.time.Interval;

import java.util.Date;
import java.util.List;

public interface EndDeviceGroup {

    long getId();

    String getType();

    List<EndDevice> getMembers(Date date);

    List<EndDeviceMembership> getMembers(Interval interval);

    boolean isMember(EndDevice endDevice, Date date);

    void setName(String name);

    void setMRID(String mrid);

    void setDescription(String description);

    void setAliasName(String aliasName);

    void save();

}
