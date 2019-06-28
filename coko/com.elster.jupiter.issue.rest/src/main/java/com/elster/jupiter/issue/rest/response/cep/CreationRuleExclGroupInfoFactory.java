package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleExclGroup;
import com.elster.jupiter.metering.groups.EndDeviceGroup;

public class CreationRuleExclGroupInfoFactory {

    public CreationRuleExclGroupInfo asInfo(CreationRuleExclGroup creationRuleExclGroup) {
        CreationRuleExclGroupInfo info = new CreationRuleExclGroupInfo();
        final CreationRule creationRule = creationRuleExclGroup.getCreationRule();
        if (creationRule != null) {
            info.ruleId = creationRule.getId();
            info.ruleName = creationRule.getName();
        }
        final EndDeviceGroup endDeviceGroup = creationRuleExclGroup.getEndDeviceGroup();
        if (endDeviceGroup != null) {
            info.deviceGroupId = endDeviceGroup.getId();
            info.deviceGroupName = endDeviceGroup.getName();
            info.isGroupDynamic = endDeviceGroup.isDynamic();
        }
        return info;
    }

}
