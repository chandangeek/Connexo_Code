package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.util.conditions.Condition;

import java.time.Instant;

public interface EndDeviceGroupBuilder {

    EnumeratedEndDeviceGroupBuilder containing(EndDevice... moreDevices);

    QueryEndDeviceGroupBuilder withCondition(Condition condition);

    interface EnumeratedEndDeviceGroupBuilder {
        EnumeratedEndDeviceGroup create();

        EnumeratedEndDeviceGroupBuilder at(Instant at);
        EnumeratedEndDeviceGroupBuilder containing(EndDevice... moreDevices);

        EnumeratedEndDeviceGroupBuilder setName(String name);
        EnumeratedEndDeviceGroupBuilder setMRID(String mRID);
        EnumeratedEndDeviceGroupBuilder setDescription(String description);
        EnumeratedEndDeviceGroupBuilder setAliasName(String aliasName);
        EnumeratedEndDeviceGroupBuilder setType(String type);
        EnumeratedEndDeviceGroupBuilder setLabel(String label);
    }

    interface QueryEndDeviceGroupBuilder {
        QueryEndDeviceGroup create();

        QueryEndDeviceGroupBuilder withCondition(Condition condition);

        QueryEndDeviceGroupBuilder setName(String name);
        QueryEndDeviceGroupBuilder setMRID(String mRID);
        QueryEndDeviceGroupBuilder setDescription(String description);
        QueryEndDeviceGroupBuilder setAliasName(String aliasName);
        QueryEndDeviceGroupBuilder setType(String type);
        QueryEndDeviceGroupBuilder setLabel(String label);
        QueryEndDeviceGroupBuilder setQueryProviderName(String queryProviderName);
    }
}

