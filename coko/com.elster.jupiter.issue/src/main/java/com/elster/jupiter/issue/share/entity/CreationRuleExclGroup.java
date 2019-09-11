package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

public interface CreationRuleExclGroup extends Entity {

    public void setCreationRule(CreationRule creationRule);

    public CreationRule getCreationRule();

    public void setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    public EndDeviceGroup getEndDeviceGroup();
}
