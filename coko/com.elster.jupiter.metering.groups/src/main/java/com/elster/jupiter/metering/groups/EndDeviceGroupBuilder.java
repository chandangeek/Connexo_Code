package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchablePropertyValue;

import java.time.Instant;

public interface EndDeviceGroupBuilder {

    EnumeratedEndDeviceGroupBuilder containing(EndDevice... moreDevices);

    QueryEndDeviceGroupBuilder withConditions(SearchablePropertyValue... conditions);

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

        QueryEndDeviceGroupBuilder withConditions(SearchablePropertyValue... conditions);
        QueryEndDeviceGroupBuilder setQueryProviderName(String queryProviderName);
        QueryEndDeviceGroupBuilder setSearchDomain(SearchDomain searchDomain);

        QueryEndDeviceGroupBuilder setName(String name);
        QueryEndDeviceGroupBuilder setMRID(String mRID);
        QueryEndDeviceGroupBuilder setDescription(String description);
        QueryEndDeviceGroupBuilder setAliasName(String aliasName);
        QueryEndDeviceGroupBuilder setType(String type);
        QueryEndDeviceGroupBuilder setLabel(String label);
    }
}

