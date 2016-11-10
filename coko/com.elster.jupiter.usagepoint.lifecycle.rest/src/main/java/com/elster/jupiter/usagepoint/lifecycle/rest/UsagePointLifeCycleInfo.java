package com.elster.jupiter.usagepoint.lifecycle.rest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsagePointLifeCycleInfo {
    public long id;
    public String name;
    public long version;
    public boolean isDefault;
    public boolean obsolete;
    public List<UsagePointLifeCycleStateInfo> states;
}
