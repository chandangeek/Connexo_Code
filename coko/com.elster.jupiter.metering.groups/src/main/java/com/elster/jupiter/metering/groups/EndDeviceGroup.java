package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Subquery;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface EndDeviceGroup extends HasId {

    String getType();

    List<EndDevice> getMembers(Instant instant);

    long getMemberCount(Instant instant);

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

    boolean isDynamic();

    void update();

    void delete();

    long getVersion();

    Subquery toSubQuery(String... fields);
}
