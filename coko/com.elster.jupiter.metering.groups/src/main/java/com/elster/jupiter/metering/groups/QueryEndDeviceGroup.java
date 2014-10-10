package com.elster.jupiter.metering.groups;

import com.elster.jupiter.util.conditions.Condition;

import java.util.List;

public interface QueryEndDeviceGroup extends EndDeviceGroup {

    String TYPE_IDENTIFIER = "QEG";

    public Condition getCondition();

    public List<SearchCriteria> getSearchCriteria();

}