package com.elster.jupiter.metering.groups;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.metering.UsagePoint;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

public interface UsagePointGroup extends IdentifiedObject {

    long getId();

    /**
     * @return Type of this group.

     */
    String getType();

    List<UsagePoint> getMembers(Instant instant);

    List<UsagePointMembership> getMembers(Range<Instant> range);

    boolean isMember(UsagePoint usagePoint, Instant instant);

    void setName(String name);

    void setMRID(String mrid);

    void setDescription(String description);

    void setAliasName(String aliasName);

    void save();
    
    boolean isDynamic();
}
