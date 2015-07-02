package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.EndDevice;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

public interface EndDeviceGroup {

    long getId();

    String getType();

    List<EndDevice> getMembers(Instant instant);
    
    List<EndDevice> getMembers(Instant instant, int start, int limit);

    List<EndDeviceMembership> getMembers(Range<Instant> range);

    boolean isMember(EndDevice endDevice, Instant instant);

    void setName(String name);

    String getName();

    String getMRID();

    void setMRID(String mrid);

    String getLabel();

    void setLabel(String label);

    void setDescription(String description);

    void setAliasName(String aliasName);

    void setQueryProviderName(String queryProviderName);
    
    String getQueryProviderName();

    boolean isDynamic();

    void save();

    void delete();

}
