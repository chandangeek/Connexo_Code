package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.conditions.Condition;

import java.time.Instant;

public interface UsagePointGroupBuilder {

    EnumeratedUsagePointGroupBuilder enumerated();

    QueryUsagePointGroupBuilder withConditions(Condition... conditions);

    interface EnumeratedUsagePointGroupBuilder {
        EnumeratedUsagePointGroup create();

        EnumeratedUsagePointGroupBuilder at(Instant at);
        EnumeratedUsagePointGroupBuilder containing(UsagePoint... moreUsagePoints);

        EnumeratedUsagePointGroupBuilder setName(String name);
        EnumeratedUsagePointGroupBuilder setMRID(String mRID);
        EnumeratedUsagePointGroupBuilder setDescription(String description);
        EnumeratedUsagePointGroupBuilder setAliasName(String aliasName);
        EnumeratedUsagePointGroupBuilder setType(String type);
    }

    interface QueryUsagePointGroupBuilder {
        QueryUsagePointGroup create();

        QueryUsagePointGroupBuilder withConditions(Condition... conditions);

        QueryUsagePointGroupBuilder setName(String name);
        QueryUsagePointGroupBuilder setMRID(String mRID);
        QueryUsagePointGroupBuilder setDescription(String description);
        QueryUsagePointGroupBuilder setAliasName(String aliasName);
        QueryUsagePointGroupBuilder setType(String type);
    }
}

