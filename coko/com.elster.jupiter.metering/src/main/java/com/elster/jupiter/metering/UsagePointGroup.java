package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.IdentifiedObject;

import java.util.Date;
import java.util.List;

public interface UsagePointGroup extends IdentifiedObject {

    String getType();

    List<UsagePoint> getMembers(Date date);

    boolean isMember(UsagePoint usagePoint, Date date);
}
