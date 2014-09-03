package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.groups.impl.EndDeviceQueryBuilderOperation;

import java.util.List;

public interface QueryEndDeviceGroup extends EndDeviceGroup {

    public List<SearchCriteria> getSearchCriteria();

    String TYPE_IDENTIFIER = "QEG";

}
