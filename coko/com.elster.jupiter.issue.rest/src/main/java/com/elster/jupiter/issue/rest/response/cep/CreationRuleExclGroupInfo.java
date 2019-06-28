package com.elster.jupiter.issue.rest.response.cep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationRuleExclGroupInfo {
    public long ruleId;
    public String ruleName;
    public long deviceGroupId;
    public String deviceGroupName;
    public boolean isGroupDynamic;
}
