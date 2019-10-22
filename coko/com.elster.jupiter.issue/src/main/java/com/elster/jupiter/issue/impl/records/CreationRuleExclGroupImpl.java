package com.elster.jupiter.issue.impl.records;

import javax.inject.Inject;

import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleExclGroup;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class CreationRuleExclGroupImpl extends EntityImpl implements CreationRuleExclGroup {

    private Reference<CreationRule> creationRule = ValueReference.absent();

    private Reference<EndDeviceGroup> endDeviceGroup = ValueReference.absent();

    @Inject
    public CreationRuleExclGroupImpl(DataModel dataModel) {
        super(dataModel);
    }

    public void setCreationRule(CreationRule creationRule) {
        this.creationRule.set(creationRule);
    }

    @Override
    public CreationRule getCreationRule() {
        return this.creationRule.orNull();
    }

    public void setEndDeviceGroup(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup.set(endDeviceGroup);
    }

    @Override
    public EndDeviceGroup getEndDeviceGroup() {
        return this.endDeviceGroup.orNull();
    }
}
